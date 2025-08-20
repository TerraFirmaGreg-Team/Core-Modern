package su.terrafirmagreg.core.mixins.common.grappling_hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yyon.grapplinghook.controllers.GrappleController;
import com.yyon.grapplinghook.utils.Vec;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GrappleController.class, remap = false)
public class GrappleControllerMixin {
    @Shadow
    public Vec motion;

    @Shadow
    public Entity entity;

    @WrapOperation(method = "doubleJump", at = @At(value = "INVOKE", target = "Lcom/yyon/grapplinghook/utils/Vec;setMotion(Lnet/minecraft/world/entity/Entity;)V"))
    private void tfg$resetFallDistanceOnDoubleJump(Vec instance, Entity e, Operation<Void> original) {
        this.motion.setMotion(e);
        e.resetFallDistance();
    }
}
