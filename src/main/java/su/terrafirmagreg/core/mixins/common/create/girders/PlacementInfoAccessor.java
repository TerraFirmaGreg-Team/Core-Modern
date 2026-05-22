package su.terrafirmagreg.core.mixins.common.create.girders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackPlacement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

@Mixin(value = TrackPlacement.PlacementInfo.class)
public interface PlacementInfoAccessor {
    @Accessor(value = "curve", remap = false)
    BezierConnection getCurve();

    @Accessor(value = "end1Extent", remap = false)
    int getEnd1Extent();

    @Accessor(value = "end2Extent", remap = false)
    int getEnd2Extent();

    @Accessor(value = "axis1", remap = false)
    Vec3 getAxis1();

    @Accessor(value = "axis2", remap = false)
    Vec3 getAxis2();

    @Accessor(value = "pos1", remap = false)
    BlockPos getPos1();

    @Accessor(value = "pos2", remap = false)
    BlockPos getPos2();
}
