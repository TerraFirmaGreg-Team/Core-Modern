package su.terrafirmagreg.core.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialHandler;
import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;
import su.terrafirmagreg.core.utils.TFGModsResolver;

public final class TFGCommonEventHandler {

    @SuppressWarnings("removal")
    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(TFGConfig::onLoad);
        bus.addListener(TFGCommonEventHandler::onCommonSetup);
        bus.addListener(TFGCommonEventHandler::onRegisterMaterialRegistry);
        bus.addListener(TFGCommonEventHandler::onPostRegisterMaterials);
        bus.addListener(TFGInteractionManager::init);
    }

    private static void onRegisterMaterialRegistry(final MaterialRegistryEvent event) {
        TFGCore.MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry(TFGCore.MOD_ID);
    }

    private static void onPostRegisterMaterials(final PostMaterialEvent event) {
        TFGHelpers.isMaterialRegistrationFinished = true;
        TFGMaterialHandler.postInit();
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (TFGConfig.enableTFCAmbientalCompat && TFGModsResolver.TFC_AMBIENTAL.isLoaded())
                TFCAmbientalCompat.register();
        });
    }
}
