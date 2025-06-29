package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DecorativePlantBlock extends ExtendedBlock {

	public static final VoxelShape DEFAULT_SHAPE = Block.box(3.0F, 0.0F, 3.0F, 13.0F, 7.0F, 13.0F);

	private final VoxelShape shape;
	private final @Nullable Supplier<? extends Item> pickBlock;


	public DecorativePlantBlock(ExtendedProperties properties, VoxelShape shape, @Nullable Supplier<? extends Item> pickBlock) {
		super(properties);
		this.shape = shape;
		this.pickBlock = pickBlock;

		getExtendedProperties().offsetType(OffsetType.XZ);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
	{
		return shape;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
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
}
