package su.terrafirmagreg.core.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Signals the camera mixin to apply a forced pose change atomically on the next Camera.tick(),
 * before any frame renders.
 */
@OnlyIn(Dist.CLIENT)
public final class PoseSnapHelper {

    public static PoseSnap pendingSnap = null;

    /** @param pose null to release the forced pose. */
    public record PoseSnap(@Nullable Pose pose, float yShift) {
    }

    private PoseSnapHelper() {
    }
}
