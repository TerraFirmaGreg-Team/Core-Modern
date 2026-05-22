package su.terrafirmagreg.core.mixins.common.create.girders;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.foundation.render.SpecialModels;

import net.createmod.catnip.data.Couple;
import net.minecraft.world.level.block.Block;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import su.terrafirmagreg.core.common.block.girder.TFGGirderData;
import su.terrafirmagreg.core.common.data.TFGPartialModels;

/***
 * Credit: Create: More Girders
 */
@Mixin(targets = "com.simibubi.create.content.trains.track.TrackVisual$BezierTrackVisual$GirderVisual")
public class GirderVisualMixin {
    @Shadow(remap = false)
    @Final
    private Couple<TransformedInstance[]> beams;

    @Shadow(remap = false)
    @Final
    private Couple<Couple<TransformedInstance[]>> beamCaps;

    @Unique
    private BezierConnection tfg$bc;

    @Unique
    private InstancerProvider tfg$provider;

    @Unique
    @SuppressWarnings("rawtypes")
    private InstanceType tfg$instanceType;

    @Redirect(method = "<init>(Lcom/simibubi/create/content/trains/track/TrackVisual$BezierTrackVisual;Lcom/simibubi/create/content/trains/track/BezierConnection;)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/track/BezierConnection;getSegmentCount()I", remap = false), require = 0, remap = false)
    private int tfg$captureSegmentCount(BezierConnection bc) {
        this.tfg$bc = bc;
        return bc.getSegmentCount();
    }

    @Redirect(method = "<init>(Lcom/simibubi/create/content/trains/track/TrackVisual$BezierTrackVisual;Lcom/simibubi/create/content/trains/track/BezierConnection;)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/render/SpecialModels;flatChunk(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;)Ldev/engine_room/flywheel/api/model/Model;", remap = false), require = 0, remap = false)
    private Model tfg$swapModel(PartialModel original) {
        if (tfg$bc != null) {
            Block girder = ((TFGGirderData) tfg$bc).tfg$getGirderBlock();
            if (girder != null) {
                PartialModel tfgModel = TFGPartialModels.getSegmentModel(girder, original);
                if (tfgModel != null)
                    return SpecialModels.flatChunk(tfgModel);
            }
        }
        return SpecialModels.flatChunk(original);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Redirect(method = "<init>(Lcom/simibubi/create/content/trains/track/TrackVisual$BezierTrackVisual;Lcom/simibubi/create/content/trains/track/BezierConnection;)V", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;", remap = false), require = 0, remap = false)
    private Instancer tfg$captureProvider(InstancerProvider provider, InstanceType type, Model model) {
        this.tfg$provider = provider;
        this.tfg$instanceType = type;
        return provider.instancer(type, model);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>(Lcom/simibubi/create/content/trains/track/TrackVisual$BezierTrackVisual;Lcom/simibubi/create/content/trains/track/BezierConnection;)V", at = @At("RETURN"), require = 0, remap = false)
    private void tfg$replaceBeamInstances(CallbackInfo ci) {
        if (tfg$bc == null || tfg$provider == null)
            return;

        Block girder = ((TFGGirderData) tfg$bc).tfg$getGirderBlock();
        if (girder == null)
            return;

        PartialModel middle = TFGPartialModels.getSegmentModel(girder, AllPartialModels.GIRDER_SEGMENT_MIDDLE);
        PartialModel alt = TFGPartialModels.getAltMiddleModel(girder);
        if (middle == null || alt == null)
            return;

        Instancer<TransformedInstance> middleInstancer = tfg$provider.instancer(
                tfg$instanceType, SpecialModels.flatChunk(middle));
        Instancer<TransformedInstance> altInstancer = tfg$provider.instancer(
                tfg$instanceType, SpecialModels.flatChunk(alt));

        beams.forEach(array -> {
            for (int i = 0; i < array.length; i++) {
                Instancer<TransformedInstance> inst = (i % 2 == 0) ? middleInstancer : altInstancer;
                array[i] = tfg$replaceInstance(array[i], inst);
            }
        });

        PartialModel top = TFGPartialModels.getSegmentModel(girder, AllPartialModels.GIRDER_SEGMENT_TOP);
        PartialModel bottom = TFGPartialModels.getSegmentModel(girder, AllPartialModels.GIRDER_SEGMENT_BOTTOM);
        if (top == null || bottom == null)
            return;

        Instancer<TransformedInstance> topInstancer = tfg$provider.instancer(
                tfg$instanceType, SpecialModels.flatChunk(top));
        Instancer<TransformedInstance> bottomInstancer = tfg$provider.instancer(
                tfg$instanceType, SpecialModels.flatChunk(bottom));

        beamCaps.forEachWithContext((couple, isTop) -> {
            Instancer<TransformedInstance> instancer = isTop ? topInstancer : bottomInstancer;
            couple.forEach(array -> {
                for (int i = 0; i < array.length; i++) {
                    array[i] = tfg$replaceInstance(array[i], instancer);
                }
            });
        });
    }

    @Unique
    private static TransformedInstance tfg$replaceInstance(TransformedInstance old, Instancer<TransformedInstance> instancer) {
        TransformedInstance replacement = instancer.createInstance();
        replacement.setTransform(old.pose);
        replacement.red = old.red;
        replacement.green = old.green;
        replacement.blue = old.blue;
        replacement.alpha = old.alpha;
        replacement.light = old.light;
        replacement.overlay = old.overlay;
        replacement.setChanged();
        old.delete();
        return replacement;
    }
}
