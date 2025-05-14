package su.terrafirmagreg.core.common.data.buds;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BudIndicator extends Block implements IFluidLoggable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;

	private static final VoxelShape AABB = Block.box(3, 0, 3, 13, 5, 13);
	private static final RandomSource RANDOM_SOURCE = RandomSource.create();

	private final Material material;


	public BudIndicator(Properties properties, Material material)
	{
		super(properties);
		this.material = material;

		registerDefaultState(this.defaultBlockState()
			.setValue(FACING, Direction.getRandom(RANDOM_SOURCE))
			.setValue(FLUID, FLUID.keyFor(Fluids.EMPTY)));

		if (GTCEu.isClientSide())
		{
			BudRenderer.create(this);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter view, BlockPos pos) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		var attachedBlock = pos.relative(Direction.DOWN);
		return level.getBlockState(attachedBlock).isFaceSturdy(level, attachedBlock, Direction.UP);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

		if (!canSurvive(state, level, pos)) {
			Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {

		final FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
		return getStateForDirection(context.getNearestLookingVerticalDirection())
				   .setValue(getFluidProperty(), getFluidProperty().keyForOrEmpty(fluid.getType()));
	}

	public BlockState getStateForDirection(Direction direction) {
		return defaultBlockState().setValue(FACING, direction);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
		builder.add(FLUID);
	}

	@OnlyIn(Dist.CLIENT)
	public static BlockColor tintedBlockColor() {
		return (state, reader, pos, tintIndex) -> {
			if (state.getBlock() instanceof BudIndicator block) {
				if (tintIndex == 0)
					return block.material.getMaterialRGB();
				else if (tintIndex == 1)
					return block.material.getMaterialSecondaryARGB();
			}
			return -1;
		};
	}

	@OnlyIn(Dist.CLIENT)
	public static ItemColor tintedItemColor() {
		return (stack, tintIndex) -> {
			if (stack.getItem() instanceof BudIndicatorItem item) {
				if (tintIndex == 0)
					return item.getMaterial().getMaterialRGB();
				else if (tintIndex == 1)
					return item.getMaterial().getMaterialSecondaryARGB();
			}
			return -1;
		};
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		return ChemicalHelper.get(TagPrefix.gem, material);
	}

	@Override
	public String getDescriptionId() {
		return "block.bud_indicator";
	}

	@Override
	public MutableComponent getName() {
		return Component.translatable("block.bud_indicator", material.getLocalizedName());
	}

	@Override
	public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid)
	{
		if (fluid instanceof FlowingFluid && !getFluidProperty().canContain(fluid))
		{
			return true;
		}
		return IFluidLoggable.super.canPlaceLiquid(level, pos, state, fluid);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn)
	{
		if (fluidStateIn.getType() instanceof FlowingFluid && !getFluidProperty().canContain(fluidStateIn.getType()))
		{
			level.destroyBlock(pos, true);
			level.setBlock(pos, fluidStateIn.createLegacyBlock(), 2);
			return true;
		}
		return IFluidLoggable.super.placeLiquid(level, pos, state, fluidStateIn);
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state)
	{
		return IFluidLoggable.super.getFluidLoggedState(state);
	}


	@Override
	public FluidProperty getFluidProperty() {
		return FLUID;
	}
}
