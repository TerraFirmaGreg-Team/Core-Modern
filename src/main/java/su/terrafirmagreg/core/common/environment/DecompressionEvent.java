package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.ForcedPosePacket;

/**
 * Represents an active decompression event caused by a sealed room being breached.
 * Pulls entities toward the breach point from inside the room, and pushes them mildly away on the outside.
 * <p>
 * Created when a room transitions from sealed to escaped (build height or dimension limit).
 * Cancelled early if the room re-seals or the machine is removed.
 */
// TODO: Add particles
public class DecompressionEvent {

    @lombok.Getter
    private BlockPos breachPoint;
    @lombok.Getter
    private final RoomScan oldRoomScan;
    private final int durationTicks;
    private int elapsed;

    // Tuning constants
    private static final double TICKS_PER_BLOCK = 0.02;       // 1 second per 1000 blocks
    private static final int MIN_DURATION = 10;               // 0.5 seconds minimum
    private static final int MAX_DURATION = 600;              // 30 seconds cap
    private static final double BASE_FORCE = 0.4;             // force at distance = 1 block
    private static final double MIN_FORCE = 0.02;             // end force after this
    private static final double MAX_FORCE = 0.5;
    private static final double DAMAGE_DISTANCE = 2.0;        // damage within this range of breach
    private static final float DAMAGE_PER_TICK = 2.0f;        // a heart per tick at point-blank, usually you're half a wall block away
    private static final double FRICTION_DISTANCE = 5.0;      // dampen perpendicular velocity within this range
    private static final double PRONE_DISTANCE = 5;           // force prone within this range of breach
    private static final double PRONE_RELEASE_DISTANCE = 8;   // release prone when further than this
    private static final double EXTERIOR_RADIUS = 6.0;        // push entities outside the room within this range
    private static final double EXTERIOR_BASE_FORCE = 0.4;

    /** Players currently forced prone by this event. Cleared and unforced on finish. */
    private final Set<ServerPlayer> crawlingPlayers = new HashSet<>();

    /**
     * @param breachPoint The block where the room was breached
     * @param oldRoomScan The room scan from before the breach. Used to determine which entities are inside the old room.
     */
    public DecompressionEvent(ServerLevel level, BlockPos breachPoint, RoomScan oldRoomScan) {
        this.breachPoint = breachPoint;
        this.oldRoomScan = oldRoomScan;
        this.durationTicks = Mth.clamp(Mth.floor(oldRoomScan.interiorSize() * TICKS_PER_BLOCK), MIN_DURATION, MAX_DURATION);
        this.elapsed = 0;
        TFGNetworkHandler.sendDecompressionSoundStart(level, breachPoint, durationTicks);
    }

    /**
     * Called each server tick. Returns false when the event is over.
     */
    public boolean tick(ServerLevel level) {
        if (elapsed >= durationTicks)
            return false;

        // Quadratic decay
        double t = (double) elapsed / durationTicks;
        double timeScale = (1.0 - t) * (1.0 - t);
        if (timeScale < 0.01) {
            finish(level);
            return false;
        }

        Vec3 target = Vec3.atCenterOf(breachPoint);

        // Update crawling players: remove those who have exited the room or moved far away
        Iterator<ServerPlayer> it = crawlingPlayers.iterator();
        while (it.hasNext()) {
            ServerPlayer player = it.next();
            if (!entityInRoom(player, true) || player.position().distanceTo(target) > PRONE_RELEASE_DISTANCE) {
                it.remove();
                player.setForcedPose(null);
                TFGNetworkHandler.sendForcedPose(player, null);
                player.refreshDimensions();
            }
        }

        // Interior entities: pull toward breach
        AABB searchEntitiesBounds = oldRoomScan.bounds().inflate(1.0);
        for (Entity entity : level.getEntities((Entity) null, searchEntitiesBounds, this::shouldAffect)) {
            if (!entityInRoom(entity, false))
                continue;
            applyForce(entity, target, timeScale);
        }

        // Exterior entities: push away from breach by escaping air
        AABB exteriorBounds = AABB.ofSize(target, EXTERIOR_RADIUS * 2, EXTERIOR_RADIUS * 2, EXTERIOR_RADIUS * 2);
        for (Entity entity : level.getEntities((Entity) null, exteriorBounds, this::shouldAffect)) {
            if (entityInRoom(entity, true))
                continue;
            applyExteriorForce(entity, target, timeScale);
        }

        elapsed++;
        return true;
    }

    /** Check if any block position overlapping the entity's bounding box is in the room interior. */
    private boolean entityInRoom(Entity entity, boolean checkEnvelope) {
        AABB box = entity.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if ((checkEnvelope && oldRoomScan.containsEnvelope(pos.set(x, y, z)))
                            || oldRoomScan.containsInterior(pos.set(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyForce(Entity entity, Vec3 target, double timeScale) {
        Vec3 entityCenter = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        Vec3 offset = target.subtract(entityCenter);
        double distance = offset.length();

        // Don't apply force when really close to the breach to avoid singularities
        if (distance < 0.1)
            return;

        Vec3 direction = offset.normalize();

        // Linear falloff because it's more fun in gameplay than quadratic
        double force = BASE_FORCE / distance * timeScale;
        force = Mth.clamp(force, 0, MAX_FORCE);

        if (force < MIN_FORCE)
            return;

        // Near the breach, dampen velocity perpendicular to the force direction.
        // This stops oscillation around the hole when against the wall
        if (distance < FRICTION_DISTANCE) {
            Vec3 vel = entity.getDeltaMovement();
            double parallelSpeed = vel.dot(direction);
            Vec3 parallelVel = direction.scale(parallelSpeed);
            Vec3 perpVel = vel.subtract(parallelVel);
            double dampFactor = distance / FRICTION_DISTANCE;
            entity.setDeltaMovement(parallelVel.add(perpVel.scale(dampFactor)));
        }

        entity.push(direction.x * force, direction.y * force, direction.z * force);

        // Apply damage when close
        if (distance < DAMAGE_DISTANCE) {
            float damage = (float) (DAMAGE_PER_TICK * (force / MAX_FORCE) * (1.0 - distance / DAMAGE_DISTANCE));
            if (damage > 0.1f) {
                entity.hurt(entity.damageSources().generic(), damage);
            }
        }

        if (entity instanceof ServerPlayer player) {
            player.hurtMarked = true;

            // Force prone if close enough to fit through a 1-block gap
            if (distance < PRONE_DISTANCE && crawlingPlayers.add(player)) {

                // Clientside: Force pose, teleport up to keep eyes at same level, prevent camera lerping
                // Applied atomically on a camera tick to prevent jitter
                TFGNetworkHandler.sendForcedPose(player, Pose.SWIMMING);

                // Serverside: Force pose, teleport up, refresh dimension and position to new values
                // Doesn't use teleportTo because that breaks atomity by sending a separate packet.
                player.setForcedPose(Pose.SWIMMING);
                double newY = player.getY() + ForcedPosePacket.SWIMMING_EYE_SHIFT;
                player.absMoveTo(player.getX(), newY, player.getZ(), player.getYRot(), player.getXRot());
                entity.refreshDimensions();
                player.connection.resetPosition();
            }
        }
    }

    /** Force for entities outside the room being pushed away */
    private void applyExteriorForce(Entity entity, Vec3 breachCenter, double timeScale) {
        Vec3 entityCenter = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        Vec3 offset = entityCenter.subtract(breachCenter);
        double distance = offset.length();

        if (distance < 0.1 || distance > EXTERIOR_RADIUS)
            return;

        Vec3 direction = offset.normalize();
        double force = EXTERIOR_BASE_FORCE / distance * timeScale;
        force = Math.min(force, MAX_FORCE);

        if (force < MIN_FORCE)
            return;

        entity.push(direction.x * force, direction.y * force, direction.z * force);

        if (entity instanceof ServerPlayer player) {
            player.hurtMarked = true;
        }
    }

    private boolean shouldAffect(Entity entity) {
        // TODO: Tags for entities that shouldn't be affected (ad astra has rope knot?)
        // if (entity instanceof Player player && (player.isCreative() || player.isSpectator()))
        //     return false;
        return true;
    }

    /** Stop the sound, unforce all crawling players, and mark as expired. */
    private void finish(ServerLevel level) {
        elapsed = durationTicks;
        TFGNetworkHandler.sendDecompressionSoundStop(level, breachPoint);
        for (ServerPlayer player : crawlingPlayers) {
            player.setForcedPose(null);
            player.refreshDimensions();
            TFGNetworkHandler.sendForcedPose(player, null);
        }
        crawlingPlayers.clear();
    }

    /** Shift the breach point mid-event (e.g. original hole closed, new hole opened). */
    public void shiftBreachPoint(ServerLevel level, BlockPos newBreachPoint) {
        TFGNetworkHandler.sendDecompressionSoundShift(level, breachPoint, newBreachPoint, durationTicks, elapsed);
        this.breachPoint = newBreachPoint;
    }

    /** Cancel this decompression event early (e.g. room re-sealed, machine removed). */
    public void cancel(ServerLevel level) {
        finish(level);
    }
}
