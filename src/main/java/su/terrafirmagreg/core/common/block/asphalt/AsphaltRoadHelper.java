package su.terrafirmagreg.core.common.block.asphalt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.data.TFGTags;

public final class AsphaltRoadHelper {

    public static final float DAMAGE_PER_TICK = 0.5F;
    public static final long THROTTLE_TICKS = 20L;

    private AsphaltRoadHelper() {
    }

    /**
     * Client-side ambient particles for hot asphalt (pouring + cooling hot road).
     */
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

    public static void tickBurn(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (level.getGameTime() % THROTTLE_TICKS != 0L) {
            return;
        }

        ItemStack feet = living.getItemBySlot(EquipmentSlot.FEET);
        if (feet.is(TFGTags.Items.HotProtectionEquipment)) {
            return;
        }

        DamageSource src = level.damageSources().hotFloor();
        if (living.isInvulnerableTo(src)) {
            return;
        }

        living.hurt(src, DAMAGE_PER_TICK);
    }
}
