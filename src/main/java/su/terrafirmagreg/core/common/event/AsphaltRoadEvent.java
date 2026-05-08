package su.terrafirmagreg.core.common.event;

import org.jetbrains.annotations.NotNull;

import com.therighthon.rnr.common.RNRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadDecal;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHotBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocksAsphalt;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AsphaltRoadEvent {
    private static final ResourceLocation ASPHALT_MIX_ID = ResourceLocation.fromNamespaceAndPath("tfg", "asphalt_mix");

    private record SprayContext(InteractionHand hand, ItemStack stack) {
    }

    private AsphaltRoadEvent() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack held = player.getItemInHand(hand);
        BlockState state = level.getBlockState(event.getPos());

        // Prefer spray logic first on asphalt road blocks.
        if (supportsRoadMarking(state) && handleSprayOnRoad(event, player, state)) {
            return;
        }
        handleAsphaltMixPour(event, player, hand, held);
    }

    private static void handleAsphaltMixPour(PlayerInteractEvent.RightClickBlock event, Player player, InteractionHand hand, ItemStack held) {
        Level level = event.getLevel();
        if (!containsAsphaltMix(held)) {
            return;
        }
        BlockPos clicked = event.getPos();
        BlockState ground = level.getBlockState(clicked);
        // Prevent vanilla fluid placement on asphalt roads.
        if (isAsphaltRoadFamily(ground) && !ground.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        if (!event.getFace().getAxis().isVertical() || event.getFace().getStepY() <= 0) {
            return;
        }
        if (!ground.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return;
        }
        BlockPos pourAbove = clicked.above();
        BlockState above = level.getBlockState(pourAbove);
        if (!above.isAir() && !above.canBeReplaced()) {
            return;
        }
        BlockState pourState = TFGBlocksAsphalt.ASPHALT_ROAD_POURING.getDefaultState();
        if (!level.setBlock(pourAbove, pourState, Block.UPDATE_ALL)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
        }
        player.swing(hand, true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static boolean isAsphaltRoadFamily(BlockState state) {
        return supportsRoadMarking(state) || state.getBlock() instanceof AsphaltRoadHotBlock;
    }

    private static boolean handleSprayOnRoad(PlayerInteractEvent.RightClickBlock event, Player player, BlockState state) {
        SprayContext spray = resolveSprayContext(player, event.getHand());
        if (spray == null) {
            return false;
        }
        InteractionHand sprayHand = spray.hand();
        ItemStack sprayStack = spray.stack();

        if (isSolventSprayCan(sprayStack)) {
            if (currentDecal(state).isNone()) {
                return false;
            }
            event.getLevel().setBlockAndUpdate(event.getPos(), clearMarking(state));
            damageSprayCan(player, sprayStack, sprayHand);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            player.swing(sprayHand, true);
            return true;
        }

        AsphaltRoadMarkingColor targetColor = sprayCanColor(sprayStack);

        AsphaltRoadDecal targetDecal = resolveTargetDecal(player, sprayHand);
        if (isSameMarking(state, targetDecal, targetColor)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        event.getLevel().setBlockAndUpdate(event.getPos(), applyMarking(state, targetDecal, targetColor));
        damageSprayCan(player, sprayStack, sprayHand);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.swing(sprayHand, true);
        return true;
    }

    private static SprayContext resolveSprayContext(Player player, InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);
        if (isSprayCan(usedStack)) {
            return new SprayContext(usedHand, usedStack);
        }
        InteractionHand oppositeHand = usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack oppositeStack = player.getItemInHand(oppositeHand);
        if (isSprayCan(oppositeStack) && canUseOffhandSpray(player, oppositeHand)) {
            return new SprayContext(oppositeHand, oppositeStack);
        }
        return null;
    }

    /**
     * Offhand spray can is only valid when main hand is empty or holding a stencil.
     */
    private static boolean canUseOffhandSpray(Player player, InteractionHand sprayHand) {
        InteractionHand mainHand = InteractionHand.MAIN_HAND;
        if (sprayHand == mainHand) {
            return true;
        }
        ItemStack mainStack = player.getItemInHand(mainHand);
        return mainStack.isEmpty() || RoadMarkingStencilItem.patternFrom(mainStack).isPresent();
    }

    private static boolean containsAsphaltMix(ItemStack stack) {
        return FluidUtil.getFluidContained(stack)
                .map(FluidStack::getFluid)
                .map(ForgeRegistries.FLUIDS::getKey)
                .filter(AsphaltRoadEvent::isAsphaltMixId)
                .isPresent();
    }

    private static boolean isAsphaltMixId(ResourceLocation fluidId) {
        if (fluidId == null || !ASPHALT_MIX_ID.getNamespace().equals(fluidId.getNamespace())) {
            return false;
        }
        String path = fluidId.getPath();
        return path.equals(ASPHALT_MIX_ID.getPath());
    }

    /**
     * No stencil or line stencil: decal follows facing. Cross stencil: {@link AsphaltRoadDecal#CROSS}, ignores facing.
     */
    private static AsphaltRoadDecal resolveTargetDecal(Player player, InteractionHand sprayHand) {
        ItemStack opposite = player.getItemInHand(sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return RoadMarkingStencilItem.patternFrom(opposite)
                .map(pattern -> switch (pattern) {
                    case LINE -> decalFromPlayerFacing(player);
                    case CROSS -> AsphaltRoadDecal.CROSS;
                    case ARROW -> arrowFromPlayerFacing(player);
                })
                .orElseGet(() -> decalFromPlayerFacing(player));
    }

    private static AsphaltRoadDecal decalFromPlayerFacing(Player player) {
        Direction facing = player.getDirection();
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        return vertical ? AsphaltRoadDecal.LINE_VERTICAL : AsphaltRoadDecal.LINE_HORIZONTAL;
    }

    private static AsphaltRoadDecal arrowFromPlayerFacing(Player player) {
        return switch (player.getDirection()) {
            case NORTH -> AsphaltRoadDecal.ARROW_NORTH;
            case EAST -> AsphaltRoadDecal.ARROW_EAST;
            case SOUTH -> AsphaltRoadDecal.ARROW_SOUTH;
            default -> AsphaltRoadDecal.ARROW_WEST;
        };
    }

    private static void damageSprayCan(Player player, ItemStack held, InteractionHand sprayHand) {
        if (player.getAbilities().instabuild) {
            return;
        }
        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(sprayHand));
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
}
