package su.terrafirmagreg.core.common.block.appliances;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;

import net.dries007.tfc.client.IHighlightHandler;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.capabilities.size.IItemSize;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.capabilities.size.Weight;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxTopOvenBlockEntity;

@SuppressWarnings("deprecation")
public class FireBoxTopOvenBlock extends DeviceBlock implements IHighlightHandler, IItemSize {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape[] SHAPES = Helpers.computeHorizontalShapes(d -> Shapes.join(Shapes.block(), Helpers.rotateShape(d, 2.0D, 1.0D, 0.0D, 14.0D, 11.0D, 15.0D), BooleanOp.ONLY_FIRST));

    public static final VoxelShape[][] SLOT_SHAPES = new VoxelShape[4][9];
    public static final AABB[][] SLOT_AABBS = new AABB[4][9];
    public static final Vec3[][] SLOT_CENTERS = new Vec3[4][9];

    static {
        double[] x = { 2.0, 6.0, 10.0, 14.0 };
        double[] z = { 1.0, 5.5, 9.5, 14.0 };

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int d = dir.get2DDataValue();
            for (int i = 0; i < 9; i++) {
                int r = i / 3, c = i % 3;
                SLOT_AABBS[d][i] = Helpers.rotateShape(dir, x[c], 0.0D, z[r], x[c + 1], 2.0D, z[r + 1]).bounds().inflate(0.01D);
                SLOT_CENTERS[d][i] = SLOT_AABBS[d][i].getCenter();
                SLOT_SHAPES[d][i] = Helpers.rotateShape(dir, x[c] + 0.5D, 1.0D, z[r] + (r == 0 ? 0.5D : 0.0D), x[c + 1] - 0.5D, 2.0D, z[r + 1] - (r == 2 ? 1.0D : 0.5D));
            }
        }
    }

    public static int getSlotForSelection(BlockHitResult result, Direction facing) {
        final Vec3 location = result.getLocation();
        final BlockPos pos = result.getBlockPos();
        final int dirIndex = facing.get2DDataValue();
        for (int i = 0; i < 9; i++) {
            if (SLOT_AABBS[dirIndex][i].move(pos).contains(location)) {
                return i;
            }
        }
        return -1;
    }

    public FireBoxTopOvenBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            return state.setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return null;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity && (level.getBlockEntity(pos) instanceof FireBoxTopOvenBlockEntity fbtobe && fbtobe.getTemperature() > 100)) {
            entity.hurt(entity.damageSources().hotFloor(), 1f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * Reports all item stacks using a comparator.
     */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
            return Mth.clamp(blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(ItemHandlerHelper::calcRedstoneFromInventory)
                    .orElse(0) * 2, 0, 15);
        }
        return 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return SHAPES[facing.get2DDataValue()];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return SHAPES[facing.get2DDataValue()];
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmItem) {
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(pos) instanceof FireBoxTopOvenBlockEntity oven) {
            Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
            int slot = getSlotForSelection(hit, facing);
            IItemHandler inv = oven.getInventory();

            // Targeted slot interactions
            if (slot != -1) {
                // If output item is in this zone, either crouch or not crouch removes it.
                if (!inv.getStackInSlot(9 + slot).isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = inv.extractItem(9 + slot, 64, false);
                        tryRoundHeatAndGiveToPlayer(player, extracted);
                        oven.markForSync();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
                // If input item is in this zone, crouch+RMB removes it.
                if (!inv.getStackInSlot(slot).isEmpty() && (player.isShiftKeyDown() || player.isCrouching())) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = inv.extractItem(slot, 64, false);
                        tryRoundHeatAndGiveToPlayer(player, extracted);
                        oven.markForSync();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
                // Insert into targeted slot
                if (!heldItem.isEmpty() && inv.getStackInSlot(slot).isEmpty() && inv.getStackInSlot(9 + slot).isEmpty() && oven.isItemValid(slot, heldItem)) {
                    if (!level.isClientSide()) {
                        ItemStack toInsert = heldItem.split(1);
                        inv.insertItem(slot, toInsert, false);
                        oven.markForSync();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            } else {
                // If holding item, try to fill all empty slots with the held stack.
                if (!heldItem.isEmpty()) {
                    boolean insertedAny = false;
                    for (int i = 0; i < 9 && !heldItem.isEmpty(); i++) {
                        if (inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(9 + i).isEmpty() && oven.isItemValid(i, heldItem)) {
                            if (!level.isClientSide()) {
                                ItemStack toInsert = heldItem.split(1);
                                inv.insertItem(i, toInsert, false);
                            }
                            insertedAny = true;
                        }
                    }
                    if (insertedAny) {
                        if (!level.isClientSide()) {
                            oven.markForSync();
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                } else {
                    // Extract all outputs.
                    boolean extractedAny = false;
                    for (int i = 9; i < 18; i++) {
                        if (!inv.getStackInSlot(i).isEmpty()) {
                            if (!level.isClientSide()) {
                                ItemStack extracted = inv.extractItem(i, 64, false);
                                tryRoundHeatAndGiveToPlayer(player, extracted);
                            }
                            extractedAny = true;
                        }
                    }
                    if (extractedAny) {
                        if (!level.isClientSide()) {
                            oven.markForSync();
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }

                    // If crouching and no outputs, extract an input item.
                    if (player.isShiftKeyDown() || player.isCrouching()) {
                        for (int i = 8; i >= 0; i--) {
                            if (!inv.getStackInSlot(i).isEmpty()) {
                                if (!level.isClientSide()) {
                                    ItemStack extracted = inv.extractItem(i, 64, false);
                                    tryRoundHeatAndGiveToPlayer(player, extracted);
                                    oven.markForSync();
                                }
                                return InteractionResult.sidedSuccess(level.isClientSide());
                            }
                        }
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    public static void tryRoundHeatAndGiveToPlayer(Player player, ItemStack extracted) {
        if (extracted.isEmpty()) {
            return;
        }
        IHeat extractedHeat = HeatCapability.get(extracted);
        if (extractedHeat != null) {
            float tempExtracted = extractedHeat.getTemperature();
            ItemStack bestMatchingStack = null;
            float smallestDiff = Float.MAX_VALUE;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (invStack.isEmpty() || invStack.getCount() >= invStack.getMaxStackSize()) {
                    continue;
                }
                IHeat invHeat = HeatCapability.get(invStack);
                if (invHeat == null) {
                    continue;
                }
                float tempInv = invHeat.getTemperature();
                float maxTemp = Math.max(tempExtracted, tempInv);
                float diff = Math.abs(tempExtracted - tempInv);
                if (maxTemp > 0 && (diff / maxTemp) > 0.30f) {
                    continue;
                }

                ItemStack testExtracted = extracted.copy();
                copyHeat(invHeat, testExtracted);
                if (ItemHandlerHelper.canItemStacksStack(invStack, testExtracted)) {
                    if (diff < smallestDiff) {
                        smallestDiff = diff;
                        bestMatchingStack = invStack;
                    }
                }
            }

            if (bestMatchingStack != null) {
                IHeat targetHeat = HeatCapability.get(bestMatchingStack);
                if (targetHeat != null) {
                    copyHeat(targetHeat, extracted);
                }
            }
        }

        ItemHandlerHelper.giveItemToPlayer(player, extracted);
    }

    @SuppressWarnings("unchecked")
    private static void copyHeat(IHeat sourceHeat, ItemStack targetStack) {
        IHeat targetHeat = HeatCapability.get(targetStack);
        if (targetHeat != null) {
            if (sourceHeat instanceof ICapabilitySerializable<?> sourceSerial && targetHeat instanceof ICapabilitySerializable<?> targetSerial) {
                CompoundTag nbt = ((ICapabilitySerializable<CompoundTag>) sourceSerial).serializeNBT();
                ((ICapabilitySerializable<CompoundTag>) targetSerial).deserializeNBT(nbt);
            } else {
                targetHeat.setTemperature(sourceHeat.getTemperature());
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean drawHighlight(Level level, BlockPos pos, Player player, BlockHitResult rayTrace, PoseStack stack, MultiBufferSource buffers, Vec3 rendererPosition) {
        BlockState state = level.getBlockState(pos);
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        int slot = getSlotForSelection(rayTrace, facing);
        if (slot != -1) {
            int dirIndex = facing.get2DDataValue();
            IHighlightHandler.drawBox(stack, SLOT_SHAPES[dirIndex][slot], buffers, pos, rendererPosition, 1.0f, 0.7f, 0.3f, 0.7f);
            return true;
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof FireBoxTopOvenBlockEntity oven && oven.getTemperature() > 0) {
            var inv = oven.getInventory();
            Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
            int dirIndex = facing.get2DDataValue();
            for (int i = 0; i < 9; i++) {
                if ((!inv.getStackInSlot(i).isEmpty() || !inv.getStackInSlot(9 + i).isEmpty()) && random.nextFloat() < 0.2f) {
                    Vec3 center = SLOT_CENTERS[dirIndex][i];
                    double x = center.x + pos.getX();
                    double y = center.y + pos.getY();
                    double z = center.z + pos.getZ();
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.005D, 0);
                }
                double x = pos.getX() + 0.3D;
                double y = pos.getY() + 0.5D;
                double z = pos.getZ() + 0.3D;
                level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.25F, random.nextFloat() * 0.7F + 0.4F, false);
                if (random.nextFloat() > 0.97f) {
                    level.addParticle(ParticleTypes.FLAME, x + random.nextFloat() * 0.6D, y + random.nextFloat() * 0.2D, z + random.nextFloat() * 0.6D, 0, 0.005D, 0);
                }
            }

        }
    }

    @Override
    public Size getSize(ItemStack stack) {
        return Size.LARGE;
    }

    @Override
    public Weight getWeight(ItemStack stack) {
        return Weight.MEDIUM;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
