package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cake.struts.compat.flywheel.StrutBakedModel;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.ChunkRenderTypeSet;

@Mixin(value = StrutBakedModel.class, remap = false)
public class StrutBakeModelMixin {
    @Mutable
    @Final
    @Shadow
    private static ChunkRenderTypeSet RENDER_TYPES;

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void tfg$changeRenderTypes(CallbackInfo ci) {
        RENDER_TYPES = ChunkRenderTypeSet.of(new RenderType[] { RenderType.cutout() });
    }
}
