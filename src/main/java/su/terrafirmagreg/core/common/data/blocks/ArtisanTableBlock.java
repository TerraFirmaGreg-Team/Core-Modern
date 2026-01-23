package su.terrafirmagreg.core.common.data.blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

/**
 * Artisan Table Block class which behaves similar to knapping.
 */
@SuppressWarnings("deprecation")
public class ArtisanTableBlock extends Block implements IForgeBlockExtension, EntityBlock {
    public static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 1, 1);
    private final ExtendedProperties properties;

    public ArtisanTableBlock(ExtendedProperties properties) {
        super(properties.properties());
        this.properties = properties;
        registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));

    }

    /**
     * Handles player interaction
     * @param state The current block state.
     * @param level The world level.
     * @param pos The block position.
     * @param player The player interacting.
     * @param hand The hand used.
     * @param hit The hit result.
     * @return The interaction result.
     */
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (player instanceof ServerPlayer serverPlayer) {
                level.getBlockEntity(pos, TFGBlockEntities.ARTISAN_TABLE.get())
                        .ifPresent(blockEntity -> Helpers.openScreen(serverPlayer, blockEntity, pos));
            }
            return InteractionResult.CONSUME;
        }
    }

    /**
     * Creates a new block entity.
     * @param pos The block position.
     * @param state The block state.
     * @return The new block entity.
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return TFGBlockEntities.ARTISAN_TABLE.get().create(pos, state);
    }

    /**
     * Returns the shape of the block.
     * @param state The block state.
     * @param level The block getter.
     * @param pos The block position.
     * @param context The collision context.
     * @return The voxel shape.
     */
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    /**
     * @param builder The state definition builder.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder
                .add(BlockStateProperties.HORIZONTAL_FACING));
    }

    /**
     * Determines the block state for placement.
     * @param context The block place context.
     * @return The block state to place.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    /**
     * @return The extended properties.
     */
    @Override
    public @NotNull ExtendedProperties getExtendedProperties() {
        return properties;
    }

    /**
     * Handles block removal with inventory ejection.
     * @param state The current block state.
     * @param level The world level.
     * @param pos The block position.
     * @param newState The new block state.
     * @param isMoving Whether the block is moving.
     */
    @Override
    public void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof su.terrafirmagreg.core.common.data.blockentity.ArtisanTableBlockEntity artisanTable) {
                artisanTable.ejectInventory();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
