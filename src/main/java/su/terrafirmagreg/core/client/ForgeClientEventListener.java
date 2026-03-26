package su.terrafirmagreg.core.client;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGPlant;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.event.AdvancedOreProspectorEventHelper;
import su.terrafirmagreg.core.common.event.NormalOreProspectorEventHelper;
import su.terrafirmagreg.core.common.event.OreProspectorEvent;
import su.terrafirmagreg.core.common.event.WeakOreProspectorEventHelper;
import su.terrafirmagreg.core.common.perf.SupportCache;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    /**
     * Evict client-side SupportCache chunk to prevent stale cache info.
     * Clients don't get placement/removal updates for chunks that aren't in range, so we can't trust the cache
     * for those chunks.
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            ChunkPos pos = event.getChunk().getPos();
            SupportCache.forLevel(level).evictChunk(pos.x, pos.z);
        }
    }

    @SubscribeEvent
    public static void onTooltip(@NotNull ItemTooltipEvent event) {
        var tooltip = event.getToolTip();
        var stack = event.getItemStack();

        // Check Weak helpers
        for (WeakOreProspectorEventHelper helper : OreProspectorEvent.getWeakOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Check Normal helpers
        for (NormalOreProspectorEventHelper helper : OreProspectorEvent.getNormalOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_count")
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Check Advanced helpers
        for (AdvancedOreProspectorEventHelper helper : OreProspectorEvent.getAdvancedOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                // Determine the mode key based on centersOnly
                String modeKey = helper.isCentersOnly()
                        ? "tfg.tooltip.ore_prospector_mode_vein"
                        : "tfg.tooltip.ore_prospector_mode_block";

                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));

                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_count")
                        .withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_xray",
                        Component.translatable(modeKey) // pass the localized "vein" or "per block"
                ).withStyle(ChatFormatting.YELLOW));
                return;
            }
        }
    }

    public static void registerColorHandlerBlocks(RegisterColorHandlersEvent.Block event) {
        final BlockColor grassColor = (state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
        final BlockColor tallGrassColor = (state, level, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
        final BlockColor grassBlockColor = (state, level, pos, tintIndex) -> state.getValue(ConnectedGrassBlock.SNOWY) || tintIndex != 1 ? -1 : grassColor.getColor(state, level, pos, tintIndex);

        event.register(tallGrassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());
        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.CYCAD).get(),
                TFGBlocks_Earth.PLANTS.get(TFGPlant.CYCAD_PLANT).get(),
                TFGBlocks_Earth.PLANTS.get(TFGPlant.TANK_BROMELIAD).get());
        event.register(grassBlockColor,
                TFGBlocks_Earth.ALFISOL_GRASS.get(),
                TFGBlocks_Earth.ALFISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.MOLLISOL_GRASS.get(),
                TFGBlocks_Earth.MOLLISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.OXISOL_GRASS.get(),
                TFGBlocks_Earth.OXISOL_CLAY_GRASS.get(),
                TFGBlocks_Earth.PODZOL_GRASS.get(),
                TFGBlocks_Earth.PODZOL_CLAY_GRASS.get());
    }

    public static void registerColorHandlerItems(RegisterColorHandlersEvent.Item event) {
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);

        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());
    }
}
