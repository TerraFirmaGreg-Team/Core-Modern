package su.terrafirmagreg.core.common.block;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
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

import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGSounds;

@SuppressWarnings("deprecation")
public class GasBurnerBlock extends DeviceBlock {
    public static final IntegerProperty HEAT = TFCBlockStateProperties.HEAT_LEVEL;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SET_LEVEL = IntegerProperty.create("set_level", 0, 10);

    protected static final VoxelShape SHAPE = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D), Block.box(1.0D, 9.0D, 1.0D, 15.0D, 16.0D, 15.0D));

    public GasBurnerBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any()
                .setValue(HEAT, 0)
                .setValue(LIT, false)
                .setValue(SET_LEVEL, 0)
                .setValue(FACING, Direction.NORTH));
    }

    // Handles particles and sounds.
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(HEAT) == 0)
            return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.875D;
        double z = pos.getZ() + 0.5D;

        float[] offsets = Helpers.square(rand, 0.5f);
        double spawnX = x + offsets[0];
        double spawnY = pos.getY() + 1.0D;
        double spawnZ = z + offsets[1];

        if (rand.nextInt(8) == 0) {
            level.playLocalSound(x, y, z, TFGSounds.BURNER.getMainEvent(), SoundSource.BLOCKS, rand.nextFloat() * 0.5f, rand.nextFloat() * 0.7F + 0.1F, false);
        }
        for (int i = 0; i < rand.nextInt(2); i++) {
            level.addParticle(ParticleTypes.SMOKE, spawnX, y + (rand.nextDouble() / 2), spawnZ, 0, 0.005D, 0);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, spawnX, spawnY + (rand.nextFloat() / 10), spawnZ, 0, 0, 0);
        }
    }

    // Burns players if they step on a lit burner.
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.fireImmune() && entity instanceof LivingEntity && level.getBlockState(pos).getValue(HEAT) > 0) {
            entity.hurt(entity.damageSources().hotFloor(), 1f);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HEAT, LIT, FACING, SET_LEVEL));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();

            int maxTempSignal = 0;
            for (Direction dir : Direction.values()) {
                int signal = getInputSignal(level, pos, dir);
                if (signal >= 3 && signal <= 15 && signal > maxTempSignal) {
                    maxTempSignal = signal;
                } else {
                    maxTempSignal = 15;
                }
            }
            int setLevel = (maxTempSignal >= 3) ? Mth.clamp(maxTempSignal - 3, 0, 10) : 0;
            return state.setValue(FACING, facing).setValue(SET_LEVEL, setLevel);
        }
        return null;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        updateBurnerRedstoneAndState(level, pos, state);
    }

    // Need to set the collision shape to a full block to support grills and pots.
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!state.is(oldState.getBlock())) {
            updateBurnerRedstoneAndState(level, pos, state);
        }
    }

    /**
     * Handles redstone signal and state updates for neighbor changes, place events, and fuel consumption.
     * - When redstone level is 15, the burner will ignite.
     * - When all redstone levels are 0, the burner will extinguish.
     * - Redstone levels are between 3 and 14, will set the max temp limit.
     * - Redstone level 0 and 15 will set max temp to highest limit for ignition with or without limit setting.
     * Also works with flags in GasBurnerBlockEntity so that manual ignition and extinguishing can override redstone.
     * Although, it mostly gets overridden on block updates.
     */
    public static void updateBurnerRedstoneAndState(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide)
            return;

        int maxSignal = 0;
        int maxTempSignal = 15;
        boolean hasIgniteSignal = false;
        boolean foundValidSignal = false;

        for (Direction dir : Direction.values()) {
            int signal = getInputSignal(level, pos, dir);
            if (signal > maxSignal) {
                maxSignal = signal;
            }
            if (signal == 15) {
                hasIgniteSignal = true;
            }

            if (signal >= 1 && signal <= 15) {
                if (!foundValidSignal) {
                    maxTempSignal = signal;
                    foundValidSignal = true;
                } else if (signal > maxTempSignal) {
                    maxTempSignal = signal;
                }
            }
        }

        BlockState newState = state;
        int setLevel = Mth.clamp(maxTempSignal - 3, 0, 10);

        if (state.getValue(SET_LEVEL) != setLevel) {
            newState = state.setValue(SET_LEVEL, setLevel);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }

        GasBurnerBlockEntity burner = level.getBlockEntity(pos, TFGBlockEntities.GAS_BURNER.get()).orElse(null);
        if (burner != null) {
            if (hasIgniteSignal) {
                burner.isRedstoneIgnited = true;
                if (!newState.getValue(LIT) && burner.stateDelayTicks <= 0 && burner.burnTicks <= 0) {
                    burner.autoLight(newState);
                }
            } else if (maxSignal == 0) {
                if (burner.isRedstoneIgnited) {
                    burner.isRedstoneIgnited = false;
                    if (newState.getValue(LIT) || burner.stateDelayTicks > 0) {
                        burner.extinguish(newState);
                    }
                }
            } else {
                if (newState.getValue(LIT) || burner.stateDelayTicks > 0) {
                    burner.isRedstoneIgnited = true;
                }
            }
        }
    }

    public static int getInputSignal(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        int signal = level.getSignal(neighborPos, direction);
        if (signal >= 15) {
            return signal;
        }
        BlockState neighborState = level.getBlockState(neighborPos);
        return Math.max(signal, neighborState.is(Blocks.REDSTONE_WIRE) ? neighborState.getValue(RedStoneWireBlock.POWER) : 0);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * Reports fuel tank level when using a comparator.
     */
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        GasBurnerBlockEntity burner = level.getBlockEntity(pos, TFGBlockEntities.GAS_BURNER.get()).orElse(null);
        if (burner != null) {
            return burner.getFuelLevel();
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

        GasBurnerBlockEntity burner = level.getBlockEntity(pos, TFGBlockEntities.GAS_BURNER.get()).orElse(null);
        if (burner != null) {
            if (FluidHelpers.transferBetweenBlockEntityAndItem(heldItem, burner, player, hand)) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                Helpers.openScreen(serverPlayer, burner, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

}
