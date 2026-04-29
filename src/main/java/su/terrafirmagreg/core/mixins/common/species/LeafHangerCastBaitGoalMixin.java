package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.ninni.species.server.entity.mob.update_3.LeafHanger;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(value = LeafHanger.CastBaitGoal.class, remap = false)
public class LeafHangerCastBaitGoalMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/world/level/material/Fluid;)Z"), remap = true)
    private boolean tfg$tick(FluidState instance, Fluid fluid) {
        return instance.is(FluidTags.WATER);
    }
}
