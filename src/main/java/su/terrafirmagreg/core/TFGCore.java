package su.terrafirmagreg.core;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.terrafirmagreg.core.client.TFGClientEventHandler;
import su.terrafirmagreg.core.common.*;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGCreativeTab;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMultiMachines;
import su.terrafirmagreg.core.compat.ad_astra.AdAstraCompat;
import su.terrafirmagreg.core.world.TFGFeatures;

@Mod(TFGCore.MOD_ID)
public final class TFGCore {

    public static final String MOD_ID = "tfg";
    public static final String NAME = "TerraFirmaGreg-Core";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(TFGCore.MOD_ID);
    public static MaterialRegistry MATERIAL_REGISTRY;

    @SuppressWarnings("removal")
    public TFGCore() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TFGConfig.SPEC);

        TFGCommonEventHandler.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            new TFGClientEventHandler();
        }

        setupFixForGlobalServerConfig();

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        TFGBlocks.BLOCKS.register(bus);
        TFGBlockEntities.BLOCK_ENTITIES.register(bus);
        TFGItems.ITEMS.register(bus);
        TFGCreativeTab.TABS.register(bus);
        TFGFeatures.FEATURES.register(bus);

        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
        bus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);

        AdAstraCompat.RegisterEvents();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    @SuppressWarnings("removal")
    private static void setupFixForGlobalServerConfig() {
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true)
        );
    }

    @SubscribeEvent
    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        TFGMachines.init();
        TFGMultiMachines.init();
    }

    @SubscribeEvent
    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        TFGRecipeTypes.init();
    }
}
