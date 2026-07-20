package su.terrafirmagreg.tfcambiental.api;

import java.util.Optional;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import team.terrafirmagreg.jellies.common.entity.JellieBase;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

@FunctionalInterface
public interface EntityTemperatureProvider {
    Optional<TempModifier> getModifier(Player player);

    static Optional<TempModifier> getEntityTempModifier(Player player) {
        float change = 0F;

        for (Entity entity : player.level().getEntitiesOfClass(Entity.class,
                new AABB(player.blockPosition()).inflate(5.0D, 2.0D, 5.0D))) {
            if (entity.getType().is(TFCAmbiental.HOT_ENTITIES)) {
                change += 1F;
            } else if (entity.getType().is(TFCAmbiental.COLD_ENTITIES)) {
                change -= 1F;
            } else if (entity instanceof JellieBase jellie) {
                change += jellie.getAmbientalTemperature();
            }
        }

        if (change == 0F) {
            return Optional.empty();
        }

        return Optional.of(new TempModifier(change, 0F));
    }
}
