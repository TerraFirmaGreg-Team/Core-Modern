package su.terrafirmagreg.core.common.block.palmtree;

import java.util.function.Supplier;

import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.crop.DecayingBlock;
import net.dries007.tfc.common.blocks.rock.IFallableBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class PalmFruitBlock extends DecayingBlock implements IFallableBlock, IFluidLoggable {

    public static final FluidProperty FLUID = TFCBlockStateProperties.WATER;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 5.5, 11.0);

    public PalmFruitBlock(ExtendedProperties properties, Supplier<? extends Block> rotted) {
        super(properties.flammable(60, 30), rotted);

        registerDefaultState(getStateDefinition().any().setValue(getFluidProperty(), getFluidProperty().keyFor(Fluids.EMPTY)));
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        if (fluid instanceof FlowingFluid && !getFluidProperty().canContain(fluid)) {
            return true;
        }
        return IFluidLoggable.super.canPlaceLiquid(level, pos, state, fluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn) {
        if (fluidStateIn.getType() instanceof FlowingFluid && !getFluidProperty().canContain(fluidStateIn.getType())) {
            level.destroyBlock(pos, true);
            level.setBlock(pos, fluidStateIn.createLegacyBlock(), 2);
            return true;
        }
        return IFluidLoggable.super.placeLiquid(level, pos, state, fluidStateIn);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return IFluidLoggable.super.getFluidLoggedState(state);
    }

    @Override
    public FluidProperty getFluidProperty() {
        return FLUID;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.getBlockEntity(pos) instanceof DecayingBlockEntity decaying && decaying.getStack().isEmpty()) {
            decaying.setStack(new ItemStack(this));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        // Using destroyBlock instead of removeBlock to ensure the loot table and decay is handled correctly.
        if (level instanceof ServerLevel serverLevel) {
            level.destroyBlock(pos, true, player);

            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BAMBOO_HIT, SoundSource.AMBIENT, 0.5f, 2.0f);

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.defaultBlockState()), pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.5);

        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onceFinishedFalling(Level level, BlockPos pos, FallingBlockEntity entity) {
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = state.getOffset(level, pos);
        return DEFAULT_SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getFluidProperty());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        FluidHelpers.tickFluid(level, currentPos, state);
        if (!state.canSurvive(level, currentPos)) {
            level.destroyBlock(currentPos, true);
            return state.getFluidState().createLegacyBlock();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}
