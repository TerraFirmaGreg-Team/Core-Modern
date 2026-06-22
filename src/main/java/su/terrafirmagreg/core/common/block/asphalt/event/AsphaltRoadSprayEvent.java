package su.terrafirmagreg.core.common.block.asphalt.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.item.ColorSprayBehaviour;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingMask;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.items.TFGItems_Asphalt;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadSprayEvent {

    private AsphaltRoadSprayEvent() {
    }

    public record SprayAction(
            InteractionHand sprayHand,
            ItemStack sprayStack,
            boolean solvent,
            boolean changesState,
            AsphaltRoadMarkingMask targetMask,
            Direction targetFacing,
            @Nullable DyeColor targetColor) {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (level.isClientSide()) {
            return;
        }

        SprayAction action = resolveSprayAction(level, event.getPos(), state, event.getEntity(),
                event.getHand(), event.getFace(), event.getHitVec());
        if (action == null) {
            return;
        }

        if (!action.changesState()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        BlockState targetState = action.solvent()
                ? clearMarking(state)
                : applyMarking(state, action.targetMask(), action.targetFacing(), action.targetColor());
        level.setBlockAndUpdate(event.getPos(), targetState);
        damageSprayCan(event.getEntity(), action.sprayStack(), action.sprayHand());

        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, event.getPos(), 0.85F, 1.0F);
        event.getEntity().swing(action.sprayHand(), true);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @Nullable
    public static SprayAction resolveSprayAction(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand,
            @Nullable Direction hitFace, @Nullable BlockHitResult hit) {
        if (!supportsRoadMarking(state)) {
            return null;
        }

        InteractionHand sprayHand = hand;
        ItemStack sprayStack = player.getItemInHand(sprayHand);
        if (isSprayCan(sprayStack)) {
            if (!canUseSprayFromHand(player, sprayHand)) {
                return null;
            }
        } else {
            sprayHand = sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            sprayStack = player.getItemInHand(sprayHand);
            if (!isSprayCan(sprayStack) || !canUseSprayFromHand(player, sprayHand)) {
                return null;
            }
        }

        if (sprayStack.is(GTItems.SPRAY_SOLVENT.get())) {
            if (state.getValue(AsphaltRoadHelper.MASK).isNone()) {
                return null;
            }
            return new SprayAction(sprayHand, sprayStack, true, true, AsphaltRoadMarkingMask.NONE, state.getValue(AsphaltRoadHelper.FACING), null);
        }

        Direction sprayDirection = resolveSprayDirection(level, pos, hitFace, hit, player);
        if (sprayDirection == null) {
            return null;
        }

        AsphaltRoadMarkingMask targetMask = resolveStencilMask(player, sprayHand);
        if (targetMask == null) {
            return null;
        }
        DyeColor targetColor = sprayCanColor(sprayStack);
        if (targetColor == null) {
            return null;
        }

        boolean sameMarking = isSameMarking(state, targetMask, sprayDirection, targetColor);
        return new SprayAction(sprayHand, sprayStack, false, !sameMarking, targetMask, sprayDirection, targetColor);
    }

    private static boolean canUseSprayFromHand(Player player, InteractionHand sprayHand) {
        if (sprayHand == InteractionHand.MAIN_HAND) {
            return true;
        }
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainStack.isEmpty() || mainStack.is(TFGTags.Items.ROAD_MARKING_STENCILS);
    }

    @Nullable
    private static Direction resolveSprayDirection(Level level, BlockPos clickedPos,
            @Nullable Direction hitFace,
            @Nullable BlockHitResult hit,
            Player player) {
        if (hitFace == null) {
            return null;
        }

        if (player instanceof DeployerFakePlayer) {
            switch (hitFace) {
                case DOWN: {
                    // not allowed
                    return null;
                }
                case NORTH:
                case EAST:
                case SOUTH:
                case WEST: {
                    // horizontal
                    return hitFace;
                }
                case UP: {
                    // decide by kinetic block axis
                    Direction deployerFacing = hitFace.getOpposite();
                    BlockPos deployerPos = clickedPos.relative(hitFace, 2);
                    BlockState deployerState = level.getBlockState(deployerPos);
                    if (!(deployerState.getBlock() instanceof DeployerBlock)
                            || deployerState.getValue(DeployerBlock.FACING) != deployerFacing
                            || !deployerState.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE)) {
                        return null;
                    }
                    return deployerState.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE) ? Direction.EAST : Direction.SOUTH;
                }
            }
        } else {
            if (hitFace != Direction.UP || hit == null) {
                return null;
            }

            double dx = hit.getLocation().x() - (clickedPos.getX() + 0.5);
            double dz = hit.getLocation().z() - (clickedPos.getZ() + 0.5);

            if (dx * dx + dz * dz < 1.0E-8) {// center
                return player.getDirection();
            }

            if (Math.abs(dx) >= Math.abs(dz)) {
                return dx >= 0.0 ? Direction.EAST : Direction.WEST;
            } else {
                return dz >= 0.0 ? Direction.SOUTH : Direction.NORTH;
            }
        }
        return null;
    }

    @Nullable
    private static AsphaltRoadMarkingMask resolveStencilMask(Player player, InteractionHand sprayHand) {
        ItemStack opposite = player.getItemInHand(sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

        if (sprayHand == InteractionHand.MAIN_HAND) {
            if (opposite.is(TFGTags.Items.ROAD_MARKING_STENCILS)) {
                return TFGItems_Asphalt.maskForStencil(opposite).orElse(null);
            }
            return AsphaltRoadMarkingMask.LINE;
        }

        if (opposite.isEmpty()) {
            return AsphaltRoadMarkingMask.LINE;
        }
        if (!opposite.is(TFGTags.Items.ROAD_MARKING_STENCILS)) {
            return null;
        }
        return TFGItems_Asphalt.maskForStencil(opposite).orElse(null);
    }

    @Nullable
    private static DyeColor sprayCanColor(ItemStack stack) {
        DyeColor[] dyeColors = DyeColor.values();
        int limit = Math.min(GTItems.SPRAY_CAN_DYES.length, dyeColors.length);
        for (int i = 0; i < limit; i++) {
            if (stack.is(GTItems.SPRAY_CAN_DYES[i].get())) {
                return dyeColors[i];
            }
        }
        return null;
    }

    public static boolean isSprayCan(ItemStack stack) {
        return stack.is(GTItems.SPRAY_SOLVENT.get()) || sprayCanColor(stack) != null;
    }

    private static boolean supportsRoadMarking(BlockState state) {
        return state.getBlock() instanceof AsphaltRoadBlock
                || state.getBlock() instanceof AsphaltRoadSlabBlock;
    }

    private static boolean isSameMarking(BlockState state, AsphaltRoadMarkingMask targetMask, Direction targetFacing, DyeColor targetColor) {
        return state.getValue(AsphaltRoadHelper.MASK) == targetMask
                && state.getValue(AsphaltRoadHelper.FACING) == targetFacing
                && state.getValue(AsphaltRoadHelper.COLOR) == targetColor;
    }

    private static BlockState applyMarking(BlockState state, AsphaltRoadMarkingMask mask, Direction facing, DyeColor color) {
        return state.setValue(AsphaltRoadHelper.MASK, mask)
                .setValue(AsphaltRoadHelper.FACING, facing)
                .setValue(AsphaltRoadHelper.COLOR, color);
    }

    private static BlockState clearMarking(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getBlock().defaultBlockState();
        }
        return state.getBlock().defaultBlockState()
                .setValue(AsphaltRoadSlabBlock.WATERLOGGED, state.getValue(AsphaltRoadSlabBlock.WATERLOGGED));
    }

    private static void damageSprayCan(Player player, ItemStack held, InteractionHand sprayHand) {
        if (player.getAbilities().instabuild) {
            return;
        }
        if (held.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof ColorSprayBehaviour spray) {
                    spray.useItemDurability(player, sprayHand, held, GTItems.SPRAY_EMPTY.asStack());
                    return;
                }
            }
        }
        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(sprayHand));
    }

}
