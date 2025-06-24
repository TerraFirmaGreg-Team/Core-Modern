package su.terrafirmagreg.core.mixins.client.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.compat.create.ChainGTMaterialInterface;

@Mixin(value = ChainConveyorRenderer.class)
public class ChainConveyorRendererMixin {
    @Unique private static ResourceLocation tfg$tempChainTextureResource = null;

    @Inject(method = "renderChains(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), remap = false)
    private void tfg$renderChains$HEAD(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci)
    {
        tfg$tempChainTextureResource = null;
    }

    @Inject(method = "renderChains(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;renderChain(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZ)V"), remap = false)
    private void tfg$renderChains(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos)
    {
        ChainGTMaterialInterface cgtbe = (ChainGTMaterialInterface) be;
        String matPath = cgtbe.getConnectionMaterial(blockPos).getResourceLocation().getPath();
        //TODO: Perhaps this could be adapted to use a white chain texture png, and simply modified colour-wise
        tfg$tempChainTextureResource = ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "textures/block/metal/chain/" + matPath + ".png");
    }

    @Inject(method = "renderChains(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("TAIL"), remap = false)
    private void tfg$renderChains$TAIL(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci)
    {
        tfg$tempChainTextureResource = null;
    }

    @ModifyArg(method = "renderChain(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static RenderType tfg$renderChain$getBuffer(RenderType pRenderType)
    {
        if(ResourceLocation.isValidResourceLocation(tfg$tempChainTextureResource.toString()))
            return RenderTypes.chain(tfg$tempChainTextureResource);
        else
            return pRenderType;
    }
}
