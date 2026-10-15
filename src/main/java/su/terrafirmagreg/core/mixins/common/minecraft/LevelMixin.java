package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Shadow
    public abstract ResourceKey<Level> dimension();

	// Changes the maximum logical skylight brightness of the level during daytime on venus

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void tfg$getSkyDarken(CallbackInfoReturnable<Integer> cir) {
        if (this.dimension() == Planet.VENUS) {
            if (cir.getReturnValue() < 5) {
                cir.setReturnValue(5);
            }
        }
    }
}
