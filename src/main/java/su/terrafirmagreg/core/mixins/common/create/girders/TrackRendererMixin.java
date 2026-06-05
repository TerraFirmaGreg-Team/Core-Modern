package su.terrafirmagreg.core.mixins.common.create.girders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import su.terrafirmagreg.core.common.block.girder.TFGGirderData;
import su.terrafirmagreg.core.common.data.TFGPartialModels;

/***
 * Credit: Create: More Girders
 */
@Mixin(value = TrackRenderer.class)
public class TrackRendererMixin {
    @Unique
    private static final ThreadLocal<Block> tfg$currentGirder = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<int[]> tfg$middleCallCounter = ThreadLocal.withInitial(() -> new int[] { 0 });

    @Inject(method = "renderGirder", at = @At("HEAD"), require = 0, remap = false)
    private static void tfg$captureGirder(Level level, BezierConnection bc, PoseStack ms,
            VertexConsumer vb, BlockPos tePosition, CallbackInfo ci) {
        tfg$currentGirder.set(((TFGGirderData) bc).tfg$getGirderBlock());
        tfg$middleCallCounter.get()[0] = 0;
    }

    @Inject(method = "renderGirder", at = @At("RETURN"), require = 0, remap = false)
    private static void tfg$clearGirder(Level level, BezierConnection bc, PoseStack ms,
            VertexConsumer vb, BlockPos tePosition, CallbackInfo ci) {
        tfg$currentGirder.remove();
        tfg$middleCallCounter.remove();
    }

    @Redirect(method = "renderGirder", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/render/CachedBuffers;partial(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;", remap = false), require = 0, remap = false)
    private static SuperByteBuffer tfg$swapRendererModel(PartialModel original, BlockState state) {
        Block girder = tfg$currentGirder.get();
        if (girder == null)
            return CachedBuffers.partial(original, state);

        if (original == AllPartialModels.GIRDER_SEGMENT_MIDDLE) {
            int callIndex = tfg$middleCallCounter.get()[0]++;
            boolean useAlt = (callIndex / 2) % 2 != 0;
            PartialModel model = useAlt
                    ? TFGPartialModels.getAltMiddleModel(girder)
                    : TFGPartialModels.getSegmentModel(girder, original);
            if (model != null)
                return CachedBuffers.partial(model, state);
        } else {
            PartialModel tfgModel = TFGPartialModels.getSegmentModel(girder, original);
            if (tfgModel != null)
                return CachedBuffers.partial(tfgModel, state);
        }

        return CachedBuffers.partial(original, state);
    }
}
