package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Mixin(PathfinderMob.class)
public class PathfinderMobMixin {

    /**
     * Don't try to stay close to a leash knot. The mob is tied to a fixed post and doesn't need to
     * navigate toward it.
     */
    @WrapOperation(method = "tickLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;shouldStayCloseToLeashHolder()Z"))
    private boolean tfg$noNavigationToLeashKnot(PathfinderMob self, Operation<Boolean> original) {
        if (self.getLeashHolder() instanceof LeashFenceKnotEntity) {
            return false;
        }
        return original.call(self);
    }
}
