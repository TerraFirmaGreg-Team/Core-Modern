package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

@Mixin(EndPodiumFeature.class)
public abstract class EndPodiumFeatureMixin extends Feature<NoneFeatureConfiguration> {

    public EndPodiumFeatureMixin(Codec<NoneFeatureConfiguration> pCodec) {
        super(pCodec);
    }

    @Inject(method = "place", at = @At(value = "HEAD"), cancellable = true)
    private void tfg$place(FeaturePlaceContext<NoneFeatureConfiguration> pContext,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
