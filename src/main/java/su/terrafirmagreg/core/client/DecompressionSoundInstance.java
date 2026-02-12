package su.terrafirmagreg.core.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side looping sound for decompression events.
 * Volume and pitch decay over the event's duration using quadratic falloff,
 * matching the force decay in {@link su.terrafirmagreg.core.common.environment.DecompressionEvent}.
 */
@OnlyIn(Dist.CLIENT)
public class DecompressionSoundInstance extends AbstractTickableSoundInstance {

    private static final Map<BlockPos, DecompressionSoundInstance> ACTIVE = new HashMap<>();

    private final BlockPos breachPos;
    private final int durationTicks;
    private int elapsed;

    private DecompressionSoundInstance(BlockPos breachPos, int durationTicks) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.breachPos = breachPos;
        this.durationTicks = durationTicks;
        this.elapsed = 0;
        this.x = breachPos.getX() + 0.5;
        this.y = breachPos.getY() + 0.5;
        this.z = breachPos.getZ() + 0.5;
        this.looping = true;
        this.delay = 0;
        this.volume = 2.0f;
        this.pitch = 1.4f;
    }

    private static final double MAX_DISTANCE_SQ = 64.0 * 64.0;

    @Override
    public void tick() {
        elapsed++;
        if (elapsed >= durationTicks) {
            stopAndRemove();
            return;
        }

        // Stop if player is too far or no longer in a level
        var player = Minecraft.getInstance().player;
        if (player == null || player.position().distanceToSqr(x, y, z) > MAX_DISTANCE_SQ) {
            stopAndRemove();
            return;
        }

        double t = (double) elapsed / durationTicks;
        double timeScale = (1.0 - t);
        if (timeScale < 0.001) {
            stopAndRemove();
            return;
        }

        this.volume = (float) (timeScale * 2.0);
        this.pitch = 1.4f + (float) (timeScale * 1f);
    }

    private void stopAndRemove() {
        this.stop();
        ACTIVE.remove(breachPos);
    }

    public static void start(BlockPos pos, int durationTicks) {
        // Stop existing sound at this position if any
        stop(pos);

        DecompressionSoundInstance instance = new DecompressionSoundInstance(pos, durationTicks);
        ACTIVE.put(pos, instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    public static void stop(BlockPos pos) {
        DecompressionSoundInstance existing = ACTIVE.remove(pos);
        if (existing != null) {
            existing.stop();
        }
    }
}
