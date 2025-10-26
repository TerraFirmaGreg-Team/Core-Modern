package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import de.mennomax.astikorcarts.config.AstikorCartsConfig;
import de.mennomax.astikorcarts.entity.AbstractDrawnInventoryEntity;
import de.mennomax.astikorcarts.util.CartItemStackHandler;

public final class RNRPlow extends AbstractDrawnInventoryEntity {
    private static final int TOOL_SLOT_COUNT = 3;
    private static final int CART_SLOT_COUNT = 36;

    private static final int UPPER_START = 0;
    private static final int UPPER_END_EXCLUSIVE = 18;
    private static final int LOWER_START = 18;
    private static final int LOWER_END_EXCLUSIVE = CART_SLOT_COUNT;

    private static final double BLADEOFFSET = 1.7D;
    private static final EntityDataAccessor<Boolean> PLOWING = SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.BOOLEAN);
    private static final ImmutableList<EntityDataAccessor<ItemStack>> TOOLS = ImmutableList.of(
            SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.ITEM_STACK),
            SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.ITEM_STACK),
            SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.ITEM_STACK));

    @SuppressWarnings({ "removal" })
    private static final ResourceLocation CRUSHED_BASE_COURSE_ID = new ResourceLocation("rnr", "crushed_base_course");
    @SuppressWarnings({ "removal" })
    private static final ResourceLocation BASE_COURSE_BLOCK_ID = new ResourceLocation("rnr", "base_course");

    private final List<BlockPos> delayedActivations = new ArrayList<>();
    private final List<BlockPos> queuedActivations = new ArrayList<>();

    private static final UUID FP_UUID = UUID.nameUUIDFromBytes("rnr_plow_fake_player".getBytes());
    private static final GameProfile FP_PROFILE = new GameProfile(FP_UUID, "[RNR_Plow]");

    public RNRPlow(final EntityType<? extends Entity> entityTypeIn, final Level worldIn) {
        super(entityTypeIn, worldIn);
        this.spacing = 1.3D;
    }

    @Override
    protected AstikorCartsConfig.CartConfig getConfig() {
        return AstikorCartsConfig.get().plow;
    }

    @Override
    protected ItemStackHandler initInventory() {
        return new CartItemStackHandler<RNRPlow>(CART_SLOT_COUNT, this) {
            @Override
            protected void onLoad() {
                for (int i = 0; i < Math.min(TOOLS.size(), this.getSlots()); i++) {
                    RNRPlow.this.updateSlot(i);
                }
            }

            @Override
            protected void onContentsChanged(final int slot) {
                RNRPlow.this.updateSlot(slot);
            }
        };
    }

    public boolean getPlowing() {
        return this.entityData.get(PLOWING);
    }

    @Override
    public void pulledTick() {
        super.pulledTick();

        if (!this.level().isClientSide) {
            processDelayedActivations();
            if (!this.queuedActivations.isEmpty()) {
                this.delayedActivations.addAll(this.queuedActivations);
                this.queuedActivations.clear();
            }
        }

        if (this.getPulling() == null)
            return;

        if (!this.level().isClientSide) {
            Player player = null;
            if (this.getPulling() instanceof Player pl) {
                player = pl;
            }
            if (this.entityData.get(PLOWING) && player != null) {
                this.plow(player);
            }
        }
    }

    @SuppressWarnings({ "removal" })
    private void plow(final Player player) {
        final TagKey<Block> dirtTag = TagKey.create(Registries.BLOCK, new ResourceLocation("minecraft", "dirt"));
        final TagKey<Block> mudTag = TagKey.create(Registries.BLOCK, new ResourceLocation("tfc", "mud"));

        final Block baseCourse = ForgeRegistries.BLOCKS.getValue(BASE_COURSE_BLOCK_ID);
        if (baseCourse == null)
            return;

        final Item crushedItem = ForgeRegistries.ITEMS.getValue(CRUSHED_BASE_COURSE_ID);

        boolean ranOut = false;

        for (int i = 0; i < TOOL_SLOT_COUNT; i++) {
            final float offset = 38.0F - i * 38.0F;
            final double blockPosX = this.getX() + Mth.sin((float) Math.toRadians(this.getYRot() - offset)) * BLADEOFFSET;
            final double blockPosZ = this.getZ() - Mth.cos((float) Math.toRadians(this.getYRot() - offset)) * BLADEOFFSET;
            final Vec3 vec3 = new Vec3(blockPosX, this.getY() - 0.5D, blockPosZ);
            final BlockPos surfacePos = BlockPos.containing(vec3);

            final BlockPos[] targets = new BlockPos[] { surfacePos, surfacePos.below() };
            for (final BlockPos pos : targets) {
                final BlockState state = this.level().getBlockState(pos);

                if (state.getBlock() == baseCourse) {
                    queueActivation(pos);
                    continue;
                }

                if (state.is(dirtTag) || state.is(mudTag)) {
                    if (!ranOut && consumeCrushedBaseCourse(crushedItem)) {
                        if (!this.level().isClientSide) {
                            this.level().setBlock(pos, baseCourse.defaultBlockState(), 3);
                            queueActivation(pos);
                        }
                    } else {
                        ranOut = true;
                    }
                }
            }
        }

        if (ranOut && player instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.literal("Out of crushed base course"), true);
        }
    }

    private boolean consumeCrushedBaseCourse(final Item crushed) {
        if (crushed == null)
            return false;
        final int slots = this.inventory.getSlots();
        final int start = Math.max(LOWER_START, 0);
        final int end = Math.min(LOWER_END_EXCLUSIVE, slots);

        for (int i = start; i < end; i++) {
            final ItemStack stack = this.inventory.getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            if (!stack.is(crushed))
                continue;

            final ItemStack newStack = stack.copy();
            newStack.shrink(1);
            this.inventory.setStackInSlot(i, newStack.isEmpty() ? ItemStack.EMPTY : newStack);
            return true;
        }
        return false;
    }

    private void queueActivation(final BlockPos pos) {
        if (!this.queuedActivations.contains(pos)) {
            this.queuedActivations.add(pos.immutable());
        }
    }

    private void processDelayedActivations() {
        if (this.delayedActivations.isEmpty())
            return;
        if (!(this.level() instanceof ServerLevel server)) {
            this.delayedActivations.clear();
            return;
        }

        final Block baseCourse = ForgeRegistries.BLOCKS.getValue(BASE_COURSE_BLOCK_ID);
        if (baseCourse == null) {
            this.delayedActivations.clear();
            return;
        }

        for (final BlockPos pos : this.delayedActivations) {
            final BlockState state = server.getBlockState(pos);
            if (state.getBlock() != baseCourse)
                continue;

            final InvPeek peek = peekOneFromUpperInventory();
            if (peek == null)
                continue;

            final boolean used = simulateRightClickOnBlock(server, pos, peek.one);
            if (used) {
                shrinkUpperSlot(peek.slot, 1);
            }
        }

        this.delayedActivations.clear();
    }

    private static final class InvPeek {
        final int slot;
        final ItemStack one;

        InvPeek(int slot, ItemStack one) {
            this.slot = slot;
            this.one = one;
        }
    }

    private InvPeek peekOneFromUpperInventory() {
        final int slots = this.inventory.getSlots();
        final int start = Math.max(UPPER_START, 0);
        final int end = Math.min(UPPER_END_EXCLUSIVE, slots);

        for (int i = start; i < end; i++) {
            final ItemStack stack = this.inventory.getStackInSlot(i);
            if (stack.isEmpty())
                continue;

            final ItemStack one = stack.copy();
            one.setCount(1);
            return new InvPeek(i, one);
        }
        return null;
    }

    private void shrinkUpperSlot(int slot, int count) {
        if (slot < 0 || slot >= this.inventory.getSlots())
            return;
        final ItemStack stack = this.inventory.getStackInSlot(slot);
        if (stack.isEmpty() || count <= 0)
            return;

        final ItemStack remaining = stack.copy();
        remaining.shrink(count);
        this.inventory.setStackInSlot(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    private boolean simulateRightClickOnBlock(final ServerLevel server, final BlockPos pos, final ItemStack one) {
        if (one.isEmpty())
            return false;

        final Vec3 hit = Vec3.atCenterOf(pos);
        final BlockHitResult bhr = new BlockHitResult(hit, Direction.UP, pos, false);

        final UseOnContext ctx = new UseOnContext(server, null, InteractionHand.MAIN_HAND, one, bhr);

        final InteractionResult result = one.useOn(ctx);

        return result.consumesAction() || result == InteractionResult.SUCCESS;
    }

    public @NotNull InteractionResult interact(final Player player, final @NotNull InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            this.openContainer(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (!this.level().isClientSide) {
            this.entityData.set(PLOWING, !this.entityData.get(PLOWING));
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void updateSlot(final int slot) {
        if (!this.level().isClientSide) {
            if (slot < TOOLS.size()) {
                final ItemStack stack = this.inventory.getStackInSlot(slot);
                this.entityData.set(TOOLS.get(slot), stack.copy());
            }
        }
    }

    public ItemStack getStackInSlot(final int i) {
        return this.entityData.get(TOOLS.get(i));
    }

    @Override
    public Item getCartItem() {
        return su.terrafirmagreg.core.common.data.TFGItems.RNR_PLOW.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PLOWING, false);
        for (final EntityDataAccessor<ItemStack> param : TOOLS) {
            this.entityData.define(param, ItemStack.EMPTY);
        }
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(PLOWING, compound.getBoolean("Plowing"));
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Plowing", this.entityData.get(PLOWING));
    }

    private void openContainer(final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(
                    serverPlayer,
                    new SimpleMenuProvider(
                            (windowId, playerInventory, p) -> new RNRPlowContainer(windowId, playerInventory, this),
                            this.getDisplayName()),
                    buf -> buf.writeInt(this.getId()));
        }
    }
}
