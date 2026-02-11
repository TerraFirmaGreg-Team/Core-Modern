package su.terrafirmagreg.core.common;

import static appeng.api.upgrades.Upgrades.add;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import de.mari_023.ae2wtlib.AE2wtlib;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.*;
import su.terrafirmagreg.core.common.data.entities.ai.TFGBrain;
import su.terrafirmagreg.core.common.data.tfgt.TFGTItems;
import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;
import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeTypes;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMultiMachines;
import su.terrafirmagreg.core.compat.ad_astra.AdAstraCompat;
import su.terrafirmagreg.core.compat.ae2.AE2Compat;
import su.terrafirmagreg.core.compat.create.CustomArmInteractionPointTypes;
import su.terrafirmagreg.core.compat.grappling_hook.GrapplehookCompat;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialHandler;
import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.utils.TFGModsResolver;
import su.terrafirmagreg.core.world.TFGFeatures;
import su.terrafirmagreg.core.world.TFGSurfaceRules;

public class CommonProxy {

    @SuppressWarnings("removal")
    public CommonProxy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        bus.addListener(TFGConfig::onLoad);

        TFGCore.REGISTRATE.registerEventListeners(bus);
        bus.addListener(CustomArmInteractionPointTypes::onRegister);

        TFGNetworkHandler.init();
        TFGBlocks.BLOCKS.register(bus);
        TFGBlockEntities.BLOCK_ENTITIES.register(bus);
        TFGItems.ITEMS.register(bus);
        TFGCreativeTab.TABS.register(bus);
        TFGFeatures.FEATURES.register(bus);
        TFGEntities.ENTITIES.register(bus);
        TFGParticles.register(bus);
        TFGFluids.FLUIDS.register(bus);
        TFGSurfaceRules.SURFACE_RULES.register(bus);
        TFGContainers.CONTAINERS.register(bus);
        TFGEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(bus);
        TFGEffects.EFFECTS.register(bus);
        TFGRecipeTypes.RECIPE_TYPES.register(bus);
        TFGRecipeSerializers.RECIPE_SERIALIZERS.register(bus);

        TFGBrain.MEMORY_TYPES.register(bus);
        TFGBrain.SENSOR_TYPES.register(bus);
        TFGBrain.POI_TYPES.register(bus);

        TFGFoodTraits.init();
        TFGTItems.init();

        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
        bus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        bus.addGenericListener(GTItems.class, this::registerGTItems);
        bus.addGenericListener(RecipeConditionType.class, this::registerRecipeConditions);

        AdAstraCompat.RegisterEvents();
        AE2Compat.registerEvents();
    }

    @SubscribeEvent
    public static void onRegisterMaterialRegistry(final MaterialRegistryEvent event) {
        TFGCore.MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry(TFGCore.MOD_ID);
    }

    @SubscribeEvent
    public static void onPostRegisterMaterials(final PostMaterialEvent event) {
        TFGHelpers.isMaterialRegistrationFinished = true;
        TFGMaterialHandler.postInit();
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
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

    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        TFGMachines.init();
        TFGMultiMachines.init();
    }

    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        TFGTRecipeTypes.init();
    }

    public void registerGTItems(GTCEuAPI.RegisterEvent<ResourceLocation, GTItems> event) {
        TFGTItems.init();
    }

    public void registerRecipeConditions(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeConditionType<?>> event) {
        TFGTRecipeConditions.init();
    }
}
