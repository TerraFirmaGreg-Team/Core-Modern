package su.terrafirmagreg.core.client;

import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.contianer.LargeNestBoxScreen;
import su.terrafirmagreg.core.common.data.particles.ColoredWindParticleProvider;
import su.terrafirmagreg.core.common.data.particles.RailgunAmmoProvider;
import su.terrafirmagreg.core.common.data.particles.RailgunBoomProvider;

import javax.annotation.Nullable;
import java.util.List;


public final class TFGClientEventHandler {

    public static final ResourceLocation TFCMetalBlockTexturePattern =
            ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern");

    @SuppressWarnings("removal")
    public TFGClientEventHandler() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        bus.addListener(TFGClientEventHandler::clientSetup);
        bus.addListener(TFGClientEventHandler::registerSpecialModels);

        forgeBus.addListener(TFGClientEventHandler::onItemTooltip);

        bus.register(this);
    }

    @SubscribeEvent
    public void registerParticles(RegisterParticleProvidersEvent event) {
        // railgun animation
        event.registerSpriteSet(TFGParticles.RAILGUN_BOOM.get(), RailgunBoomProvider::new);
        event.registerSpriteSet(TFGParticles.RAILGUN_AMMO.get(), RailgunAmmoProvider::new);
        // martian wind
        event.registerSpriteSet(TFGParticles.DEEP_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 12477985))); // avg color of red sand
        event.registerSpriteSet(TFGParticles.MEDIUM_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 12878934))); // avg color of ad astra mars sand
        event.registerSpriteSet(TFGParticles.LIGHT_MARS_WIND.get(), (set) -> (new ColoredWindParticleProvider(set, 13606745))); // avg color of ad astra venus sand
    }

    @SuppressWarnings("removal")
    public static void clientSetup(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            MenuScreens.register(TFGContainers.LARGE_NEST_BOX.get(), LargeNestBoxScreen::new);

            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getSource(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGBlocks.REFLECTOR_BLOCK.get(), RenderType.translucent());
        });
    }

    private static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern"));
    }

    @SuppressWarnings("ConstantConditions")
    private static void onItemTooltip(ItemTooltipEvent event)
    {
        final ItemStack stack = event.getItemStack();
        final List<Component> text = event.getToolTip();
        if (!stack.isEmpty())
        {
            final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
            if (egg != null)
            {
                egg.addTooltipInfo(text);
            }
        }
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event)
    {

    }
}
