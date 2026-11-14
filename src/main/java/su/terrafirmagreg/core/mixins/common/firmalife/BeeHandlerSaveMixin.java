package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.eerussianguy.firmalife.common.capabilities.bee.BeeHandler;


/**
 * Fixes bees not getting diseases. Mixin implementation of <a href="https://github.com/eerussianguy/firmalife/pull/297">this FirmaLife PR</a>
 * @author Zippity
 * @since 2026-11-13
 */
@Mixin(value = BeeHandler.class, remap = false)
public abstract class BeeHandlerSaveMixin {

    @Shadow
    private void save() {
    }

    @Inject(method = "setGeneticDisease", at = @At("RETURN"))
    private void tfg$saveGeneticDisease(int id, CallbackInfo ci) {
        save();
    }

    @Inject(method = "setParasiticInfection", at = @At("RETURN"))
    private void tfg$saveParasiticInfection(int id, CallbackInfo ci) {
        save();
    }
}
