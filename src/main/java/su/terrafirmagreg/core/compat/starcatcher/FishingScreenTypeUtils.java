package su.terrafirmagreg.core.compat.starcatcher;

import net.dries007.tfc.util.climate.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Utility class for determining the appropriate fishing screen type.
 */
public class FishingScreenTypeUtils {

    private static final ResourceKey<Level> MARS_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.parse("ad_astra:mars"));

    private static final ResourceKey<Level> VENUS_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.parse("ad_astra:venus"));

    private static final TagKey<Biome> IS_OCEAN = TagKey.create(
            net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("starcatcher", "is_ocean"));

    // Cave depth max Y threshold.
    private static final int CAVE_THRESHOLD = 50;

    /**
     * Determines the fishing screen type based on current environment.
     */
    public static FishingScreenType determineScreenType() {
        ClientLevel level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        final float temperature = Climate.getTemperature(level, player.blockPosition());

        ResourceKey<Level> dimension = level.dimension();
        double playerY = player.getY();
        Holder<Biome> biomeHolder = level.getBiome(player.blockPosition());

        if (dimension.equals(Level.NETHER)) {
            return FishingScreenType.NETHER;
        }

        if (dimension.equals(Level.END)) {
            return FishingScreenType.END;
        }

        if (dimension.equals(MARS_DIMENSION)) {
            return FishingScreenType.MARS;
        }

        if (dimension.equals(VENUS_DIMENSION)) {
            return FishingScreenType.VENUS;
        }

        if (dimension.equals(Level.OVERWORLD)) {

            if (playerY < CAVE_THRESHOLD) {
                return FishingScreenType.CAVE;
            }

            if (biomeHolder.is(IS_OCEAN)) {
                if (temperature > 30) {
                    return FishingScreenType.SURFACE_WARM;
                }
                if (temperature < 5) {
                    return FishingScreenType.SURFACE_COLD;
                }
                return FishingScreenType.SURFACE;
            }

            if (!biomeHolder.is(IS_OCEAN)) {
                if (temperature < 5) {
                    return FishingScreenType.SURFACE_COLD;
                }
                return FishingScreenType.SURFACE;
            }

        }
        return FishingScreenType.SURFACE;
    }
}
