package su.terrafirmagreg.tfc_textile.event;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import su.terrafirmagreg.tfc_textile.item.TFCTextileItems;
import su.terrafirmagreg.tfcambiental.curios.ClothesCurioRenderer;

public class ClientEvent {

    public static void init(IEventBus bus) {
        bus.addListener(ClientEvent::setup);
    }

    private static void setup(FMLClientSetupEvent event) {

        CuriosRendererRegistry.register(TFCTextileItems.COTTON_HAT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COTTON_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COTTON_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COTTON_SOCKS.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.CROCODILE_HAT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.CROCODILE_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.CROCODILE_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.CROCODILE_BOOTS.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.LINEN_HAT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.LINEN_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.LINEN_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.LINEN_SOCKS.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.RAW_HAT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.RAW_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.RAW_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.RAW_SOCKS.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.CARIBOU_HAT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.CARIBOU_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.CARIBOU_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.CARIBOU_SOCKS.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.POLAR_HAT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.POLAR_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.POLAR_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.POLAR_SOCKS.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.TIGER_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.TIGER_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.TIGER_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.TIGER_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.PANTHER_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.PANTHER_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.PANTHER_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.PANTHER_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.SABERTOOTH_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.SABERTOOTH_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.SABERTOOTH_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.SABERTOOTH_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.COUGAR_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.BLACK_BEAR_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.BLACK_BEAR_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.BLACK_BEAR_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.BLACK_BEAR_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.GRIZZLY_BEAR_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.GRIZZLY_BEAR_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.GRIZZLY_BEAR_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.GRIZZLY_BEAR_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.DIREWOLF_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.DIREWOLF_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.DIREWOLF_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.DIREWOLF_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.LION_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.LION_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.LION_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.LION_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.PHANTOM_SILK_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.PHANTOM_SILK_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.PHANTOM_SILK_SOCKS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.PHANTOM_SILK_HAT.get(), ClothesCurioRenderer::new);

        CuriosRendererRegistry.register(TFCTextileItems.RED_ELK_PANTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.RED_ELK_SHIRT.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.RED_ELK_BOOTS.get(), ClothesCurioRenderer::new);
        CuriosRendererRegistry.register(TFCTextileItems.RED_ELK_HAT.get(), ClothesCurioRenderer::new);
    }

}
