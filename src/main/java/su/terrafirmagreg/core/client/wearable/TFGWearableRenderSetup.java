package su.terrafirmagreg.core.client.wearable;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TFGWearableRenderSetup {

    public static final ModelLayerLocation SNORKEL = layer("tfg_wearable_snorkel");
    public static final ModelLayerLocation FLIPPERS = layer("tfg_wearable_flippers");
    public static final ModelLayerLocation SNOWSHOES = layer("tfg_wearable_snowshoes");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(TFGCore.id(name), "main");
    }

    private TFGWearableRenderSetup() {
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                SNORKEL,
                () -> LayerDefinition.create(TFGWearableHeadModel.createSnorkel(), 64, 32));
        event.registerLayerDefinition(
                FLIPPERS,
                () -> LayerDefinition.create(TFGWearableLegsModel.createFlippers(), 64, 64));
        event.registerLayerDefinition(
                SNOWSHOES,
                () -> LayerDefinition.create(TFGWearableLegsModel.createSnowshoes(), 64, 64));
    }

    @SubscribeEvent
    public static void registerCuriosRenderers(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(TFGItems.SNORKEL.get(), TFGWearableCurioRenderer::snorkel);
            CuriosRendererRegistry.register(TFGItems.FLIPPERS.get(), TFGWearableCurioRenderer::flippers);
            CuriosRendererRegistry.register(TFGItems.SNOWSHOES.get(), TFGWearableCurioRenderer::snowshoes);
        });
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinName : event.getSkins()) {
            PlayerRenderer renderer = event.getPlayerSkin(skinName);
            if (renderer != null) {
                renderer.addLayer(new TFGWearableEquipmentLayer(renderer, event.getEntityModels()));
            }
        }
    }
}
