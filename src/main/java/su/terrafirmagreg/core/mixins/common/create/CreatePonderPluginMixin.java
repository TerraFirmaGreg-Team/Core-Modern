package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.ponder.CreatePonderPlugin;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.compat.create.CustomCreatePonderTags;

@Mixin(value = CreatePonderPlugin.class, remap = false)
public abstract class CreatePonderPluginMixin implements PonderPlugin {
    @Inject(method = "registerTags", at = @At(value = "TAIL"))
    public void tfg$registerTags(PonderTagRegistrationHelper<ResourceLocation> helper, CallbackInfo ci) {
        CustomCreatePonderTags.register(helper);
    }

}
