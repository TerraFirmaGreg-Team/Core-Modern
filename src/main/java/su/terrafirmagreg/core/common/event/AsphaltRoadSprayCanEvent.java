package su.terrafirmagreg.core.common.event;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadDecal;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class AsphaltRoadSprayCanEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        BlockState state = event.getLevel().getBlockState(event.getPos());

        if (!supportsRoadMarking(state) || held.isEmpty()) {
            return;
        }

        if (isSolventSprayCan(held)) {
            if (currentDecal(state).isNone()) {
                return;
            }
            event.getLevel().setBlockAndUpdate(event.getPos(), clearMarking(state));
            damageSprayCan(player, held, event);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            player.swing(event.getHand(), true);
            return;
        }

        AsphaltRoadMarkingColor targetColor = sprayCanColor(held);
        if (targetColor.isNone()) {
            return;
        }

        AsphaltRoadDecal targetDecal = resolveTargetDecal(player, event.getHand());
        if (isSameMarking(state, targetDecal, targetColor)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        event.getLevel().setBlockAndUpdate(event.getPos(), applyMarking(state, targetDecal, targetColor));
        damageSprayCan(player, held, event);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.swing(event.getHand(), true);
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
                })
                .orElseGet(() -> decalFromPlayerFacing(player));
    }

    private static AsphaltRoadDecal decalFromPlayerFacing(Player player) {
        Direction facing = player.getDirection();
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        return vertical ? AsphaltRoadDecal.LINE_VERTICAL : AsphaltRoadDecal.LINE_HORIZONTAL;
    }

    private static void damageSprayCan(Player player, ItemStack held, PlayerInteractEvent.RightClickBlock event) {
        if (player.getAbilities().instabuild) {
            return;
        }
        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
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
