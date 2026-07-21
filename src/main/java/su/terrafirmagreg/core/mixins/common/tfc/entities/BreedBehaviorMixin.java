package su.terrafirmagreg.core.mixins.common.tfc.entities;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.dries007.tfc.common.entities.ai.livestock.BreedBehavior;

@Mixin(value = BreedBehavior.class, remap = false)
public class BreedBehaviorMixin {

    /**
     * Allow the spawn check to pass up to 10 ticks early to account for brain tick throttling.
     * Normally the spawning happens on the last valid tick of the BreedBehavior before timeout,
     * but with the throttle we might miss this moment.
     */
    // spotless:off
    @ModifyExpressionValue(
            method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;J)V",
            at = @At(value = "FIELD", target = "Lnet/dries007/tfc/common/entities/ai/livestock/BreedBehavior;spawnChildAtTime:J", opcode = Opcodes.GETFIELD))
    // spotless:on
    private long tfg$allowEarlierSpawn(long spawnChildAtTime) {
        return spawnChildAtTime - 10;
    }
}
