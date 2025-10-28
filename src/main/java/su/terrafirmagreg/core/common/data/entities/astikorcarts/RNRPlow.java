package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.therighthon.rnr.common.recipe.BlockModRecipe;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import de.mennomax.astikorcarts.config.AstikorCartsConfig;
import de.mennomax.astikorcarts.entity.AbstractDrawnInventoryEntity;
import de.mennomax.astikorcarts.util.CartItemStackHandler;

import su.terrafirmagreg.core.common.data.TFGItems;

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

    private static final ResourceLocation CRUSHED_BASE_COURSE_ID = ResourceLocation.fromNamespaceAndPath("rnr", "crushed_base_course");
    private static final ResourceLocation BASE_COURSE_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("rnr", "base_course");

    private static final List<TagKey<Block>> BASE_COURSE_SOURCE_TAGS = List.of(
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfg", "base_course_sources")),
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "dirt")),
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "mud")),
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "grass")));

    public ItemStackHandler inventory;

    private final List<BlockPos> delayedActivations = new ArrayList<>();
    private final List<BlockPos> queuedActivations = new ArrayList<>();

    private int lastNoCrushedWarnTick = -100;

    private boolean isRandom = false;

    public RNRPlow(final EntityType<? extends RNRPlow> type, final Level level) {
        super(type, level);
        this.spacing = 1.3D;
    }

    @Override
    public @NotNull Item getCartItem() {
        return TFGItems.RNR_PLOW.get();
    }

    @Override
    protected AstikorCartsConfig.CartConfig getConfig() {
        return AstikorCartsConfig.get().plow;
    }

    @Override
    protected ItemStackHandler initInventory() {
        this.inventory = new CartItemStackHandler<RNRPlow>(CART_SLOT_COUNT, this) {
            @Override
            protected void onLoad() {
                for (int i = 0; i < TOOL_SLOT_COUNT; i++) {
                    updateSlot(i);
                }
            }

            @Override
            protected void onContentsChanged(final int slot) {
                if (slot >= 0 && slot < TOOL_SLOT_COUNT) {
                    updateSlot(slot);
                }
            }
        };
        return this.inventory;
    }

    public boolean getPlowing() {
        return this.entityData.get(PLOWING);
    }

    @Override
    public void pulledTick() {
        super.pulledTick();

        if (!this.level().isClientSide) {
            processDelayedActivations();

            if (this.getPulling() == null) {
                return;
            }

            Player player = null;
            if (this.getPulling() instanceof Player pl) {
                player = pl;
            } else if (this.getPulling().getControllingPassenger() instanceof Player pl) {
                player = pl;
            }
            if (player != null && this.getPlowing()) {
                if (this.xo != this.getX() || this.zo != this.getZ()) {
                    this.plow(player);
                }
            }
        }
    }

    private boolean isPlantish(final BlockState state) {
        return state.isAir()
                || state.is(BlockTags.REPLACEABLE)
                || state.is(TFCTags.Blocks.CAN_BE_SNOW_PILED)
                || state.is(TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE);
    }

    private boolean isAboveClearOrPlant(final ServerLevel server, final BlockPos pos) {
        final BlockState above = server.getBlockState(pos.above());
        return isPlantish(above);
    }

    private static boolean isAnyTagged(final BlockState state) {
        for (final TagKey<Block> tag : RNRPlow.BASE_COURSE_SOURCE_TAGS) {
            if (state.is(tag))
                return true;
        }
        return false;
    }

    private void plow(final Player player) {
        if (!(this.level() instanceof ServerLevel server))
            return;

        final Block baseCourse = ForgeRegistries.BLOCKS.getValue(BASE_COURSE_BLOCK_ID);
        final Item crushedItem = ForgeRegistries.ITEMS.getValue(CRUSHED_BASE_COURSE_ID);
        if (baseCourse == null || crushedItem == null)
            return;

        if (!hasAnyCrushedInLowerInventory(crushedItem) && player instanceof ServerPlayer sp) {
            if (this.tickCount - lastNoCrushedWarnTick >= 20) {
                sp.displayClientMessage(Component.translatable("tfg.gui.rnr_plow.empty_crushed_base_course"), true);
                lastNoCrushedWarnTick = this.tickCount;
            }
        }

        for (int i = 0; i < TOOL_SLOT_COUNT; i++) {
            final float offset = 38.0F - i * 38.0F;
            final float yaw = (float) Math.toRadians(this.getYRot() - offset);

            final double x = this.getX() + Mth.sin(yaw) * BLADEOFFSET;
            final double z = this.getZ() - Mth.cos(yaw) * BLADEOFFSET;
            final Vec3 v = new Vec3(x, this.getY() - 0.5D, z);
            final BlockPos top = BlockPos.containing(v);
            final BlockPos below = top.below();

            if (server.getBlockState(top).is(baseCourse)) {
                if (!tryApplyTopInventoryTransformation(server, top)) {
                    queueActivation(top);
                }
            }
            if (server.getBlockState(below).is(baseCourse)) {
                if (!tryApplyTopInventoryTransformation(server, below)) {
                    queueActivation(below);
                }
            }

            if (!placeBaseCourseIfValid(server, top, baseCourse, crushedItem)) {
                placeBaseCourseIfValid(server, below, baseCourse, crushedItem);
            }
        }
    }

    private boolean hasAnyCrushedInLowerInventory(final Item crushed) {
        if (crushed == null)
            return false;
        final int slots = this.inventory.getSlots();
        final int start = Math.max(LOWER_START, 0);
        final int end = Math.min(LOWER_END_EXCLUSIVE, slots);
        for (int i = start; i < end; i++) {
            final ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(crushed)) {
                return true;
            }
        }
        return false;
    }

    private boolean placeBaseCourseIfValid(final ServerLevel server,
            final BlockPos pos,
            final Block baseCourse,
            final Item crushedItem) {
        final BlockState in = server.getBlockState(pos);

        if (!isAnyTagged(in)) {
            return false;
        }
        if (!isAboveClearOrPlant(server, pos)) {
            return false;
        }
        if (!consumeCrushedBaseCourse(crushedItem)) {
            return false;
        }

        server.setBlock(pos, baseCourse.defaultBlockState(), 3);
        server.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 0.2f, 1.0f);

        if (!tryApplyTopInventoryTransformation(server, pos)) {
            queueActivation(pos);
        }
        return true;
    }

    private boolean consumeCrushedBaseCourse(final Item crushed) {
        if (crushed == null)
            return false;
        final int slots = this.inventory.getSlots();
        final int start = Math.max(LOWER_START, 0);
        final int end = Math.min(LOWER_END_EXCLUSIVE, slots);

        for (int i = start; i < end; i++) {
            final ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(crushed)) {
                final ItemStack copy = stack.copy();
                copy.shrink(1);
                this.inventory.setStackInSlot(i, copy.isEmpty() ? ItemStack.EMPTY : copy);
                return true;
            }
        }
        return false;
    }

    private void queueActivation(final BlockPos pos) {
        final BlockPos imm = pos.immutable();
        if (!this.queuedActivations.contains(imm) && !this.delayedActivations.contains(imm)) {
            this.queuedActivations.add(imm);
        }
    }

    private void processDelayedActivations() {
        if (!(this.level() instanceof ServerLevel server))
            return;

        final Block baseCourse = ForgeRegistries.BLOCKS.getValue(BASE_COURSE_BLOCK_ID);
        if (baseCourse == null)
            return;

        final List<BlockPos> batch = new ArrayList<>(this.queuedActivations);
        this.queuedActivations.clear();
        batch.addAll(this.delayedActivations);
        this.delayedActivations.clear();

        final Set<BlockPos> unique = new HashSet<>(batch);

        for (final BlockPos pos : unique) {
            if (server.getBlockState(pos).is(baseCourse)) {
                if (!tryApplyTopInventoryTransformation(server, pos)) {
                    this.delayedActivations.add(pos);
                }
            }
        }
    }

    private boolean tryApplyTopInventoryTransformation(final ServerLevel server, final BlockPos pos) {
        final Block baseCourse = ForgeRegistries.BLOCKS.getValue(BASE_COURSE_BLOCK_ID);
        if (baseCourse == null)
            return false;
        final BlockState in = server.getBlockState(pos);
        if (in.getBlock() != baseCourse)
            return false;

        final InvPeek peek = peekOneFromUpperInventory();
        if (peek == null || peek.one.isEmpty())
            return false;

        final Boolean result = tryRnrBlockModRecipe(server, pos, peek.one);
        if (result == null)
            return false;
        if (result) {
            shrinkUpperSlot(peek.slot, 1);
        }
        return true;
    }

    @Nullable
    private Boolean tryRnrBlockModRecipe(final ServerLevel level, final BlockPos pos, final ItemStack held) {
        if (held.isEmpty())
            return null;

        final BlockState in = level.getBlockState(pos);
        final BlockModRecipe recipe = BlockModRecipe.getRecipe(in, held);
        if (recipe == null) {
            return null;
        }

        final BlockState out = recipe.getOutputBlock();
        if (out == null || out == in) {
            return null;
        }

        level.setBlock(pos, out, 3);
        return Boolean.TRUE.equals(recipe.consumesItem());
    }

    private record InvPeek(int slot, ItemStack one) {
    }

    private InvPeek peekOneFromUpperInventory() {
        final int slots = this.inventory.getSlots();
        final int start = Math.max(UPPER_START, 0);
        final int end = Math.min(UPPER_END_EXCLUSIVE, slots);

        if (!this.isRandom) {
            for (int i = start; i < end; i++) {
                final ItemStack stack = this.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    final ItemStack one = stack.copy();
                    one.setCount(1);
                    return new InvPeek(i, one);
                }
            }
            return null;
        } else {
            final List<Integer> filledIndices = new ArrayList<>();
            for (int i = start; i < end; i++) {
                final ItemStack stack = this.inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    filledIndices.add(i);
                }
            }
            if (filledIndices.isEmpty()) {
                return null;
            }
            final int chosenIndex = filledIndices.get(this.random.nextInt(filledIndices.size()));
            final ItemStack one = this.inventory.getStackInSlot(chosenIndex).copy();
            one.setCount(1);
            return new InvPeek(chosenIndex, one);
        }
    }

    private void shrinkUpperSlot(int slot, int count) {
        if (slot < 0 || slot >= this.inventory.getSlots() || count <= 0)
            return;
        final ItemStack stack = this.inventory.getStackInSlot(slot);
        if (stack.isEmpty())
            return;

        final ItemStack remaining = stack.copy();
        remaining.shrink(count);
        this.inventory.setStackInSlot(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    public boolean isRandomMode() {
        return this.isRandom;
    }

    public void setRandomMode(boolean value) {
        this.isRandom = value;
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
        if (!this.level().isClientSide && slot >= 0 && slot < TOOL_SLOT_COUNT) {
            final ItemStack s = this.inventory.getStackInSlot(slot);
            this.entityData.set(TOOLS.get(slot), s.isEmpty() ? ItemStack.EMPTY : s);
        }
    }

    public ItemStack getStackInSlot(final int i) {
        return this.entityData.get(TOOLS.get(i));
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
