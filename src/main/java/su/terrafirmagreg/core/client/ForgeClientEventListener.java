package su.terrafirmagreg.core.client;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.asphalt.AsphaltRoadColorHandlers;
import su.terrafirmagreg.core.common.data.TFGPlant;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
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

    public static void registerColorHandlerBlocks(RegisterColorHandlersEvent.Block event) {
        final BlockColor grassColor = (state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
        final BlockColor tallGrassColor = (state, level, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
        final BlockColor magmaColor = (blockState, blockAndTintGetter, blockPos, i) -> 0xFF5500;
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
        event.register(magmaColor, Blocks.MAGMA_BLOCK);
        TFCBlocks.MAGMA_BLOCKS.values().forEach(registryObject -> {
            event.register(magmaColor, registryObject.get());
        });

        AsphaltRoadColorHandlers.registerBlocks(event);
    }

    public static void registerColorHandlerItems(RegisterColorHandlersEvent.Item event) {
        final ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);

        event.register(grassColor,
                TFGBlocks_Earth.PLANTS.get(TFGPlant.RED_OAT_GRASS).get());

        AsphaltRoadColorHandlers.registerItems(event);
    }
}
