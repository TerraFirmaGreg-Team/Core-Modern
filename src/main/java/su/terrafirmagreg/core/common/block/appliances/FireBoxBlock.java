package su.terrafirmagreg.core.common.block.appliances;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.blocks.devices.IBellowsConsumer;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.size.IItemSize;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.capabilities.size.Weight;
import net.dries007.tfc.util.Fuel;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

@SuppressWarnings("deprecation")
public class FireBoxBlock extends DeviceBlock implements IBellowsConsumer, IItemSize {
    public static final IntegerProperty HEAT = TFCBlockStateProperties.HEAT_LEVEL;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE = Shapes.block();

    public FireBoxBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any()
                .setValue(HEAT, 0)
                .setValue(LIT, false)
                .setValue(FACING, Direction.NORTH));
    }

    // Handles particles and sounds.
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(HEAT) == 0) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        float airTicks = 0;
        if (be instanceof FireBoxBlockEntity fbbe) {
            airTicks = fbbe.getAirTicks();
        }
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        Direction facing = state.getValue(FACING);
        BlockPos posFront = pos.relative(facing);
        BlockPos posBack = pos.relative(facing);
        BlockPos posChimney = posBack.above();

        boolean isFrontOpen = level.getBlockState(posFront).isAir();
        boolean isBackOpen = level.getBlockState(posBack).isAir();
        boolean isChimneyOpen = level.getBlockState(posChimney).isAir();

        double forwardOffset = 0.7D;
        double backOffset = 0.6D;
        double chimneyOffset = 0.6D;

        if (rand.nextInt(8) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, rand.nextFloat() * 2.0f, rand.nextFloat() * 0.7F + 0.1F, false);
        }
        if (rand.nextInt(4) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, rand.nextFloat(), rand.nextFloat() * 0.7F + 0.1F, false);
        }
        if (isBackOpen && isChimneyOpen) {
            double sideSpread = (rand.nextDouble() - 0.5D) * 0.5D;
            double px = x + 0.5D - (facing.getStepX() * backOffset) + (facing.getStepZ() * sideSpread);
            double py = y + 0.5D + (rand.nextDouble() * 0.4D) + chimneyOffset;
            double pz = z + 0.5D - (facing.getStepZ() * backOffset) + (facing.getStepX() * sideSpread);

            for (int i = 0; i < rand.nextInt(2); i++) {
                level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.005D, 0);
            }
            for (int i = 0; i < rand.nextInt(2); i++) {
                int smokeChance = airTicks > 0 ? 3 : 8;
                if (rand.nextInt(smokeChance) == 0) {
                    SimpleParticleType smokeType = airTicks > 0 ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
                    level.addParticle(smokeType, px, py + 0.3D, pz, 0, 0.05D, 0);
                }
            }
        }
        if (isFrontOpen) {
            double sideSpread = (rand.nextDouble() - 0.5D) * 0.5D;
            double px = x + 0.5D + (facing.getStepX() * forwardOffset) + (facing.getStepZ() * sideSpread);
            double py = y + 0.5D + (rand.nextDouble() * 0.4D);
            double pz = z + 0.5D + (facing.getStepZ() * forwardOffset) + (facing.getStepX() * sideSpread);

            for (int i = 0; i < rand.nextInt(2); i++) {
                if (rand.nextInt(5) == 0) {
                    level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.005D, 0);
                }
            }
        }
    }

    // Burns players if they step on a lit fire box.
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity && level.getBlockState(pos).getValue(HEAT) > 0) {
            entity.hurt(entity.damageSources().hotFloor(), 1f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HEAT, LIT, FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            return state.setValue(FACING, facing);
        }
        return null;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        updateFireBoxRedstoneAndState(level, pos, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!state.is(oldState.getBlock())) {
            updateFireBoxRedstoneAndState(level, pos, state);
        }
    }

    /**
     * Handles redstone signal and state updates for neighbor changes, place events, and fuel consumption.
     * When powered, the fire box will ignite. When unpowered, the fire box will extinguish.
     * Also works with flags in FireBoxBlockEntity so that manual ignition and extinguishing can override redstone.
     * Although, it mostly gets overridden on block updates.
     */
    public static void updateFireBoxRedstoneAndState(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide)
            return;

        boolean hasIgniteSignal = false;

        for (Direction dir : Direction.values()) {
            int signal = getInputSignal(level, pos, dir);
            if (signal > 0) {
                hasIgniteSignal = true;
            }
        }

        FireBoxBlockEntity fireBox = level.getBlockEntity(pos, TFGBlockEntities.FIRE_BOX.get()).orElse(null);
        if (fireBox != null) {
            if (hasIgniteSignal) {
                fireBox.isRedstoneIgnited = true;
                if (!state.getValue(LIT) && fireBox.stateDelayTicks <= 0 && fireBox.burnTicks <= 0) {
                    fireBox.autoLight(state);
                }
            } else {
                if (fireBox.isRedstoneIgnited) {
                    fireBox.isRedstoneIgnited = false;
                    if (state.getValue(LIT) || fireBox.stateDelayTicks > 0) {
                        fireBox.extinguish(state);
                    }
                }
            }
        }
    }

    public static int getInputSignal(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        int signal = level.getSignal(neighborPos, direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        return Math.max(signal, neighborState.is(Blocks.REDSTONE_WIRE) ? neighborState.getValue(RedStoneWireBlock.POWER) : 0);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * Reports fuel level when using a comparator.
     */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        FireBoxBlockEntity fireBox = level.getBlockEntity(pos, TFGBlockEntities.FIRE_BOX.get()).orElse(null);
        if (fireBox != null && !fireBox.getInventory().getStackInSlot(0).isEmpty()) {
            ItemStack fuelStack = fireBox.getInventory().getStackInSlot(0);
            float stackSize = fuelStack.getCount();
            float maxStackSize = fuelStack.getMaxStackSize();
            return Mth.floor((stackSize / maxStackSize) * 14.0F) + 1;
        }
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        var heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmItem) {
            return InteractionResult.FAIL;
        }

        FireBoxBlockEntity fireBox = level.getBlockEntity(pos, TFGBlockEntities.FIRE_BOX.get()).orElse(null);
        if (fireBox != null) {
            if (!heldItem.isEmpty() && Fuel.get(heldItem) != null) {
                var itemHandler = fireBox.getCapability(Capabilities.ITEM).resolve().orElse(null);
                if (itemHandler != null && itemHandler.isItemValid(FireBoxBlockEntity.SLOT_FUEL_IN, heldItem)) {
                    ItemStack remainder = itemHandler.insertItem(FireBoxBlockEntity.SLOT_FUEL_IN, heldItem, true);
                    if (remainder.getCount() < heldItem.getCount()) {
                        if (!level.isClientSide()) {
                            ItemStack leftover = itemHandler.insertItem(FireBoxBlockEntity.SLOT_FUEL_IN, heldItem.copy(), false);
                            if (!player.isCreative()) {
                                player.setItemInHand(hand, leftover);
                            }
                            fireBox.markForSync();
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }

            if (player instanceof ServerPlayer serverPlayer) {
                Helpers.openScreen(serverPlayer, fireBox, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
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

    @Override
    public boolean canAcceptAir(Level level, BlockPos pos, BlockState state) {
        return IBellowsConsumer.super.canAcceptAir(level, pos, state);
    }

    @Override
    public void intakeAir(Level level, BlockPos pos, BlockState state, int amount) {
        level.getBlockEntity(pos, TFGBlockEntities.FIRE_BOX.get()).ifPresent(fireBox -> fireBox.intakeAir(amount));
    }
}
