package su.terrafirmagreg.core.common.block.asphalt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Client-side ambient particles for hot asphalt (pouring + cooling hot road).
 * Uses vanilla cosy campfire smoke — finer than {@code tfg:cooling_steam}.
 */
public final class AsphaltRoadHeatVisuals {

    private AsphaltRoadHeatVisuals() {
    }

    /** Heat hint slightly above the path surface (path collision tops out at y=15/16). */
    public static void spawnHotAsphaltAmbient(Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            return;
        }
        double baseY = pos.getY() + 0.94 + random.nextDouble() * 0.06;
        int puffs = random.nextInt(2);
        for (int i = 0; i < puffs; i++) {
            double x = pos.getX() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double rise = 0.05 + random.nextDouble() * 0.04;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, baseY, z, 0.0, rise, 0.0);
        }
    }
}
