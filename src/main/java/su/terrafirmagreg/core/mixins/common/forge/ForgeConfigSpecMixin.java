package su.terrafirmagreg.core.mixins.common.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.CheckForNull;

/// This can be removed if TFC hot or not fixes its issues
@Mixin(ForgeConfigSpec.ConfigValue.class)
public class ForgeConfigSpecMixin {
    @Redirect(method = "get", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Preconditions;checkState(ZLjava/lang/Object;)V"), remap = false)
    public void tfg$cancelError(boolean expression, @CheckForNull Object errorMessage) {}
}
