package su.terrafirmagreg.core.world.new_ow_wg;

import static net.dries007.tfc.world.biome.BiomeBuilder.builder;

import net.dries007.tfc.world.biome.BiomeBlendType;
import net.dries007.tfc.world.biome.BiomeBuilder;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.surface.builder.BadlandsSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.LowlandsSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg.TFCBiomesAccessor;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.*;

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

    // Low biomes
    // BiomeNoise.hills, BiomeNoise.lowlands, and BiomeNoise.canyons are the same

    // Very flat, slightly above sea level.
    public static final BiomeExtension PLAINS = register("plains",
            riverType(TFGRiverBlendType.FLOODPLAIN,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 4, 10))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Small hills, slightly above sea level.
    public static final BiomeExtension HILLS = register("hills",
            riverType(TFGRiverBlendType.FLOODPLAIN,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 16))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Flat, swamp-like, lots of shallow pools below sea level.
    public static final BiomeExtension LOWLANDS = register("lowlands",
            riverType(TFGRiverBlendType.BANKED,
                    builder().heightmap(BiomeNoise::lowlands)
                            .surface(LowlandsSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16).spawnable().noSandyRiverShores()));
    // Flat, swamp-like, lots of shallow pools below sea level.
    public static final BiomeExtension SALT_MARSH = register("salt_marsh",
            riverType(TFGRiverBlendType.BANKED,
                    builder().heightmap(BiomeNoise::lowlands)
                            .surface(LowlandsSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16).spawnable().salty().noSandyRiverShores()));
    // Sharp, small hills, with lots of water / snaking winding rivers.
    public static final BiomeExtension LOW_CANYONS = register("low_canyons",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> BiomeNoise.canyons(seed, -8, 21))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16).spawnable().noSandyRiverShores()));

    // Mid biomes

    // Higher hills, above sea level. Some larger / steeper hills.
    public static final BiomeExtension ROLLING_HILLS = register("rolling_hills",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 28))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Hills with sharp, exposed rocky areas.
    public static final BiomeExtension HIGHLANDS = register("highlands",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.sharpHills(seed, -3, 28))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Very high flat area with steep relief carving, similar to vanilla mesas.
    public static final BiomeExtension BADLANDS = register("badlands",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.badlands(seed, 22, 19.5f))
                            .surface(BadlandsSurfaceBuilder.NORMAL)
                            .spawnable()));
    // Very high area, very flat top.
    public static final BiomeExtension PLATEAU = register("plateau",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Very high area, very flat top.
    public static final BiomeExtension PLATEAU_WIDE = register("plateau_wide",
            riverType(TFGRiverBlendType.TALUS,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Medium height with snake like ridges, minor volcanic activity
    public static final BiomeExtension CANYONS = register("canyons",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> BiomeNoise.canyons(seed, -2, 40))
                            .surface(SimpleSurfaceBuilder.VOLCANIC_SOIL)
                            .volcanoes(6, 14, 30, 28)
                            .spawnable().noSandyRiverShores()));

    // High biomes

    // High, picturesque mountains. Pointed peaks, low valleys well above sea level.
    public static final BiomeExtension MOUNTAINS = register("mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
                            .surface(TFGNormalSurfaceBuilder.ROCKY).spawnable()));
    // Rounded top mountains, very large hills.
    public static final BiomeExtension OLD_MOUNTAINS = register("old_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 16, 40))
                            .surface(TFGNormalSurfaceBuilder.ROCKY).spawnable()));
    // Mountains with high areas, and low, below sea level valleys. Water is salt water here.
    public static final BiomeExtension OCEANIC_MOUNTAINS = register("oceanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
                            .surface(ShoreAndOceanSurfaceBuilder.MOUNTAINS)
                            .aquiferHeightOffset(-8).salty().spawnable()));
    // Volcanic mountains - slightly smaller, but with plentiful tall volcanoes
    public static final BiomeExtension VOLCANIC_MOUNTAINS = register("volcanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
                            .surface(SimpleSurfaceBuilder.ROCKY_VOLCANIC_SOIL)
                            .volcanoes(4, 25, 50, 40)));
    // Volcanic oceanic islands. Slightly smaller and lower but with very plentiful volcanoes
    public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAINS = register("volcanic_oceanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
                            .surface(ShoreAndOceanSurfaceBuilder.VOLCANIC_MOUNTAINS)
                            .aquiferHeightOffset(-8).salty().volcanoes(2, -12, 50, 20)));

    // Island Only
    // Mimic oceanic mountains
    public static final BiomeExtension GUANO_ISLAND = register("guano_island",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(TFGBiomeNoise::rockyIslands)
                            .surface(ShoreAndOceanSurfaceBuilder.ROCKY_SHORE)
                            .spawnable().noSandyRiverShores().salty()));

    // Shores
    // Each shore type is paired with a secondary shore type, which is sometimes applied
    // Standard shore / beach. Material will vary based on location
    public static final BiomeExtension SHORE = register("shore",
            shoreType(TFGRiverBlendType.WIDE, ShoreBlendType.SANDY, -4,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.SANDY)
                            .aquiferHeightOffset(-16).type(BiomeBlendType.LAND).salty().shore()
                            .noRivers().noSandyRiverShores()));
    public static final BiomeExtension TIDAL_FLATS = register("tidal_flats",
            shoreType(TFGRiverBlendType.WIDE, ShoreBlendType.SANDY, -4,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.SANDY)
                            .aquiferHeightOffset(-16).type(BiomeBlendType.OCEAN).salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Inspired by Bay of Fundy, 12 Apostles, etc. -- High biome shore
    public static final BiomeExtension SEA_STACKS = register("sea_stacks",
            shoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.SEA_STACKS, -6,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 10, 30))
                            .surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
                            .aquiferHeightOffset(-40).type(BiomeBlendType.LAND).salty().shore()
                            .noRivers().noSandyRiverShores()));
    // TODO: more kinds of shores

    // Water
    public static final BiomeExtension LAKE = register("lake",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(BiomeNoise::lake)
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16).type(BiomeBlendType.LAKE).noRivers()));
    public static final BiomeExtension RIVER = register("river",
            builder().surface(TFGRiverSurfaceBuilder.INSTANCE));

    // Lakes
    // BiomeNoise.mountains and BiomeNoise.undergroundLakes are unchanged
    public static final BiomeExtension MOUNTAIN_LAKE = register("mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
                    .surface(NormalSurfaceBuilder.ROCKY)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE).noRivers());
    public static final BiomeExtension OLD_MOUNTAIN_LAKE = register("old_mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
                    .surface(NormalSurfaceBuilder.ROCKY)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE).noRivers());
    public static final BiomeExtension OCEANIC_MOUNTAIN_LAKE = register("oceanic_mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
                    .surface(ShoreAndOceanSurfaceBuilder.MOUNTAINS)
                    .carving(BiomeNoise::undergroundLakes)
                    .salty().type(BiomeBlendType.LAKE).noRivers());
    public static final BiomeExtension VOLCANIC_MOUNTAIN_LAKE = register("volcanic_mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
                    .surface(SimpleSurfaceBuilder.ROCKY_VOLCANIC_SOIL)
                    .volcanoes(4, 25, 50, 40)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE).noRivers());
    public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register("volcanic_oceanic_mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
                    .surface(ShoreAndOceanSurfaceBuilder.VOLCANIC_MOUNTAINS)
                    .volcanoes(2, -12, 50, 20)
                    .carving(BiomeNoise::undergroundLakes)
                    .salty().type(BiomeBlendType.LAKE).noRivers());
    public static final BiomeExtension PLATEAU_LAKE = register("plateau_lake",
            builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                    .surface(NormalSurfaceBuilder.INSTANCE)
                    .carving(BiomeNoise::undergroundLakes).type(BiomeBlendType.LAKE).noRivers());

    // Dry Biomes
    public static final BiomeExtension MUD_FLATS = register("mud_flats",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(TFGBiomeNoise::flats)
                            .surface(seed -> new FlatsSurfaceBuilder(true))
                            .aquiferHeightOffset(-16).spawnable().noSandyRiverShores()));
    public static final BiomeExtension SALT_FLATS = register("salt_flats",
            riverType(TFGRiverBlendType.WIDE, builder().heightmap(TFGBiomeNoise::saltFlats)
                    .surface(seed -> new FlatsSurfaceBuilder(false))
                    .aquiferHeightOffset(-16).salty().spawnable().noSandyRiverShores()));
    public static final BiomeExtension DUNE_SEA = register("dune_sea",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                            .surface(DuneSurfaceBuilder::new)
                            .aquiferHeightOffset(-16).spawnable()));
    public static final BiomeExtension GRASSY_DUNES = register("grassy_dunes",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                            .surface(GrassyDunesSurfaceBuilder::new)
                            .aquiferHeightOffset(-16).spawnable()));

    private static BiomeBuilder riverType(TFGRiverBlendType river, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        return ib.tfg$type(river);
    }

    private static BiomeBuilder shoreType(TFGRiverBlendType river, ShoreBlendType shore, int shoreHeight, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        ib = (IBiomeBuilder) ib.tfg$type(river);
        ib = (IBiomeBuilder) ib.tfg$type(shore);
        return ib.tfg$setShoreBaseHeight(shoreHeight);
    }

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
