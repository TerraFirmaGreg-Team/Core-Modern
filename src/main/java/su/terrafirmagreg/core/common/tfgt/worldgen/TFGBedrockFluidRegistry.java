package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

public class TFGBedrockFluidRegistry {

    private static final Map<ResourceLocation, TFGBedrockFluidDefinition> DEFINITIONS = new HashMap<>();

    public static void addClimate(ResourceLocation id, ClimateWeightModifier modifier) {
        DEFINITIONS.computeIfAbsent(id, TFGBedrockFluidDefinition::new)
                .addClimateModifier(modifier);
    }

    public static TFGBedrockFluidDefinition get(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    public static Collection<TFGBedrockFluidDefinition> getAll() {
        return DEFINITIONS.values();
    }

    public static boolean isEmpty() {
        return DEFINITIONS.isEmpty();
    }

    public static void clear() {
        DEFINITIONS.clear();
    }
}
