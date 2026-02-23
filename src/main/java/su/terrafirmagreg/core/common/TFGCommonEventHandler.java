package su.terrafirmagreg.core.common;

import static appeng.api.upgrades.Upgrades.add;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import de.mari_023.ae2wtlib.AE2wtlib;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggHandler;
import su.terrafirmagreg.core.compat.grappling_hook.GrapplehookCompat;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialHandler;
import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.FuelSyncPacket;
import su.terrafirmagreg.core.utils.TFGModsResolver;

public final class TFGCommonEventHandler {

    @SuppressWarnings("removal")
    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus otherBus = MinecraftForge.EVENT_BUS;

        otherBus.addGenericListener(ItemStack.class, TFGCommonEventHandler::attachItemCapabilities);
        otherBus.addListener(TFGCommonEventHandler::onPlayerLogin);

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

    private static void attachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!stack.isEmpty()) {
            if (stack.getItem() == TFGItems.SNIFFER_EGG.get() || stack.getItem() == TFGItems.WRAPTOR_EGG.get()) {
                event.addCapability(LargeEggCapability.KEY, new LargeEggHandler(stack));
            }
        }
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (TFGConfig.COMMON.ENABLE_TFC_AMBIENTAL_COMPAT.get() && TFGModsResolver.TFC_AMBIENTAL.isLoaded())
                TFCAmbientalCompat.register();
            if (TFGModsResolver.GRAPPLEMOD.isLoaded())
                GrapplehookCompat.init();
            addUpgrades(AEItems.WIRELESS_TERMINAL);
            addUpgrades(AEItems.WIRELESS_CRAFTING_TERMINAL);
            addUpgrades(AE2wtlib.PATTERN_ENCODING_TERMINAL);
            addUpgrades(AE2wtlib.PATTERN_ACCESS_TERMINAL);
            addUpgrades(AE2wtlib.UNIVERSAL_TERMINAL);
        });
    }

    private static void addUpgrades(ItemLike item) {
        add(TFGItems.WIRELESS_CARD.get(), item, 1, GuiText.WirelessTerminals.getTranslationKey());
    }

    /**
     * Send the blaze burner liquid fuel map to send to the client and populate emi.
     */
    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TFGNetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new FuelSyncPacket(FuelSyncPacket.capturedJsonData));
        }
    }
}
