package com.ninni.species;

import com.mojang.logging.LogUtils;
import com.ninni.species.client.ClientProxy;
import com.ninni.species.registry.*;
import com.ninni.species.registry.SpeciesParticles;
import com.ninni.species.server.events.ForgeEvents;
import com.ninni.species.server.events.ModEvents;
import com.ninni.species.server.world.poi.SpeciesPointOfInterestTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(Species.MOD_ID)
public class Species {
    public static final String MOD_ID = "species";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final List<Runnable> CALLBACKS = new ArrayList<>();
    public static CommonProxy PROXY;

    public Species() {

        // Fix for classloading issue
        // Defer assigning PROXY until class construction ~Mqrius
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PROXY = new ClientProxy();
        } else {
            PROXY = new CommonProxy();
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);

        SpeciesBlocks.BLOCKS.register(modEventBus);
        SpeciesBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        SpeciesBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        SpeciesCreativeModeTabs.CREATIVE_MODE_TABAS.register(modEventBus);
        SpeciesStatusEffects.MOB_EFFECTS.register(modEventBus);
        SpeciesEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        SpeciesEntities.ENTITY_TYPES.register(modEventBus);
        SpeciesFeatures.FEATURES.register(modEventBus);
        SpeciesItems.ITEMS.register(modEventBus);
        SpeciesPotions.POTIONS.register(modEventBus);
        SpeciesSoundEvents.SOUND_EVENTS.register(modEventBus);
        SpeciesStructureTypes.STRUCTURES.register(modEventBus);
        SpeciesStructurePieceTypes.STRUCTURE_PIECE_TYPES.register(modEventBus);
        SpeciesMemoryModuleTypes.MEMORY_MODULE_TYPES.register(modEventBus);
        SpeciesSensorTypes.SENSOR_TYPES.register(modEventBus);
        SpeciesParticles.PARTICLE_TYPES.register(modEventBus);
        SpeciesPointOfInterestTypes.POI_TYPES.register(modEventBus);
        SpeciesTreeDecorators.TREE_DECORATOR_TYPE.register(modEventBus);
        SpeciesBannerPatterns.BANNER_PATTERNS.register(modEventBus);
        SpeciesVillagerTypes.VILLAGER_TYPES.register(modEventBus);
        SpeciesEnchantments.ENCHANTMENTS.register(modEventBus);
        SpeciesPaintingVariants.PAINTING_VARIANTS.register(modEventBus);
        SpeciesRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        SpeciesMenus.MENUS.register(modEventBus);
        PROXY.init();
        eventBus.register(new ModEvents());
        eventBus.register(new ForgeEvents());
        eventBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> PROXY.commonSetup());
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.clientSetup());
    }

}
