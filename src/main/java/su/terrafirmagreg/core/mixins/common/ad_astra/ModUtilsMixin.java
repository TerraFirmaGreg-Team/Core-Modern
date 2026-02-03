package su.terrafirmagreg.core.mixins.common.ad_astra;

import static earth.terrarium.adastra.common.utils.ModUtils.teleportToDimension;

import java.util.List;
import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.common.container.VehicleContainer;
import earth.terrarium.adastra.common.entities.vehicles.Lander;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.registry.ModEntityTypes;
import earth.terrarium.adastra.common.utils.ModUtils;

@Mixin(value = ModUtils.class, remap = false)
public class ModUtilsMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void land(ServerPlayer pilotPlayer, ServerLevel targetLevel, Vec3 pos) {
        Entity vehicle = pilotPlayer.getVehicle();

        if (vehicle instanceof Rocket rocket) {
            List<Entity> passengers = vehicle.getPassengers();
            for (Entity entity : passengers) {
                if (Objects.isNull(entity))
                    continue;
                if (entity instanceof ServerPlayer player) {
                    player.stopRiding();
                    player.moveTo(pos);
                    Entity teleportedPlayer = teleportToDimension(player, targetLevel);
                    Lander lander = (Lander) ((EntityType<?>) ModEntityTypes.LANDER.get()).create(targetLevel);

                    if (Objects.isNull(lander))
                        continue;

                    lander.setPos(pos);
                    targetLevel.addFreshEntity(lander);
                    teleportedPlayer.startRiding(lander);

                    if (player != pilotPlayer)
                        continue;

                    VehicleContainer rocketInventory = rocket.inventory();
                    VehicleContainer landerInventory = lander.inventory();

                    for (int i = 0; i < rocketInventory.getContainerSize(); ++i) {
                        landerInventory.setItem(i + 1, rocketInventory.getItem(i));
                    }

                    landerInventory.setItem(0, rocket.getDropStack());
                }
            }

            if (rocket.isAlive()) {
                rocket.discard();
            }

        } else {
            pilotPlayer.stopRiding();
            pilotPlayer.moveTo(pos);
            teleportToDimension(pilotPlayer, targetLevel);
        }
    }
}
