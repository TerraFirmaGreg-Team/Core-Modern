package su.terrafirmagreg.core.client;

import static earth.terrarium.adastra.client.forge.AdAstraClientForge.ITEM_RENDERERS;
import static su.terrafirmagreg.core.common.data.TFGEntities.*;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.wanmine.wab.entity.render.EntityRenderer;
import net.wanmine.wab.entity.render.model.SurferModel;
import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.client.renderers.entities.vehicles.RocketRenderer;

import su.terrafirmagreg.core.common.CommonProxy;
import su.terrafirmagreg.core.common.data.*;
import su.terrafirmagreg.core.common.data.container.ArtisanTableScreen;
import su.terrafirmagreg.core.common.data.container.LargeNestBoxScreen;
import su.terrafirmagreg.core.common.data.entities.astikorcarts.RNRPlowModel;
import su.terrafirmagreg.core.common.data.entities.astikorcarts.RNRPlowRenderer;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRam;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRamModel;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRamRenderer;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbit;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbitRenderer;
import su.terrafirmagreg.core.common.data.entities.rocket.RocketHelper;
import su.terrafirmagreg.core.common.data.entities.sniffer.*;
import su.terrafirmagreg.core.common.data.entities.surfer.TFCSurfer;
import su.terrafirmagreg.core.common.data.entities.wraptor.TFCWraptor;
import su.terrafirmagreg.core.common.data.entities.wraptor.TFCWraptorRenderer;
import su.terrafirmagreg.core.common.data.particles.*;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
    }

    @SubscribeEvent
    public void registerParticles(@NotNull RegisterParticleProvidersEvent event) {
        // railgun animation
        event.registerSpriteSet(TFGParticles.RAILGUN_BOOM.get(), RailgunBoomProvider::new);
        event.registerSpriteSet(TFGParticles.RAILGUN_AMMO.get(), RailgunAmmoProvider::new);
        // prospector
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR.get(), OreProspectorProvider::new);
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR_VEIN.get(), OreProspectorVeinProvider::new);
        event.registerSpriteSet(TFGParticles.COOLING_STEAM.get(), CoolingSteamProvider::new);
        // martian wind
        event.registerSpriteSet(TFGParticles.DARK_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xbe6621))); // avg color of red sand
        event.registerSpriteSet(TFGParticles.MEDIUM_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xc48456))); // avg color of ad astra mars sand
        event.registerSpriteSet(TFGParticles.LIGHT_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xcf9f59))); // avg color of ad astra venus sand
        // Other
        event.registerSpriteSet(TFGParticles.FISH_SCHOOL.get(), FishSchoolProvider::new);
    }

    @SuppressWarnings("removal")
    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            MenuScreens.register(TFGContainers.LARGE_NEST_BOX.get(), LargeNestBoxScreen::new);
            MenuScreens.register(TFGContainers.ARTISAN_TABLE.get(), ArtisanTableScreen::new);

            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.SULFUR_FUMES.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.SULFUR_FUMES.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.GEYSER_SLURRY.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.GEYSER_SLURRY.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.MARS_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.MARS_ICICLE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.DRY_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.REFLECTOR_BLOCK.get(), RenderType.translucent());
        });

        onRegisterItemRenderers(ITEM_RENDERERS::put);
    }

    public static void onRegisterItemRenderers(BiConsumer<Item, BlockEntityWithoutLevelRenderer> consumer) {
        consumer.accept(TFGItems.TIER_1_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE));
        consumer.accept(TFGItems.TIER_2_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_2_LAYER, RocketRenderer.TIER_2_TEXTURE));
        consumer.accept(TFGItems.TIER_3_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_3_LAYER, RocketRenderer.TIER_3_TEXTURE));
        consumer.accept(TFGItems.TIER_4_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_4_LAYER, RocketRenderer.TIER_4_TEXTURE));
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(MOON_RABBIT.get(), MoonRabbit.createAttributes().build());
        event.put(GLACIAN_RAM.get(), TFCGlacianRam.createMobAttributes().build());
        event.put(SNIFFER.get(), TFCSniffer.createMobAttributes().build());
        event.put(WRAPTOR.get(), TFCWraptor.createMobAttributes().build());
        event.put(SURFER.get(), TFCSurfer.getDefaultAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(
                MOON_RABBIT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MoonRabbit::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                GLACIAN_RAM.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TFCGlacianRam::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                SNIFFER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TFCSniffer::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                WRAPTOR.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TFCWraptor::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(
                SURFER.get(),
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.OCEAN_FLOOR,
                TFCSurfer::spawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MOON_RABBIT.get(), MoonRabbitRenderer::new);
        event.registerEntityRenderer(GLACIAN_RAM.get(), TFCGlacianRamRenderer::new);
        event.registerEntityRenderer(SNIFFER.get(), TFCSnifferRenderer::new);
        event.registerEntityRenderer(WRAPTOR.get(), TFCWraptorRenderer::new);

        event.registerEntityRenderer(SURFER.get(), EntityRenderer.create(SurferModel::new, 0.6F));
        event.registerEntityRenderer(RNR_PLOW.get(), RNRPlowRenderer::new);

        event.registerEntityRenderer(TIER_1_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT1);
        event.registerEntityRenderer(TIER_2_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT2);
        event.registerEntityRenderer(TIER_3_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT3);
        event.registerEntityRenderer(TIER_4_DOUBLE_ROCKET.get(), RocketHelper::makeRocketRendererT4);

    }

    @SubscribeEvent
    public static void onEntityLayerRegister(EntityRenderersEvent.RegisterLayerDefinitions event) {
        //RocketHelper.register(event);
        event.registerLayerDefinition(TFCGlacianRamModel.LAYER_LOCATION, TFCGlacianRamModel::createBodyLayer);
        event.registerLayerDefinition(RNRPlowModel.LAYER_LOCATION, RNRPlowModel::createLayer);
    }

    @SubscribeEvent
    private void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern"));
    }
}
