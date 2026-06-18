/*
package su.terrafirmagreg.core.compat.starcatcher;



 // Utility class for determining the appropriate fishing screen type.
public class FishingScreenTypeUtils {

    private static final ResourceKey<Level> MARS_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.parse("ad_astra:mars"));

    private static final ResourceKey<Level> VENUS_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.parse("ad_astra:venus"));

    private static final float CAVE_THRESHOLD = 50;
    private static final float COLD_1_THRESHOLD = 0;
    private static final float COLD_2_THRESHOLD = -8;
    private static final float COLD_3_THRESHOLD = -20;

     // Determines the fishing screen type based on current environment.
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

            if (temperature <= COLD_1_THRESHOLD && temperature > COLD_2_THRESHOLD) {
                return FishingScreenType.SURFACE_COLD_1;
            }
            if (temperature <= COLD_2_THRESHOLD && temperature > COLD_3_THRESHOLD) {
                return FishingScreenType.SURFACE_COLD_2;
            }
            if (temperature <= COLD_3_THRESHOLD) {
                return FishingScreenType.SURFACE_COLD_3;
            }

            return FishingScreenType.SURFACE;

        }
        // Default.
        return FishingScreenType.SURFACE;
    }
}
*/
