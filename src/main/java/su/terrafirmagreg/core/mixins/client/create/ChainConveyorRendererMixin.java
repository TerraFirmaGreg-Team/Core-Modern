package su.terrafirmagreg.core.mixins.client.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import su.terrafirmagreg.core.compat.create.ChainGTMaterialInterface;

import java.util.Iterator;

@Mixin(value = ChainConveyorRenderer.class)
public class ChainConveyorRendererMixin {
    @Unique private static ResourceLocation tfg$tempChainTextureResource = null;

    @Inject(method = "renderChains(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), remap = false)
    private void tfg$renderChains$HEAD(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci)
    {
        tfg$tempChainTextureResource = null;
    }

    @Inject(method = "renderChains(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorRenderer;renderChain(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZ)V"), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tfg$renderChains(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci, float time, float animation, Iterator var8, BlockPos blockPos, ChainConveyorBlockEntity.ConnectionStats stats, Vec3 diff, double yaw, double pitch, Level level, BlockPos tilePos, Vec3 startOffset, PoseTransformStack chain, int light1, int light2, boolean far)
    {
        ChainGTMaterialInterface cgtbe = (ChainGTMaterialInterface) be;
        String matPath = cgtbe.getConnectionMaterial(blockPos).getResourceLocation().getPath();
        //TODO: Perhaps this could be adapted to use a white chain texture png, and simply modified colour-wise
        tfg$tempChainTextureResource = new ResourceLocation(TerraFirmaCraft.MOD_ID, "textures/block/metal/chain/" + matPath + ".png");
    }

    @Inject(method = "renderChains(Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("TAIL"), remap = false)
    private void tfg$renderChains$TAIL(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci)
    {
        tfg$tempChainTextureResource = null;
    }

    @ModifyArg(method = "renderChain(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), remap = false)
    private static RenderType tfg$renderChain$getBuffer(RenderType pRenderType)
    {
        if(ResourceLocation.isValidResourceLocation(tfg$tempChainTextureResource.toString()))
            return RenderTypes.chain(tfg$tempChainTextureResource);
        else
            return pRenderType;
    }
}
