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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadDecal;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStencilPattern;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadSprayEvent {

    private record SprayContext(InteractionHand hand, ItemStack stack) {
    }

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
        SprayContext spray = resolveSprayContext(player, event.getHand());
        if (spray == null) {
            return;
        }

        if (isSolventSprayCan(spray.stack())) {
            handleSolventSpray(event, player, state, spray);
            return;
        }

        handleColorSpray(event, player, state, spray);
    }

    private static void handleSolventSpray(
            PlayerInteractEvent.RightClickBlock event,
            Player player,
            BlockState state,
            SprayContext spray) {
        if (currentDecal(state).isNone() && currentMarkingColor(state).isNone()) {
            return;
        }

        finishSpray(event, player, spray, clearMarking(state), true);
    }

    private static void handleColorSpray(
            PlayerInteractEvent.RightClickBlock event,
            Player player,
            BlockState state,
            SprayContext spray) {
        AsphaltRoadMarkingColor targetColor = sprayCanColor(spray.stack());
        Direction sprayDirection = resolveSprayDirection(event, player);
        if (sprayDirection == null) {
            return;
        }
        AsphaltRoadStencilPattern stencilPattern = resolveStencilPattern(player, spray.hand());
        AsphaltRoadDecal targetDecal = decalFromPattern(stencilPattern, sprayDirection);

        if (isSameMarking(state, targetDecal, targetColor)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        finishSpray(event, player, spray, applyMarking(state, targetDecal, targetColor), true);
    }

    private static void finishSpray(
            PlayerInteractEvent.RightClickBlock event,
            Player player,
            SprayContext spray,
            BlockState targetState,
            boolean consumeDurability) {
        event.getLevel().setBlockAndUpdate(event.getPos(), targetState);
        if (consumeDurability) {
            damageSprayCan(player, spray.stack(), spray.hand());
            playSprayCanSound(event.getLevel(), event.getPos());
            player.swing(spray.hand(), true);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @Nullable
    private static SprayContext resolveSprayContext(Player player, InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);
        if (isSprayCan(usedStack)) {
            if (!canUseSprayFromHand(player, usedHand)) {
                return null;
            }
            return new SprayContext(usedHand, usedStack);
        }
        InteractionHand oppositeHand = usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack oppositeStack = player.getItemInHand(oppositeHand);
        if (isSprayCan(oppositeStack) && canUseSprayFromHand(player, oppositeHand)) {
            return new SprayContext(oppositeHand, oppositeStack);
        }
        return null;
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
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !"gtceu".equals(id.getNamespace())) {
            return AsphaltRoadMarkingColor.NONE;
        }
        String path = id.getPath();
        if (!path.endsWith("_dye_spray_can")) {
            return AsphaltRoadMarkingColor.NONE;
        }
        String colorName = path.substring(0, path.length() - "_dye_spray_can".length());
        return AsphaltRoadMarkingColor.fromSerializedName(colorName);
    }

    private static boolean isSolventSprayCan(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "gtceu".equals(id.getNamespace()) && "solvent_spray_can".equals(id.getPath());
    }

    private static boolean isSprayCan(ItemStack stack) {
        return isSolventSprayCan(stack) || !sprayCanColor(stack).isNone();
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

    private static void playSprayCanSound(Level level, BlockPos pos) {
        GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, pos, 0.85F, 1.0F);
    }

}
