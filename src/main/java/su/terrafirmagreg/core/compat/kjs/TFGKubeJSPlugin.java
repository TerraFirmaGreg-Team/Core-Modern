package su.terrafirmagreg.core.compat.kjs;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.api.systems.GravityApi;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.TemperatureApi;

import su.terrafirmagreg.core.common.TFGHelpers;
import su.terrafirmagreg.core.compat.gtceu.TFGPropertyKeys;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialFlags;
import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;
import su.terrafirmagreg.core.compat.kjs.events.TFGServerEvents;
import su.terrafirmagreg.core.compat.kjs.events.TFGStartupEvents;

public final class TFGKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {

        RegistryInfo.BLOCK.addType("tfg:decorative_plant", DecorativePlantBlockBuilder.class,
                DecorativePlantBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:tall_decorative_plant", TallDecorativePlantBlockBuilder.class,
                TallDecorativePlantBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:floating_decorative_plant", DecorativeFloatingPlantBlockBuilder.class,
                DecorativeFloatingPlantBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:attached_decorative_plant", DecorativeAttachedPlantBlockBuilder.class,
                DecorativeAttachedPlantBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:particle_emitter_decoration", ParticleEmitterDecorationBlockBuilder.class,
                ParticleEmitterDecorationBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:particle_emitter", ParticleEmitterBlockBuilder.class,
                ParticleEmitterBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:layer_block", LayerBlockBuilder.class, LayerBlockBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:active_cardinal", GTActiveCardinalBuilder.class, GTActiveCardinalBuilder::new);
        RegistryInfo.BLOCK.addType("tfg:active_particle_emitter", GTActiveParticleBuilder.class, GTActiveParticleBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("TFGTagPrefix", TFGTagPrefix.class);
        event.add("TFGPropertyKey", TFGPropertyKeys.class);
        event.add("TFGMaterialFlags", TFGMaterialFlags.class);
        event.add("TFGHelpers", TFGHelpers.class);

        event.add("ToolHelper", ToolHelper.class);
        event.add("MaterialFlags", MaterialFlags.class);
        event.add("ItemMaterialInfo", ItemMaterialInfo.class);
        event.add("MaterialStack", MaterialStack.class);

        event.add("AdAstraEvents", AdAstraEvents.class);
        event.add("OxygenAPI", OxygenApi.API);
        event.add("PlanetAPI", PlanetApi.API);
        event.add("TemperatureAPI", TemperatureApi.API);
        event.add("GravityAPI", GravityApi.API);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        TFGStartupEvents.GROUP.register();
        TFGServerEvents.GROUP.register();
    }

    @Override
    public void onServerReload() {
        TFGAE2PowerConsumption.powerConsumption.clear();
        TFGServerEvents.AE2_POWER_CONSUMPTION.post(new TFGAE2PowerConsumption());
    }
}
