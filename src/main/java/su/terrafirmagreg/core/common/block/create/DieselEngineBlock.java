/*
 * Originally from [Create Diesel Generators] (https://github.com/george8188625/Create-Diesel-Generators)
 * Licensed under the MIT license.
 */

package su.terrafirmagreg.core.common.block.create;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.content.kinetics.simpleRelays.ITieredShaftBlock;
import electrolyte.greate.registry.GreateTagPrefixes;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class DieselEngineBlock extends DirectionalKineticBlock implements IBE<DieselEngineBlockEntity>, ITieredBlock, ITieredShaftBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private int tier;
    private Supplier<Block> shaftType;
    private Material material;

    public DieselEngineBlock(Properties properties, Material material) {
        super(properties);
        this.material = material;
        this.shaftType = () -> ChemicalHelper.getBlock(GreateTagPrefixes.shaft, material);
        registerDefaultState(super.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        level.setBlock(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)), 2);
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.hasBlockEntity())
            withBlockEntityDo(worldIn, pos, be -> {
                if (worldIn.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING))
                    be.targetSpeed.setValue(nbe.targetSpeed.getValue());
                if (worldIn.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING))
                    be.targetSpeed.setValue(nbe.targetSpeed.getValue());
                if (worldIn.getBlockEntity(pos.relative(state.getValue(FACING))) instanceof DieselEngineBlockEntity nbe && nbe.getBlockState().getValue(FACING) == state.getValue(FACING).getOpposite())
                    be.targetSpeed.setValue(nbe.targetSpeed.getValue() == 1 ? 0 : 1);
                if (worldIn.getBlockEntity(pos.relative(state.getValue(FACING).getOpposite())) instanceof DieselEngineBlockEntity nbe
                        && nbe.getBlockState().getValue(FACING) == state.getValue(FACING).getOpposite())
                    be.targetSpeed.setValue(nbe.targetSpeed.getValue() == 1 ? 0 : 1);
            });

        super.onPlace(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public Class<DieselEngineBlockEntity> getBlockEntityClass() {
        return DieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DieselEngineBlockEntity> getBlockEntityType() {
        return TFGBlockEntities.DIESEL_ENGINE.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof SmartBlockEntity be))
            return InteractionResult.PASS;

        IFluidHandler tank = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);

        if (tank == null)
            return InteractionResult.PASS;

        if (stack.getItem() instanceof BucketItem || stack.getItem() instanceof MilkBucketItem) {
            Fluid fluid = stack.getItem() instanceof BucketItem bi ? bi.getFluid() : ForgeMod.MILK.get();

            if (!tank.getFluidInTank(0).isEmpty())
                return InteractionResult.FAIL;

            tank.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (!player.isCreative())
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));

            return InteractionResult.SUCCESS;
        }

        IFluidHandlerItem itemTank = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (itemTank == null)
            return InteractionResult.PASS;

        itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        if (state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH) {
            return Shapes.or(Block.box(3, 3, 0, 13, 13, 16), Block.box(0, 0, 0, 16, 4, 16));
        } else if (state.getValue(FACING) == Direction.DOWN) {
            return Shapes.or(Block.box(3, 0, 3, 13, 16, 13), Block.box(0, 4, 4, 16, 12, 12));
        } else if (state.getValue(FACING) == Direction.UP) {
            return Shapes.or(Block.box(3, 0, 3, 13, 16, 13), Block.box(4, 4, 0, 12, 12, 16));
        } else {
            return Shapes.or(Block.box(0, 3, 3, 16, 13, 13), Block.box(0, 0, 0, 16, 4, 16));
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING).getAxis() == face.getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING).getAxis();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public Block getShaft() {
        return shaftType.get();
    }
}
