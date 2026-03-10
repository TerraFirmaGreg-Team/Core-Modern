package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.entities.ai.FastGateBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@Mixin(value = FastGateBehavior.class, remap = false)
public class FastGateBehaviorMixin<E extends LivingEntity> {

    @Shadow
    private Behavior.Status status;

    /**
     * Don't start idle movement if a walk target is already set by a higher-priority behavior like breeding or following
     */
    @Inject(method = "tryStart", at = @At("HEAD"), cancellable = true, remap = true)
    private void tfg$guardWalkTarget(ServerLevel level, E entity, long gameTime, CallbackInfoReturnable<Boolean> cir) {
        if (entity.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * When an idle behavior starts, actually mark it as running otherwise it will be overwritten in the next tick.
     */
    @Inject(method = "tryStart", at = @At("TAIL"), remap = true)
    private void tfg$setRunningOnStart(ServerLevel level, E entity, long gameTime, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            status = Behavior.Status.RUNNING;
        }
    }
}
