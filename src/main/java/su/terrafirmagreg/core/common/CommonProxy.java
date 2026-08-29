package su.terrafirmagreg.core.common;

import static appeng.api.upgrades.Upgrades.add;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;

import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import de.mari_023.ae2wtlib.AE2wtlib;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.*;
import su.terrafirmagreg.core.common.data.blocks.*;
import su.terrafirmagreg.core.common.data.fuel_type.FuelType;
import su.terrafirmagreg.core.common.data.items.TFGItems;
import su.terrafirmagreg.core.common.data.tfgt.TFGMachines;
import su.terrafirmagreg.core.common.data.tfgt.TFGMultiMachines;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeConditions;
import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeTypes;
import su.terrafirmagreg.core.common.entity.ai.TFGBrain;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;
import su.terrafirmagreg.core.common.tfgt.material.TFGMaterialHandler;
import su.terrafirmagreg.core.compat.ad_astra.AdAstraCompat;
import su.terrafirmagreg.core.compat.ae2.AE2Compat;
import su.terrafirmagreg.core.compat.create.CustomArmInteractionPointTypes;
import su.terrafirmagreg.core.compat.grappling_hook.GrapplehookCompat;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.utils.TFGHelpers;
import su.terrafirmagreg.core.utils.TFGModsResolver;
import su.terrafirmagreg.core.world.*;

public class CommonProxy {

    @SuppressWarnings("removal")
    public CommonProxy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        bus.addListener(TFGConfig::onLoad);

        TFGCore.REGISTRATE.registerEventListeners(bus);
        bus.addListener(CustomArmInteractionPointTypes::onRegister);

        TFGNetworkHandler.init();
        TFGBlocks.init();
        TFGBlockEntities.init();
        TFGPartialModels.init();
        TFGItems.init();
        TFGCreativeTab.init();
        TFGFeatures.FEATURES.register(bus);
        TFGEntities.init();
        TFGParticles.register(bus);
        TFGPlacements.PLACEMENT_MODIFIERS.register(bus);
        TFGFluids.FLUIDS.register(bus);
        TFGSurfaceRules.SURFACE_RULES.register(bus);
        TFGSurfaceConditions.SURFACE_CONDITIONS.register(bus);
        TFGContainers.CONTAINERS.register(bus);
        TFGEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(bus);
        TFGEffects.EFFECTS.register(bus);
        TFGRecipeTypes.RECIPE_TYPES.register(bus);
        TFGRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
        TFGEvents.register();
        TFGCarvers.CARVERS.register(bus);
        TFGStructureProcessors.STRUCTURE_PROCESSORS.register(bus);
        TFGLootConditions.LOOT_CONDITIONS.register(bus);

        TFGBrain.MEMORY_TYPES.register(bus);
        TFGBrain.SENSOR_TYPES.register(bus);
        TFGBrain.POI_TYPES.register(bus);

        TFGPoiTypes.TYPES.register(bus);

        TFGFoodTraits.init();

        bus.addGenericListener(SoundEntry.class, this::registerSounds);
        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
        bus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        bus.addGenericListener(RecipeConditionType.class, this::registerRecipeConditions);

        AdAstraCompat.RegisterEvents();
        AE2Compat.registerEvents();
        EnvironmentSystem.init();
    }

    @SubscribeEvent
    public void onRegisterMaterialRegistry(final MaterialRegistryEvent event) {
        TFGCore.MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry(TFGCore.MOD_ID);
    }

    @SubscribeEvent
    public void onPostRegisterMaterials(final PostMaterialEvent event) {
        TFGHelpers.isMaterialRegistrationFinished = true;
        TFGMaterialHandler.postInit();
        TFGModifyMaterials.modify();
    }

    @SubscribeEvent
    public void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (TFGModsResolver.GRAPPLEMOD.isLoaded())
                GrapplehookCompat.init();
            addUpgrades(AEItems.WIRELESS_TERMINAL);
            addUpgrades(AEItems.WIRELESS_CRAFTING_TERMINAL);
            addUpgrades(AE2wtlib.PATTERN_ENCODING_TERMINAL);
            addUpgrades(AE2wtlib.PATTERN_ACCESS_TERMINAL);
            addUpgrades(AE2wtlib.UNIVERSAL_TERMINAL);

            TFGBlockEntities.finaliseBEModification();
            TFGFluids.registerFluidInteractions();
            registerFlowerPots();
        });
    }

    @SubscribeEvent
    public void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                TFGRegistries.FUEL_TYPE,
                FuelType.CODEC,
                FuelType.NCODEC);
    }

    private void registerFlowerPots() {
        FlowerPotBlock emptyPot = (FlowerPotBlock) Blocks.FLOWER_POT;
        for (TFGFruitTree.FruitTreeType tree : TFGFruitTree.FruitTreeType.values()) {
            emptyPot.addPlant(TFGFruitTree.FRUIT_TREE_SAPLINGS.get(tree).getId(), TFGFruitTree.FRUIT_TREE_POTTED_SAPLINGS.get(tree));
        }
        for (PalmTrees tree : PalmTrees.values()) {
            emptyPot.addPlant(TFGBlocks_PalmTrees.PALM_SAPLINGS.get(tree).getId(), TFGBlocks_PalmTrees.POTTED_SAPLINGS.get(tree));
        }
    }

    private void addUpgrades(ItemLike item) {
        add(TFGItems.WIRELESS_CARD.get(), item, 1, GuiText.WirelessTerminals.getTranslationKey());
    }

    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        TFGMachines.init();
        TFGMultiMachines.init();
    }

    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        TFGTRecipeTypes.init();
    }

    public void registerRecipeConditions(GTCEuAPI.RegisterEvent<ResourceLocation, RecipeConditionType<?>> event) {
        TFGRecipeConditions.init();
    }

    public void registerSounds(GTCEuAPI.RegisterEvent<ResourceLocation, SoundEntry> event) {
        TFGSounds.init();
    }
}
