package su.terrafirmagreg.core.common.event;

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
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadDecal;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStencilPattern;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadSprayEvent {

    private AsphaltRoadSprayEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (level.isClientSide() || !supportsRoadMarking(state)) {
            return;
        }

        Player player = event.getEntity();
        InteractionHand sprayHand = event.getHand();
        ItemStack sprayStack = player.getItemInHand(sprayHand);
        if (isSprayCan(sprayStack)) {
            if (!canUseSprayFromHand(player, sprayHand)) {
                return;
            }
        } else {
            sprayHand = sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            sprayStack = player.getItemInHand(sprayHand);
            if (!isSprayCan(sprayStack) || !canUseSprayFromHand(player, sprayHand)) {
                return;
            }
        }

        BlockState targetState;
        if (sprayStack.is(GTItems.SPRAY_SOLVENT.get())) {
            if (currentDecal(state).isNone() && currentMarkingColor(state).isNone()) {
                return;
            }

            targetState = clearMarking(state);
        } else {
            Direction sprayDirection = resolveSprayDirection(event, player);
            if (sprayDirection == null) {
                return;
            }

            AsphaltRoadStencilPattern stencilPattern = resolveStencilPattern(player, sprayHand);
            AsphaltRoadDecal targetDecal = decalFromPattern(stencilPattern, sprayDirection);
            AsphaltRoadMarkingColor targetColor = sprayCanColor(sprayStack);

            if (isSameMarking(state, targetDecal, targetColor)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            targetState = applyMarking(state, targetDecal, targetColor);
        }

        level.setBlockAndUpdate(event.getPos(), targetState);
        damageSprayCan(player, sprayStack, sprayHand);

        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, event.getPos(), 0.85F, 1.0F);
        player.swing(sprayHand, true);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static boolean canUseSprayFromHand(Player player, InteractionHand sprayHand) {
        if (sprayHand == InteractionHand.MAIN_HAND) {
            return true;
        }
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainStack.isEmpty() || RoadMarkingStencilItem.patternFrom(mainStack).isPresent();
    }

    @Nullable
    private static Direction resolveSprayDirection(PlayerInteractEvent.RightClickBlock event, Player player) {
        Direction hitFace = event.getFace();
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
                    BlockPos deployerPos = event.getPos().relative(hitFace, 2);
                    BlockState deployerState = event.getLevel().getBlockState(deployerPos);
                    if (!(deployerState.getBlock() instanceof DeployerBlock)
                            || deployerState.getValue(DeployerBlock.FACING) != deployerFacing
                            || !deployerState.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE)) {
                        return null;
                    }
                    return deployerState.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE) ? Direction.EAST : Direction.SOUTH;
                }
            }
        } else {
            BlockHitResult hit = event.getHitVec();
            if (hitFace != Direction.UP || hit == null) {
                return null;
            }

            BlockPos clickedPos = event.getPos();
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

    private static AsphaltRoadStencilPattern resolveStencilPattern(Player player, InteractionHand sprayHand) {
        ItemStack opposite = player.getItemInHand(sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return RoadMarkingStencilItem.patternFrom(opposite).orElse(AsphaltRoadStencilPattern.LINE);
    }

    private static AsphaltRoadDecal decalFromPattern(AsphaltRoadStencilPattern pattern, Direction direction) {
        return switch (pattern) {
            case LINE -> decalFromHorizontalFacing(direction);
            case CROSS -> AsphaltRoadDecal.CROSS;
            case ARROW -> arrowFromHorizontalFacing(direction);
        };
    }

    private static AsphaltRoadDecal decalFromHorizontalFacing(Direction facing) {
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        return vertical ? AsphaltRoadDecal.LINE_VERTICAL : AsphaltRoadDecal.LINE_HORIZONTAL;
    }

    private static AsphaltRoadDecal arrowFromHorizontalFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> AsphaltRoadDecal.ARROW_NORTH;
            case EAST -> AsphaltRoadDecal.ARROW_EAST;
            case SOUTH -> AsphaltRoadDecal.ARROW_SOUTH;
            default -> AsphaltRoadDecal.ARROW_WEST;
        };
    }

    private static AsphaltRoadMarkingColor sprayCanColor(ItemStack stack) {
        DyeColor[] dyeColors = DyeColor.values();
        int limit = Math.min(GTItems.SPRAY_CAN_DYES.length, dyeColors.length);
        for (int i = 0; i < limit; i++) {
            if (stack.is(GTItems.SPRAY_CAN_DYES[i].get())) {
                return AsphaltRoadMarkingColor.fromSerializedName(dyeColors[i].getName());
            }
        }
        return AsphaltRoadMarkingColor.NONE;
    }

    private static boolean isSprayCan(ItemStack stack) {
        return stack.is(GTItems.SPRAY_SOLVENT.get()) || !sprayCanColor(stack).isNone();
    }

    private static boolean supportsRoadMarking(BlockState state) {
        return state.getBlock() instanceof AsphaltRoadBlock
                || state.getBlock() instanceof AsphaltRoadSlabBlock;
    }

    private static AsphaltRoadDecal currentDecal(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.DECAL);
        }
        return state.getValue(AsphaltRoadSlabBlock.DECAL);
    }

    private static AsphaltRoadMarkingColor currentMarkingColor(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.COLOR);
        }
        return state.getValue(AsphaltRoadSlabBlock.COLOR);
    }

    private static boolean isSameMarking(BlockState state, AsphaltRoadDecal targetDecal, AsphaltRoadMarkingColor targetColor) {
        return currentDecal(state) == targetDecal && currentMarkingColor(state) == targetColor;
    }

    private static BlockState applyMarking(BlockState state, AsphaltRoadDecal decal, AsphaltRoadMarkingColor color) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.DECAL, decal).setValue(AsphaltRoadBlock.COLOR, color);
        }
        return state.setValue(AsphaltRoadSlabBlock.DECAL, decal).setValue(AsphaltRoadSlabBlock.COLOR, color);
    }

    private static BlockState clearMarking(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.DECAL, AsphaltRoadDecal.NONE)
                    .setValue(AsphaltRoadBlock.COLOR, AsphaltRoadMarkingColor.NONE);
        }
        return state.setValue(AsphaltRoadSlabBlock.DECAL, AsphaltRoadDecal.NONE)
                .setValue(AsphaltRoadSlabBlock.COLOR, AsphaltRoadMarkingColor.NONE);
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
