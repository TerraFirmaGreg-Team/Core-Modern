package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.world.biome.BiomeBuilder;
import net.dries007.tfc.world.biome.BiomeExtension;
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

public class TFGBiomes {

    // Dry Biomes
    public static final BiomeExtension MUD_FLATS = register("mud_flats",
            BiomeBuilder.builder().heightmap(TFGBiomeNoise::flats)
                    .surface(seed -> new FlatsSurfaceBuilder(true))
                    .aquiferHeightOffset(-16).spawnable().type(RiverBlendType.WIDE).noSandyRiverShores());
    public static final BiomeExtension SALT_FLATS = register("salt_flats",
            BiomeBuilder.builder().heightmap(TFGBiomeNoise::saltFlats)
                    .surface(seed -> new FlatsSurfaceBuilder(false))
                    .aquiferHeightOffset(-16).salty().spawnable().type(RiverBlendType.WIDE).noSandyRiverShores());
    public static final BiomeExtension DUNE_SEA = register("dune_sea",
            BiomeBuilder.builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                    .surface(DuneSurfaceBuilder::new)
                    .aquiferHeightOffset(-16).spawnable().type(RiverBlendType.WIDE));
    public static final BiomeExtension GRASSY_DUNES = register("grassy_dunes",
            BiomeBuilder.builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
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
