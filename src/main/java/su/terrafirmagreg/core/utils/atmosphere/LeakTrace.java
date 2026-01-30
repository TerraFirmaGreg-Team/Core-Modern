package su.terrafirmagreg.core.utils.atmosphere;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class LeakTrace {

    private static final List<ActiveTrace> ACTIVE = new ArrayList<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        ACTIVE.removeIf(ActiveTrace::tick);
    }

    public static void spawn(ServerLevel level, List<BlockPos> path) {
        ACTIVE.add(new ActiveTrace(level, path));
    }

    private static class ActiveTrace {
        private final ServerLevel level;
        private final List<BlockPos> path;
        private int index = 0;

        private static final int SEGMENTS_PER_TICK = 1;

        ActiveTrace(ServerLevel level, List<BlockPos> path) {
            this.level = level;
            this.path = path;
        }

        boolean tick() {
            int spawned = 0;

            while (spawned < SEGMENTS_PER_TICK && index < path.size() - 1) {
                spawnSegment(level, path, index++);
                spawned++;
            }

            return index >= path.size() - 3;
        }
    }

    private static void spawnSegment(ServerLevel level, List<BlockPos> path, int i) {
        for (int j = i; j < i + 3; j++) {
            BlockPos a = path.get(j);
            BlockPos b = path.get(j + 1);

            Vec3 from = Vec3.atCenterOf(a);
            Vec3 to = Vec3.atCenterOf(b);
            Vec3 dir = to.subtract(from).normalize();

            level.sendParticles(
                    ParticleTypes.CLOUD,
                    from.x, from.y, from.z,
                    3,
                    dir.x * 0.15,
                    dir.y * 0.15,
                    dir.z * 0.15,
                    0);
        }
    }
}
