package su.terrafirmagreg.core.client.renderer;

import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * A bunch of helper stuff for cloud rendering, like TFC wind, coloring them on other planets, etc.
 * Thanks @Redotix99 on the TFC server for help
 */

public class CloudRendererHelper {

    private static float velocityX, velocityZ;
    private static double x, z;

    public static final Vec3 earthCloudColor = new Vec3(1f, 1f, 1f);
    public static final Vec3 bottomVenusCloudColor = new Vec3(133f / 255f, 127f / 255f, 31f / 255f);
    public static final Vec3 middleVenusCloudColor = new Vec3(165f / 255f, 162f / 255f, 32f / 255f);
    public static final Vec3 topVenusCloudColor = new Vec3(190f / 255f, 191f / 255f, 48f / 255f);

    public static float currentYOffset;
    public static float currentSpeedMult;
    public static Vec3 currentCloudColor;

    public static void onClientTick() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused())
            return;

        // Get the wind this way instead of through the climate cache, because that method returns zero when you're below sea level
        Vec2 wind = Climate.getWindVector(mc.level, mc.player.blockPosition().atY(TFCChunkGenerator.SEA_LEVEL_Y));
        // Skip dimensions with no wind
        if (wind == Vec2.ZERO)
            return;

        // Get offset
        velocityX = wind.x;
        velocityZ = wind.y;

        // Add offset
        x += velocityX;
        z += velocityZ;
    }

    /** Smooth position between ticks, so movement doesn't stutter at high frame rates. */
    public static double offsetX(float partialTick) {
        return x + velocityX * partialTick;
    }

    public static double offsetZ(float partialTick) {
        return z + velocityZ * partialTick;
    }

    public static Vec3 tintClouds(Vec3 cloudColor, Vec3 cloudTint) {
        return new Vec3(cloudColor.x * cloudTint.x, cloudColor.y * cloudTint.y, cloudColor.z * cloudTint.z);
    }

    public static void render(float yOffset, float speedMult, Vec3 cloudColor, Runnable renderAction) {
        currentYOffset = yOffset;
        currentSpeedMult = speedMult;
        currentCloudColor = cloudColor;
        renderAction.run();
    }
}
