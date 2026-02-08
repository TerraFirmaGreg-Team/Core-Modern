package su.terrafirmagreg.core.world.new_ow_wg;

import static net.dries007.tfc.world.biome.BiomeBuilder.builder;

import net.dries007.tfc.world.biome.BiomeBlendType;
import net.dries007.tfc.world.biome.BiomeBuilder;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.river.RiverBlendType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg.TFCBiomesAccessor;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.DuneSurfaceBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.FlatsSurfaceBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.GrassyDunesSurfaceBuilder;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.ShoreAndOceanSurfaceBuilder;

public class TFGBiomes {

    // Aquatic biomes
    // BiomeNoise.ocean and BiomeNoise.oceanRidge are identical between 1.20 and 1.21

    // Ocean biome found near continents.
    public static final BiomeExtension OCEAN = register("ocean",
            builder().heightmap(seed -> BiomeNoise.ocean(seed, -26, -12))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24).salty().type(BiomeBlendType.OCEAN).noRivers());
    // Ocean biome with reefs depending on climate. Could be interpreted as either barrier, fringe, or platform reefs.
    public static final BiomeExtension OCEAN_REEF = register("ocean_reef",
            builder().heightmap(seed -> BiomeNoise.ocean(seed, -16, -8))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24).salty().type(BiomeBlendType.OCEAN).noRivers());
    // Deep ocean biome covering most all oceans.
    public static final BiomeExtension DEEP_OCEAN = register("deep_ocean",
            builder().heightmap(seed -> BiomeNoise.ocean(seed, -30, -16))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24).type(BiomeBlendType.OCEAN).salty().noRivers());
    // Deeper ocean with sharp relief carving to create very deep trenches
    public static final BiomeExtension DEEP_OCEAN_TRENCH = register("deep_ocean_trench",
            builder().heightmap(seed -> BiomeNoise.oceanRidge(seed, -30, -16))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24).type(BiomeBlendType.OCEAN).salty().noRivers());

    // Dry Biomes
    public static final BiomeExtension MUD_FLATS = register("mud_flats",
            builder().heightmap(TFGBiomeNoise::flats)
                    .surface(seed -> new FlatsSurfaceBuilder(true))
                    .aquiferHeightOffset(-16).spawnable().type(RiverBlendType.WIDE).noSandyRiverShores());
    public static final BiomeExtension SALT_FLATS = register("salt_flats",
            builder().heightmap(TFGBiomeNoise::saltFlats)
                    .surface(seed -> new FlatsSurfaceBuilder(false))
                    .aquiferHeightOffset(-16).salty().spawnable().type(RiverBlendType.WIDE).noSandyRiverShores());
    public static final BiomeExtension DUNE_SEA = register("dune_sea",
            builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                    .surface(DuneSurfaceBuilder::new)
                    .aquiferHeightOffset(-16).spawnable().type(RiverBlendType.WIDE));
    public static final BiomeExtension GRASSY_DUNES = register("grassy_dunes",
            builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                    .surface(GrassyDunesSurfaceBuilder::new)
                    .aquiferHeightOffset(-16).spawnable().type(RiverBlendType.WIDE));

    private static BiomeExtension register(String name, BiomeBuilder builder) {
        final ResourceLocation id = TFGCore.id("earth/" + name);
        final ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        final BiomeExtension variants = builder.build(key);

        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            TFCBiomesAccessor.getExtensions().put(key, variants);
        }

        return variants;
    }
}
