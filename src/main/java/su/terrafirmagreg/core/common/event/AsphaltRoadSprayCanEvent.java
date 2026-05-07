package su.terrafirmagreg.core.common.event;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingColor;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadTopLayer;

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
            if (currentVerticalColor(state).isNone() && currentHorizontalColor(state).isNone()) {
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

        Direction facing = player.getDirection();
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        AsphaltRoadTopLayer topLayer = vertical ? AsphaltRoadTopLayer.VERTICAL : AsphaltRoadTopLayer.HORIZONTAL;
        if (isSameMarking(state, vertical, targetColor, topLayer)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        event.getLevel().setBlockAndUpdate(event.getPos(), applyMarking(state, vertical, targetColor, topLayer));
        damageSprayCan(player, held, event);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.swing(event.getHand(), true);
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

    private static AsphaltRoadMarkingColor currentVerticalColor(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.VERTICAL_COLOR);
        }
        return state.getValue(AsphaltRoadSlabBlock.VERTICAL_COLOR);
    }

    private static AsphaltRoadMarkingColor currentHorizontalColor(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.HORIZONTAL_COLOR);
        }
        return state.getValue(AsphaltRoadSlabBlock.HORIZONTAL_COLOR);
    }

    private static AsphaltRoadTopLayer currentTopLayer(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.TOP_LAYER);
        }
        return state.getValue(AsphaltRoadSlabBlock.TOP_LAYER);
    }

    private static boolean isSameMarking(BlockState state, boolean vertical, AsphaltRoadMarkingColor targetColor, AsphaltRoadTopLayer targetTopLayer) {
        AsphaltRoadMarkingColor current = vertical ? currentVerticalColor(state) : currentHorizontalColor(state);
        return current == targetColor && currentTopLayer(state) == targetTopLayer;
    }

    private static BlockState applyMarking(BlockState state, boolean vertical, AsphaltRoadMarkingColor color, AsphaltRoadTopLayer topLayer) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return vertical
                    ? state.setValue(AsphaltRoadBlock.VERTICAL_COLOR, color).setValue(AsphaltRoadBlock.TOP_LAYER, topLayer)
                    : state.setValue(AsphaltRoadBlock.HORIZONTAL_COLOR, color).setValue(AsphaltRoadBlock.TOP_LAYER, topLayer);
        }
        return vertical
                ? state.setValue(AsphaltRoadSlabBlock.VERTICAL_COLOR, color).setValue(AsphaltRoadSlabBlock.TOP_LAYER, topLayer)
                : state.setValue(AsphaltRoadSlabBlock.HORIZONTAL_COLOR, color).setValue(AsphaltRoadSlabBlock.TOP_LAYER, topLayer);
    }

    private static BlockState clearMarking(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.VERTICAL_COLOR, AsphaltRoadMarkingColor.NONE)
                    .setValue(AsphaltRoadBlock.HORIZONTAL_COLOR, AsphaltRoadMarkingColor.NONE);
        }
        return state.setValue(AsphaltRoadSlabBlock.VERTICAL_COLOR, AsphaltRoadMarkingColor.NONE)
                .setValue(AsphaltRoadSlabBlock.HORIZONTAL_COLOR, AsphaltRoadMarkingColor.NONE);
    }
}
