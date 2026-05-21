package su.terrafirmagreg.core.common.block.girder;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TFGGirderBlock extends GirderBlock {
    BlockEntry<? extends Block> encasedShaftBlock;
    int placementHelperId;

    public TFGGirderBlock(Properties properties, int placementHelperId) {
        super(properties);
        this.placementHelperId = placementHelperId;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (AllBlocks.SHAFT.isIn(stack)) {
            KineticBlockEntity.switchToBlockState(level, pos, encasedShaftBlock.getDefaultState()
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                    .setValue(TOP, state.getValue(TOP))
                    .setValue(BOTTOM, state.getValue(BOTTOM))
                    .setValue(GirderEncasedShaftBlock.HORIZONTAL_AXIS, state.getValue(X) || hitResult.getDirection()
                            .getAxis() == Direction.Axis.Z ? Direction.Axis.Z : Direction.Axis.X));

            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
            if (!level.isClientSide && !player.isCreative()) {
                stack.shrink(1);
                if (stack.isEmpty())
                    player.setItemInHand(hand, ItemStack.EMPTY);
            }

            return InteractionResult.SUCCESS;
        }

        InteractionResult wrenchResult = tryGirderWrenchInteraction(stack, state, level, pos, player, hitResult);
        if (wrenchResult != null)
            return wrenchResult;

        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult)
                    .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;

        if (state.getValue(X) ^ state.getValue(Z)) {
            BlockPos pos = context.getClickedPos();
            LevelAccessor level = context.getLevel();
            if (!level.getBlockState(pos.above()).isAir())
                state = state.setValue(TOP, true);
            if (!level.getBlockState(pos.below()).isAir())
                state = state.setValue(BOTTOM, true);
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
            LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
        boolean prevX = state.getValue(X);
        boolean prevZ = state.getValue(Z);
        boolean wasHorizontal = prevX ^ prevZ;
        boolean prevTop = state.getValue(TOP);
        boolean prevBottom = state.getValue(BOTTOM);

        BlockState result = super.updateShape(state, direction, neighbourState, world, pos, neighbourPos);

        // Preserve horizontal orientation if super cleared it
        if (wasHorizontal && !result.getValue(X) && !result.getValue(Z)) {
            result = result.setValue(X, prevX).setValue(Z, prevZ);
        }

        boolean isHorizontal = result.getValue(X) ^ result.getValue(Z);

        if (isHorizontal) {
            // Restore previous TOP/BOTTOM — never clear via neighbor updates
            result = result.setValue(TOP, prevTop).setValue(BOTTOM, prevBottom);

            // Enable connector when a non-air block appears above or below
            if (direction == Direction.UP && !neighbourState.isAir())
                result = result.setValue(TOP, true);
            else if (direction == Direction.DOWN && !neighbourState.isAir())
                result = result.setValue(BOTTOM, true);
        }

        return result;
    }

    protected static InteractionResult tryGirderWrenchInteraction(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!AllItems.WRENCH.isIn(stack) || player.isShiftKeyDown())
            return null;
        if (TFGGirderWrenchBehavior.handleClick(level, pos, state, hitResult))
            return InteractionResult.sidedSuccess(level.isClientSide);
        return InteractionResult.FAIL;
    }
}
