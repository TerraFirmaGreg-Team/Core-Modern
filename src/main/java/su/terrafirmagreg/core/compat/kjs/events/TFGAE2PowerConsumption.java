package su.terrafirmagreg.core.compat.kjs.events;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventJS;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import su.terrafirmagreg.core.TFGCore;

public class TFGAE2PowerConsumption extends EventJS {
    public static final Object2ObjectOpenHashMap<ResourceLocation, Double> powerConsumption = new Object2ObjectOpenHashMap<>();

    public void add(ResourceLocation level, double power) {
        if (powerConsumption.containsKey(level)) {
            TFGCore.LOGGER.warn("Power consumption for level {} already exists, overwriting!", level);
        }
        powerConsumption.put(level, power);
    }

    public void add(String level, double power) {
        var location = ResourceLocation.parse(level);
        if (powerConsumption.containsKey(location)) {
            TFGCore.LOGGER.warn("Power consumption for level {} already exists, overwriting!", location);
        }
        powerConsumption.put(location, power);
    }

    public void remove(ResourceLocation level) {
        if (!powerConsumption.containsKey(level)) {
            TFGCore.LOGGER.warn("Power consumption for level {} does not exist, skipping!", level);
            return;
        }
        powerConsumption.remove(level);
    }

}
