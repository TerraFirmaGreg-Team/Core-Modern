package su.terrafirmagreg.core.common.effect;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

/**
 * Mob effect kills the entity it's applied to when the duration ends.
 */
public class FinalMomentsEffect extends MobEffect {

    public FinalMomentsEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B0082);
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (!entity.level().isClientSide && entity.isAlive() && !entity.isInvulnerable()) {
            if (entity instanceof Player player) {
                if (player.getAbilities().invulnerable)
                    return;
            }
            entity.kill();
        }
    }
}
