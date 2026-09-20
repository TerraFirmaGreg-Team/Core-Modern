package su.terrafirmagreg.core.mixins.client.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.client.PoseSnapHelper;

@Mixin(Camera.class)
@OnlyIn(Dist.CLIENT)
public abstract class CameraMixin {

    @Shadow
    private float eyeHeight;
    @Shadow
    private float eyeHeightOld;
    @Shadow
    private Entity entity;

    /**
     * Applies a pending forced pose change atomically at the start of Camera.tick(), before any
     * frame renders. This prevents sub-tick jitter from pose, position, and eye height updating
     * across different render frames.
     * The yShift allows for teleporting the player upwards to put the new pose eyes at the same
     * height as the old pose eyes. Note that you need to update the server position and pose separately.
     * Releasing a forced pose (null) lets Camera.tick() run normally so the eye height lerps
     * back naturally.
     * Used by DecompressionEvent.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tfg$applyPendingPoseSnap(CallbackInfo ci) {
        PoseSnapHelper.PoseSnap snap = PoseSnapHelper.pendingSnap;
        if (snap == null || entity == null || !(entity instanceof Player player))
            return;

        PoseSnapHelper.pendingSnap = null;

        if (snap.pose() != null) {
            player.setForcedPose(snap.pose());
            player.setPose(snap.pose());
            player.refreshDimensions();
            if (snap.yShift() != 0f) {
                player.setPos(player.getX(), player.getY() + snap.yShift(), player.getZ());
                eyeHeightOld = eyeHeight = entity.getEyeHeight();
                entity.yo = entity.getY();
                ci.cancel();
            }
        } else {
            player.setForcedPose(null);
            // Let Camera.tick() run normally so the eye height lerps back up naturally
        }
    }
}
