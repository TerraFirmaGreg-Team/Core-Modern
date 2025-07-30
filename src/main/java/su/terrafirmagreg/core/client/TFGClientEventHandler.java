package su.terrafirmagreg.core.client;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import su.terrafirmagreg.core.common.data.TFGFluids;

public final class TFGClientEventHandler {

    public static final ResourceLocation TFCMetalBlockTexturePattern = ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern");

    @SuppressWarnings("removal")
    public TFGClientEventHandler() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(TFGClientEventHandler::clientSetup);
        bus.addListener(TFGClientEventHandler::registerSpecialModels);

        bus.register(this);
    }

    public static void clientSetup(FMLClientSetupEvent evt)
    {
        evt.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getFlowing(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TFGFluids.MARS_WATER.getSource(), RenderType.translucent());
        });
    }

    private static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "block/metal/smooth_pattern"));
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event)
    {

    }
}
