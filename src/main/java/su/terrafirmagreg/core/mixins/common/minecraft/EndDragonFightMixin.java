package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.level.dimension.end.EndDragonFight;

@Mixin(value = EndDragonFight.class)
public abstract class EndDragonFightMixin {

    @Redirect(method = "setDragonKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;spawnExitPortal(Z)V"))
    private void tfg$setDragonKilled$spawnExitPortal(EndDragonFight instance, boolean pActive) {
    }

    @Redirect(method = "setDragonKilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;spawnNewGateway()V"))
    private void spawnNewGateway(EndDragonFight instance) {
    }
}
