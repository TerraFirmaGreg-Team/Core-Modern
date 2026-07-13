package su.terrafirmagreg.core.common.effect;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Mob effect kills the entity it's applied to when the duration ends.
 */
public class FinalMomentsEffect extends MobEffect {

    public FinalMomentsEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B0082);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.isAlive() && !entity.isInvulnerable()) {
            if (entity instanceof Player player && player.getAbilities().invulnerable) {
                return;
            }
            entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration == 1;
    }
}
