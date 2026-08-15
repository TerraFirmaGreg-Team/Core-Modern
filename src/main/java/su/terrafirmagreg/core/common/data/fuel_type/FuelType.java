/*
 * Originally from [Create Diesel Generators] (https://github.com/george8188625/Create-Diesel-Generators)
 * Licensed under the MIT license.
 */

package su.terrafirmagreg.core.common.data.fuel_type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;

public record FuelType(HolderSet<Fluid> fluid, int amountMb, int durationTicks) {

    // If gregtech consumes 64mB/t of steam for 1A of LV, and the steam engines consume 16mB/t for 32SU,
    // that's a 4x difference. To make things easier, the input mb and duration numbers should just be copied from
    // whatever gregtech's generators use, then we can do the 4x multiplier over here.
    public static final double MULTIPLIER = 0.25;

    public static final Codec<FuelType> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluid").forGetter(FuelType::fluid),
            Codec.INT.fieldOf("amount_mb").forGetter(FuelType::amountMb),
            Codec.INT.fieldOf("duration_ticks").forGetter(FuelType::durationTicks)).apply(i, FuelType::new));

    // Since the client doesn't have the tags when it joins a server and receives fuel types, this different codec is needed to not cause an error when joining a server.
    // This codec sends all the fluids, instead of sometimes sending just the tag.
    public static final Codec<FuelType> NCODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluid").forGetter(type -> HolderSet.direct(type.fluid.stream().toList())),
            Codec.INT.fieldOf("amount_mb").forGetter(FuelType::amountMb),
            Codec.INT.fieldOf("duration_ticks").forGetter(FuelType::durationTicks)).apply(i, FuelType::new));

    public static final FuelType EMPTY = new FuelType(null, 0, 0);

    public static FuelType getTypeFor(HolderLookup.RegistryLookup<FuelType> registry, Fluid fluid) {
        if (registry == null)
            return EMPTY;
        var type = registry.listElements()
                .filter(r -> r.get().fluid().contains(fluid.builtInRegistryHolder()))
                .findFirst();
        return type.isEmpty() ? EMPTY : type.get().get();
    }

    public double burnRate() {
        return MULTIPLIER * (amountMb / (double) durationTicks);
    }

    public double getFuelBurnRate(int targetRpm, int tier) {
        return Math.max(0.0001, burnRate() * Math.abs(targetRpm / 256.0) * (tier == 1 ? 1 : tier * 4));
    }
}
