package su.terrafirmagreg.core.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.common.data.TFGParticles;

/**
 * Client-side looping sound and particle effect for decompression events.
 * Volume and pitch decay over the event's duration using quadratic falloff,
 * matching the force decay in {@link su.terrafirmagreg.core.common.environment.DecompressionEvent}.
 */
@OnlyIn(Dist.CLIENT)
public class ClientDecompressionEvent extends AbstractTickableSoundInstance {

    private static final Map<BlockPos, ClientDecompressionEvent> ACTIVE = new ConcurrentHashMap<>();

    // Tuning
    private static final double MAX_DISTANCE_SQ = 64.0 * 64.0;
    private static final int PARTICLES_PER_TICK = 15;
    private static final double SPAWN_RADIUS = 5.0;
    private static final double BASE_FORCE = 0.4;
    private static final double MAX_FORCE = 0.06;
    private static final double MIN_FORCE = 0.01;
    /** Particles further than this from the breach have a chance to be culled each tick. */
    private static final double FADE_START = 6.0;
    /** Over this additional distance the cull probability ramps to ~1. */
    private static final double FADE_BAND = 3.0;

    private BlockPos breachPos;
    private Vec3 breachCenter;
    private Vec3 outwardAxis;
    private Quaternionf rotation;
    private final int durationTicks;
    private int elapsed;

    private final List<Particle> particles = new ArrayList<>();
    private final RandomSource random = RandomSource.create();

    private ClientDecompressionEvent(BlockPos breachPos, int durationTicks, int elapsedOffset, Vec3 breachAxis) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.breachPos = breachPos;
        this.breachCenter = Vec3.atCenterOf(breachPos);
        this.durationTicks = durationTicks;
        this.elapsed = elapsedOffset;
        this.x = breachCenter.x;
        this.y = breachCenter.y;
        this.z = breachCenter.z;
        this.looping = true;
        this.delay = 0;
        this.volume = 2.0f;
        this.pitch = 1.4f;
        setBreachAxis(breachAxis);
    }

    private void setBreachAxis(Vec3 axis) {
        this.outwardAxis = axis;
        this.rotation = new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f((float) axis.x, (float) axis.y, (float) axis.z));
    }

    @Override
    public void tick() {
        elapsed++;
        if (elapsed >= durationTicks) {
            stopAndRemove();
            return;
        }

        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || player.position().distanceToSqr(x, y, z) > MAX_DISTANCE_SQ) {
            stopAndRemove();
            return;
        }

        double t = (double) elapsed / durationTicks;
        double timeScale = (1.0 - t) * (1.0 - t); // quadratic, matches server force decay
        if (timeScale < 0.01) {
            stopAndRemove();
            return;
        }

        this.volume = (float) (timeScale * 2.0);
        this.pitch = 1.4f + (float) (timeScale * 1f);

        ClientLevel level = mc.level;
        if (level != null) {
            spawnParticles(level, timeScale);
            tickParticles(timeScale);
        }
    }

    private void spawnParticles(ClientLevel level, double timeScale) {
        if (timeScale < 0.05)
            return;

        int count = Math.max(1, (int) Math.round(PARTICLES_PER_TICK * timeScale * timeScale));
        for (int i = 0; i < count; i++) {
            // Spawn position: interior half-sphere only (cy < 0 = inside the room), biased toward the breach.
            // Spherical coords: rho = radius, phi = polar angle from Y, tau = azimuthal angle in XZ.
            double rho = (SPAWN_RADIUS - 0.5) * random.nextDouble() + 0.5;
            //double phi = Math.acos(-random.nextDouble()); // [PI/2, PI]: interior half only
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double tau = random.nextDouble() * 2 * Math.PI;

            double sinPhi = Math.sin(phi);
            double cx = rho * sinPhi * Math.cos(tau);
            double cy = rho * Math.cos(phi);
            double cz = rho * sinPhi * Math.sin(tau);

            // Random starting velocity: cylindrical coords to give vTau a consistent direction
            double vRho = (random.nextDouble() * 2 - 1) * 0.05;
            double vTau = random.nextDouble() * -0.2;
            double vy = (random.nextDouble() * 2 - 1) * 0.05;
            double vx = vRho * Math.cos(tau) - vTau * Math.sin(tau);
            double vz = vRho * Math.sin(tau) + vTau * Math.cos(tau);

            // Rotate into world frame, Y+ becomes breachward from the inside
            Vector3f wPos = rotation.transform(new Vector3f((float) cx, (float) cy, (float) cz));
            Vector3f wVel = rotation.transform(new Vector3f((float) vx, (float) vy, (float) vz));

            Particle p = Minecraft.getInstance().particleEngine.createParticle(TFGParticles.DECOMPRESSION.get(),
                    breachCenter.x + wPos.x, breachCenter.y + wPos.y, breachCenter.z + wPos.z,
                    wVel.x, wVel.y, wVel.z);

            if (p != null)
                particles.add(p);
        }
    }

    private void tickParticles(double timeScale) {
        particles.removeIf(p -> {
            if (!p.isAlive())
                return true;

            Vec3 pos = p.getPos();
            Vec3 offset = pos.subtract(breachCenter);
            double dist = offset.length();

            if (dist > 0.3) {
                double force = Math.min(BASE_FORCE / dist * timeScale, MAX_FORCE);
                if (force >= MIN_FORCE) {
                    // Axial/radial force: interior particles sucked in, exterior pushed away.
                    double side = offset.dot(outwardAxis); // positive = exterior, negative = interior
                    double sign = side >= 0 ? 1.0 : -1.0;
                    Vec3 inoutDir = offset.scale(sign / dist);
                    p.xd += inoutDir.x * force;
                    p.yd += inoutDir.y * force;
                    p.zd += inoutDir.z * force;

                    // Tangential force: perpendicular to both outwardAxis and the radial direction.
                    //                    Vec3 radial = offset.subtract(outwardAxis.scale(offset.dot(outwardAxis)));
                    //                    double radialLen = radial.length();
                    //                    if (radialLen > 0.01) {
                    //                        Vec3 tangential = outwardAxis.cross(radial.scale(1.0 / radialLen));
                    //                        p.xd += tangential.x * force;
                    //                        p.yd += tangential.y * force;
                    //                        p.zd += tangential.z * force;
                    //                    }
                }
            }

            // Probabilistic fade beyond FADE_START
            if (dist > FADE_START) {
                double cullChance = (dist - FADE_START) / FADE_BAND;
                if (random.nextDouble() < cullChance) {
                    p.remove();
                    return true;
                }
            }

            // Kill particles inside solid blocks
            if (false && Minecraft.getInstance().level.getBlockState(BlockPos.containing(pos)).isSolid()) {
                p.remove();
                return true;
            }

            return false;
        });
    }

    private void stopAndRemove() {
        this.stop();
        particles.forEach(Particle::remove);
        particles.clear();
        ACTIVE.remove(breachPos);
    }

    public static void start(BlockPos pos, int durationTicks, Vec3 breachAxis) {
        stop(pos);
        var instance = new ClientDecompressionEvent(pos, durationTicks, 0, breachAxis);
        ACTIVE.put(pos, instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    public static void shift(BlockPos oldPos, BlockPos newPos, int durationTicks, int elapsedTicks, Vec3 breachAxis) {
        ClientDecompressionEvent instance = ACTIVE.remove(oldPos);
        if (instance == null) {
            if (!ACTIVE.containsKey(newPos)) {
                instance = new ClientDecompressionEvent(newPos, durationTicks, elapsedTicks, breachAxis);
                ACTIVE.put(newPos, instance);
                Minecraft.getInstance().getSoundManager().play(instance);
            }
            return;
        }
        instance.breachPos = newPos;
        instance.breachCenter = Vec3.atCenterOf(newPos);
        instance.x = instance.breachCenter.x;
        instance.y = instance.breachCenter.y;
        instance.z = instance.breachCenter.z;
        instance.setBreachAxis(breachAxis);
        ACTIVE.put(newPos, instance);
    }

    public static void stop(BlockPos pos) {
        ClientDecompressionEvent existing = ACTIVE.remove(pos);
        if (existing != null) {
            existing.particles.forEach(Particle::remove);
            existing.particles.clear();
            existing.stop();
        }
    }
}
