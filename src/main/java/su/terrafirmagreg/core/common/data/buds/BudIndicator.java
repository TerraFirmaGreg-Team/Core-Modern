package su.terrafirmagreg.core.common.data.buds;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import lombok.Getter;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BudIndicator extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	private static final VoxelShape AABB = Block.box(3, 0, 3, 13, 5, 13);
	private static final RandomSource RANDOM_SOURCE = RandomSource.create();

	@Getter
	private final Material material;


	public BudIndicator(Properties properties, Material material)
	{
		super(properties);
		this.material = material;

		registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.getRandom(RANDOM_SOURCE)));

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
		return getStateForDirection(context.getNearestLookingVerticalDirection());
	}

	public BlockState getStateForDirection(Direction direction) {
		return defaultBlockState().setValue(FACING, direction);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
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
}
