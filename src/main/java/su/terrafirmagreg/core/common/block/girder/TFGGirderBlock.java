package su.terrafirmagreg.core.common.block.girder;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.girder.GirderBlock;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/***
 * Credit: Adapted from Create: More Girders
 */
public class TFGGirderBlock extends GirderBlock {
    boolean climbable;
    int placementHelperId;

    public TFGGirderBlock(Properties properties, int placementHelperId, boolean climbable) {
        super(properties);
        this.placementHelperId = placementHelperId;
        this.climbable = climbable;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (climbable && entity instanceof LivingEntity livingEntity) {
            double currentAccel = 0.15D * (livingEntity.getDeltaMovement().y < 0.3D ? 2.5D : 1.0D);
            Vec3 deltaMovement = livingEntity.getDeltaMovement();
            livingEntity.resetFallDistance();
            float f = 0.10F;
            double d0 = Mth.clamp(deltaMovement.x, -f, f);
            double d1 = Mth.clamp(deltaMovement.z, -f, f);
            double d2 = Math.max(deltaMovement.y, -f);
            if (d2 < 0.0 && !livingEntity.getFeetBlockState().isScaffolding(livingEntity) &&
                    livingEntity.isSuppressingSlidingDownLadder() &&
                    livingEntity instanceof Player) {
                d2 = Math.min(deltaMovement.y + currentAccel, 0.0D);
            }
            if (livingEntity.horizontalCollision) {
                d2 = 0.22F;
            }
            deltaMovement = new Vec3(d0, d2, d1);
            entity.setDeltaMovement(deltaMovement);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);

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

        if (wasHorizontal && !result.getValue(X) && !result.getValue(Z)) {
            result = result.setValue(X, prevX).setValue(Z, prevZ);
        }

        boolean isHorizontal = result.getValue(X) ^ result.getValue(Z);

        if (isHorizontal) {
            result = result.setValue(TOP, prevTop).setValue(BOTTOM, prevBottom);

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
