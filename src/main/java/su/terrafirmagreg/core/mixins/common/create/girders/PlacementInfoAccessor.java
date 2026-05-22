package su.terrafirmagreg.core.mixins.common.create.girders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackPlacement;

/***
 * Credit: Adapted from Create: More Girders
 */
@Mixin(value = TrackPlacement.PlacementInfo.class)
public interface PlacementInfoAccessor {
    @Accessor(value = "curve", remap = false)
    BezierConnection getCurve();
}
