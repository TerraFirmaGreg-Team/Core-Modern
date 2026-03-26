package su.terrafirmagreg.core.client;

import static earth.terrarium.adastra.client.forge.AdAstraClientForge.ITEM_RENDERERS;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.client.renderers.entities.vehicles.RocketRenderer;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.CommonProxy;
import su.terrafirmagreg.core.common.container.ArtisanTableScreen;
import su.terrafirmagreg.core.common.container.LargeNestBoxScreen;
import su.terrafirmagreg.core.common.data.*;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Casings;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Mars;
import su.terrafirmagreg.core.common.particle.*;
import su.terrafirmagreg.core.common.tfgt.machine.render.BouleRender;

public class ClientProxy extends CommonProxy {
    @SuppressWarnings("removal")
    public ClientProxy() {
        super();
        initializeDynamicRenders();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ForgeClientEventListener::registerColorHandlerBlocks);
        bus.addListener(ForgeClientEventListener::registerColorHandlerItems);
    }

    @SubscribeEvent
    public void registerParticles(@NotNull RegisterParticleProvidersEvent event) {
        // railgun animation
        event.registerSpriteSet(TFGParticles.RAILGUN_BOOM.get(), RailgunBoomProvider::new);
        event.registerSpriteSet(TFGParticles.RAILGUN_AMMO.get(), RailgunAmmoProvider::new);
        // prospector
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR.get(), OreProspectorProvider::new);
        event.registerSpriteSet(TFGParticles.ORE_PROSPECTOR_VEIN.get(), OreProspectorVeinProvider::new);
        // martian wind
        event.registerSpriteSet(TFGParticles.DARK_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xbe6621))); // avg color of red sand
        event.registerSpriteSet(TFGParticles.MEDIUM_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xc48456))); // avg color of ad astra mars sand
        event.registerSpriteSet(TFGParticles.LIGHT_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 0xcf9f59))); // avg color of ad astra venus sand
        // Other
        event.registerSpriteSet(TFGParticles.COOLING_STEAM.get(), CoolingSteamProvider::new);
        event.registerSpriteSet(TFGParticles.FISH_SCHOOL.get(), FishSchoolProvider::new);
        event.registerSpriteSet(TFGParticles.VOLCANO_SMOKE.get(), VolcanoSmokeProvider::new);
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
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Mars.MARS_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Mars.MARS_ICICLE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.DRY_ICE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Casings.REFLECTOR_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.SANDY_LOAM_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.SILTY_LOAM_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.SILT_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.LOAM_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.ALFISOL_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.ALFISOL_CLAY_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.ALFISOL_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.MOLLISOL_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.MOLLISOL_CLAY_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.MOLLISOL_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.OXISOL_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.OXISOL_CLAY_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.OXISOL_DUFF.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.PODZOL_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.PODZOL_CLAY_GRASS.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks_Earth.PODZOL_DUFF.get(), RenderType.cutoutMipped());
            TFGBlocks_Earth.PLANTS.forEach((plant, block) -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped()));

            // Fruit Trees.
            for (TFGFruitTree.FruitTreeType tree : TFGFruitTree.FruitTreeType.values()) {
                ItemBlockRenderTypes.setRenderLayer(TFGFruitTree.FRUIT_TREE_SAPLINGS.get(tree).get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(TFGFruitTree.FRUIT_TREE_POTTED_SAPLINGS.get(tree).get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(TFGFruitTree.FRUIT_TREE_LEAVES.get(tree).get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(TFGFruitTree.FRUIT_TREE_GROWING_BRANCHES.get(tree).get(), RenderType.cutout());
            }
        });
        onRegisterItemRenderers(ITEM_RENDERERS::put);
    }

    public void onRegisterItemRenderers(BiConsumer<Item, BlockEntityWithoutLevelRenderer> consumer) {
        consumer.accept(TFGItems.TIER_1_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE));
        consumer.accept(TFGItems.TIER_2_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_2_LAYER, RocketRenderer.TIER_2_TEXTURE));
        consumer.accept(TFGItems.TIER_3_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_3_LAYER, RocketRenderer.TIER_3_TEXTURE));
        consumer.accept(TFGItems.TIER_4_DOUBLE_ROCKET.get(), new RocketRenderer.ItemRenderer(RocketModel.TIER_4_LAYER, RocketRenderer.TIER_4_TEXTURE));
    }

    public void initializeDynamicRenders() {
        DynamicRenderManager.register(TFGCore.id("boule"), BouleRender.TYPE);
    }

    @SubscribeEvent
    public void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern"));
    }
}
