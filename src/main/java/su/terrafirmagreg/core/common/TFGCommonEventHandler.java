package su.terrafirmagreg.core.common;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggHandler;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialHandler;
import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;
import su.terrafirmagreg.core.utils.TFGModsResolver;

public final class TFGCommonEventHandler {

    @SuppressWarnings("removal")
    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus otherBus = MinecraftForge.EVENT_BUS;

        otherBus.addGenericListener(ItemStack.class, TFGCommonEventHandler::attachItemCapabilities);

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

    private static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event){
        ItemStack stack = event.getObject();
        if (!stack.isEmpty())
        {
            if (stack.getItem() == TFGItems.SNIFFER_EGG.get())
            {
                event.addCapability(LargeEggCapability.KEY, new LargeEggHandler(stack));
            }
        }
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (TFGConfig.enableTFCAmbientalCompat && TFGModsResolver.TFC_AMBIENTAL.isLoaded())
                TFCAmbientalCompat.register();
        });
    }
}
