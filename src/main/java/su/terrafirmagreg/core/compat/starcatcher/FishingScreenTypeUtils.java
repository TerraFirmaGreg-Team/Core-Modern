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

    private static final float CAVE_THRESHOLD = 50;
    private static final float WARM_THRESHOLD = 30;
    private static final float COLD_THRESHOLD = 0;

    /**
     * Determines the fishing screen type based on current environment.
     */
    public static FishingScreenType determineScreenType() {
        ClientLevel level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        assert level != null;
        assert player != null;
        final float temperature = Climate.getTemperature(level, player.blockPosition());

        ResourceKey<Level> dimension = level.dimension();
        double playerY = player.getY();
        Holder<Biome> biomeHolder = level.getBiome(player.blockPosition());

        // Beneath.
        if (dimension.equals(Level.NETHER)) {
            return FishingScreenType.NETHER;
        }

        // Mars.
        if (dimension.equals(MARS_DIMENSION)) {
            return FishingScreenType.MARS;
        }

        // Venus.
        if (dimension.equals(VENUS_DIMENSION)) {
            return FishingScreenType.VENUS;
        }

        // Earth.
        if (dimension.equals(Level.OVERWORLD)) {

            if (playerY < CAVE_THRESHOLD) {
                return FishingScreenType.CAVE;
            }

            if (biomeHolder.is(IS_OCEAN)) {
                // Only Ocean has a warm screen type since it just adds coral.
                if (temperature > WARM_THRESHOLD) {
                    return FishingScreenType.SURFACE_WARM;
                }
                if (temperature < COLD_THRESHOLD) {
                    return FishingScreenType.SURFACE_COLD;
                }
                return FishingScreenType.SURFACE;
            }

            if (!biomeHolder.is(IS_OCEAN)) {
                if (temperature < COLD_THRESHOLD) {
                    return FishingScreenType.SURFACE_COLD;
                }
                return FishingScreenType.SURFACE;
            }

        }
        // Default.
        return FishingScreenType.SURFACE;
    }
}
