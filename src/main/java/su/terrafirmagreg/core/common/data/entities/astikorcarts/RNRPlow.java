package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

/**
 * The RNR Plow entity, an extension of the Astikor Carts mod which
 * uses logic similar to its plow cart but adapted for use with the
 * Roads and Roofs mod's road construction mechanics.
 */
public final class RNRPlow extends AbstractDrawnInventoryEntity {
    // Inventory slot configuration.
    private static final int CART_SLOT_COUNT = 54;
    private static final int UPPER_START = 0;
    private static final int UPPER_END_EXCLUSIVE = CART_SLOT_COUNT / 2;
    private static final int LOWER_START = UPPER_END_EXCLUSIVE;
    private static final int LOWER_END_EXCLUSIVE = CART_SLOT_COUNT;

    // Plow configuration.
    private static final double BLADEOFFSET = 1.7D;
    private static final int MIN_PLOW_WIDTH = 1;
    private static final int MAX_PLOW_WIDTH = 5;
    private static final int DEFAULT_PLOW_WIDTH = 3;
    private static final float HALF_SPREAD_DEGREES = 38.0F;

    private static final EntityDataAccessor<Boolean> PLOWING = SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PLOW_WIDTH = SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PLOW_TEXTURE = SynchedEntityData.defineId(RNRPlow.class, EntityDataSerializers.INT);

    private static final ResourceLocation CRUSHED_BASE_COURSE_ID = ResourceLocation.fromNamespaceAndPath("rnr", "crushed_base_course");
    private static final ResourceLocation BASE_COURSE_BLOCK_ID = ResourceLocation.fromNamespaceAndPath("rnr", "base_course");

    // Array of block tags that can be converted into base course by the plow.
    // Added `#tfg:base_course_sources` to have an easy way to add more.
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

    /**
     * Instantiates a new RNR plow.
     *
     * @param type  the type
     * @param level the level
     */
    public RNRPlow(final EntityType<? extends RNRPlow> type, final Level level) {
        super(type, level);
        this.spacing = 1.3D;
    }

    // Item representation of the entity.
    @Override
    public @NotNull Item getCartItem() {
        return TFGItems.RNR_PLOW.get();
    }

    // Retrieves the configuration for the plow cart.
    // As far as I can tell, this doesn't cause any issues.
    @Override
    protected AstikorCartsConfig.CartConfig getConfig() {
        return AstikorCartsConfig.get().plow;
    }

    @Override
    protected ItemStackHandler initInventory() {
        this.inventory = new CartItemStackHandler<RNRPlow>(CART_SLOT_COUNT, this) {
        };
        return this.inventory;
    }

    public boolean getPlowing() {
        return this.entityData.get(PLOWING);
    }

    public int getPlowWidth() {
        return Mth.clamp(this.entityData.get(PLOW_WIDTH), MIN_PLOW_WIDTH, MAX_PLOW_WIDTH);
    }

    public void setPlowWidth(int width) {
        this.entityData.set(PLOW_WIDTH, Mth.clamp(width, MIN_PLOW_WIDTH, MAX_PLOW_WIDTH));
    }

    public int getPlowTextureVariant() {
        return Math.max(0, this.entityData.get(PLOW_TEXTURE));
    }

    public void setPlowTextureVariant(int variant) {
        this.entityData.set(PLOW_TEXTURE, Math.max(0, variant));
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

    // Array of blocks that can be ignored when checking for clear space above.
    private boolean ignoredCeilings(final BlockState state) {
        return state.isAir()
                || state.is(BlockTags.REPLACEABLE)
                || state.is(TFCTags.Blocks.CAN_BE_SNOW_PILED)
                || state.is(TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE);
    }

    // Uses ignoredCeilings to check if the block above is clear.
    private boolean isAboveClear(final ServerLevel server, final BlockPos pos) {
        final BlockState above = server.getBlockState(pos.above());
        return ignoredCeilings(above);
    }

    // Check if the block state is tagged with any of the base course source tags.
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
        // Warn the player if there is no crushed base course in the lower inventory.
        if (!hasAnyCrushedInLowerInventory(crushedItem) && player instanceof ServerPlayer sp) {
            if (this.tickCount - lastNoCrushedWarnTick >= 20) {
                sp.displayClientMessage(Component.translatable("tfg.gui.rnr_plow.empty_crushed_base_course"), true);
                lastNoCrushedWarnTick = this.tickCount;
            }
        }

        final int lanes = this.getPlowWidth();
        if (lanes <= 0)
            return;

        // Fun math to calculate the positions for plowing.
        final double yawRad = Math.toRadians(this.getYRot());
        final double fx = Math.sin(yawRad);
        final double fz = -Math.cos(yawRad);
        final double px = Math.cos(yawRad);
        final double pz = Math.sin(yawRad);
        final double centerX = this.getX() + fx * BLADEOFFSET;
        final double centerZ = this.getZ() + fz * BLADEOFFSET;
        final double mid = (lanes - 1) / 2.0;
        final double laneSpacing = 1.0;

        for (int i = 0; i < lanes; i++) {
            final double lateral = (i - mid) * laneSpacing;
            final double x = centerX + px * lateral;
            final double z = centerZ + pz * lateral;
            final Vec3 v = new Vec3(x, this.getY() - 0.5D, z);
            final BlockPos top = BlockPos.containing(v);
            final BlockPos below = top.below();

            if (server.getBlockState(top).is(baseCourse)) {
                if (tryApplyTopInventoryTransformation(server, top)) {
                    queueActivation(top);
                }
            }
            if (server.getBlockState(below).is(baseCourse)) {
                if (tryApplyTopInventoryTransformation(server, below)) {
                    queueActivation(below);
                }
            }

            if (!placeBaseCourseIfValid(server, top, baseCourse, crushedItem)) {
                placeBaseCourseIfValid(server, below, baseCourse, crushedItem);
            }
        }
    }

    // Check if there is any crushed base course item in the lower inventory.
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

    // Attempt to place a base course block at the specified position if valid.
    private boolean placeBaseCourseIfValid(final ServerLevel server, final BlockPos pos, final Block baseCourse, final Item crushedItem) {
        final BlockState in = server.getBlockState(pos);

        // Check if the block can be converted to base course.
        if (!isAnyTagged(in))
            return false;
        if (!isAboveClear(server, pos))
            return false;
        if (!consumeCrushedBaseCourse(crushedItem))
            return false;

        // Place the base course block. And play sound.
        server.setBlock(pos, baseCourse.defaultBlockState(), 3);
        server.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 0.2f, 1.0f);

        if (tryApplyTopInventoryTransformation(server, pos)) {
            queueActivation(pos);
        }
        return true;
    }

    // Consume one crushed base course item from the lower inventory.
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
                if (tryApplyTopInventoryTransformation(server, pos)) {
                    this.delayedActivations.add(pos);
                }
            }
        }
    }

    // Try to apply a block modification from the top inventory at the given position.
    private boolean tryApplyTopInventoryTransformation(final ServerLevel server, final BlockPos pos) {
        final Block baseCourse = ForgeRegistries.BLOCKS.getValue(BASE_COURSE_BLOCK_ID);
        if (baseCourse == null)
            return true;
        final BlockState in = server.getBlockState(pos);
        if (in.getBlock() != baseCourse)
            return true;

        final InvPeek peek = peekOneFromUpperInventory();
        if (peek == null || peek.one.isEmpty())
            return true;

        final Boolean result = tryRnrBlockModRecipe(server, pos, peek.one);
        if (result == null)
            return true;
        if (result) {
            shrinkUpperSlot(peek.slot, 1);
        }
        return false;
    }

    // Try to apply a Roads and Roofs block modification recipe at the specified position.
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
        final int start = UPPER_START;
        final int end = Math.min(UPPER_END_EXCLUSIVE, slots);

        // Non-random mode: return the first non-empty slot.
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
            // Random mode: collect all non-empty slots and pick one at random.
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

    /**
     * Boolean to randomize road placement.
     *
     * @return boolean
     */
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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PLOWING, false);
        this.entityData.define(PLOW_WIDTH, DEFAULT_PLOW_WIDTH);
        this.entityData.define(PLOW_TEXTURE, 0); // default variant 0
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(PLOWING, compound.getBoolean("Plowing"));
        if (compound.contains("PlowWidth")) {
            this.setPlowWidth(compound.getInt("PlowWidth"));
        }
        if (compound.contains("PlowTexture")) {
            this.setPlowTextureVariant(compound.getInt("PlowTexture"));
        }
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Plowing", this.entityData.get(PLOWING));
        compound.putInt("PlowWidth", this.getPlowWidth());
        compound.putInt("PlowTexture", this.getPlowTextureVariant());
    }

    // Opens the plow's inventory container for the player.
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
