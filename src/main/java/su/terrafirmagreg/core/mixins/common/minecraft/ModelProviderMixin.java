package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelProvider;

import su.terrafirmagreg.core.TFGCore;

// This mixin makes it so minecraft will put models in the right folder.
// Typically, minecraft will put the model for a block 'tfg:grass/mars_path' in 'models/grass' instead of 'models/block/grass', this mixin changes that for TFG stuff.
@Mixin(ModelProvider.class)
public class ModelProviderMixin {
    @Final
    @Shadow
    protected String folder;

    @Inject(method = "extendWithFolder", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$extendWithFolder(ResourceLocation rl, CallbackInfoReturnable<ResourceLocation> cir) {
        if (rl.getNamespace().equals("tfg") && !rl.getPath().startsWith("item") && !rl.getPath().startsWith("block"))
            cir.setReturnValue(TFGCore.id(folder + "/" + rl.getPath()));
    }
}
