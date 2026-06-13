package su.terrafirmagreg.core.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.renderer.SprayCanHudOverlay;
import su.terrafirmagreg.core.client.screen.ArtisanTableScreen;
import su.terrafirmagreg.core.client.screen.LargeNestBoxScreen;
import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.entity.astikorcarts.RNRPlowScreen;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TFGClientScreens {
    private TFGClientScreens() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(TFGContainers.RNR_PLOW_MENU.get(), RNRPlowScreen::new);
            MenuScreens.register(TFGContainers.LARGE_NEST_BOX.get(), LargeNestBoxScreen::new);
            MenuScreens.register(TFGContainers.ARTISAN_TABLE.get(), ArtisanTableScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("spray_can_info", SprayCanHudOverlay.HUD_SPRAY_CAN);
    }
}
