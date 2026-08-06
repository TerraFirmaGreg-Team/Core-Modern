package su.terrafirmagreg.core.mixins.common.fowlplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.MobCategory;

import aqario.fowlplay.common.entity.forge.CustomMobCategoryImpl;

@Mixin(value = CustomMobCategoryImpl.class, remap = false)
public class CustomMobCategoryMixin {

    @Inject(method = "ambientBirds", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$ambientBirds(CallbackInfoReturnable<MobCategory> cir) {
        cir.setReturnValue(MobCategory.AMBIENT);
    }

    @Inject(method = "birds", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$birds(CallbackInfoReturnable<MobCategory> cir) {
        cir.setReturnValue(MobCategory.AMBIENT);
    }
}
