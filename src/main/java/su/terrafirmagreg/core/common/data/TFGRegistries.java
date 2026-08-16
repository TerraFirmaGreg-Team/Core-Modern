package su.terrafirmagreg.core.common.data;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.fuel_type.FuelType;

public class TFGRegistries {
    public static final ResourceKey<Registry<FuelType>> FUEL_TYPE = key("fuel_type");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(TFGCore.id(name));
    }
}
