package su.terrafirmagreg.core.common.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Represents an active decompression event caused by a sealed room being breached.
 * Pulls entities toward the breach point with inverse-square force that decays over time.
 * <p>
 * Created when a room transitions from sealed to escaped (build height or dimension limit).
 * Cancelled early if the room re-seals or the machine is removed.
 */
public class DecompressionEvent {

    private final BlockPos breachPoint;
    private final RoomScan oldRoomScan;
    private final int durationTicks;
    private int elapsed;

    // Tuning constants
    private static final double TICKS_PER_BLOCK = 0.2;        // 1 tick of vortex per 5 blocks of room volume
    private static final int MIN_DURATION = 10;               // 0.5 seconds minimum
    private static final int MAX_DURATION = 600;              // 30 seconds cap
    private static final double BASE_FORCE = 0.4;             // force at distance = 1 block
    private static final double MIN_FORCE = 0.02;             // negligible below this
    private static final double MAX_FORCE = 0.5;              // cap so collision doesn't freak out
    private static final double DAMAGE_DISTANCE = 2.0;        // damage within this range of breach
    private static final float DAMAGE_PER_TICK = 1.0f;        // half a heart per tick at point blank

    /**
     * @param breachPoint The block where the room was breached
     * @param oldRoomScan The room scan from before the breach. Used to determine which entities are inside the old room.
     */
    public DecompressionEvent(BlockPos breachPoint, RoomScan oldRoomScan) {
        this.breachPoint = breachPoint;
        this.oldRoomScan = oldRoomScan;
        this.durationTicks = Mth.clamp(Mth.floor(oldRoomScan.interiorSize() * TICKS_PER_BLOCK), MIN_DURATION, MAX_DURATION);
        this.elapsed = 0;
    }

    /**
     * Called each server tick. Returns false when the event is over.
     */
    public boolean tick(ServerLevel level) {
        if (elapsed >= durationTicks)
            return false;

        // Quadratic decay: strong start, smooth fade
        double t = (double) elapsed / durationTicks;
        double timeScale = (1.0 - t) * (1.0 - t);
        if (timeScale < 0.01) {
            elapsed = durationTicks;
            return false;
        }

        Vec3 target = Vec3.atCenterOf(breachPoint);
        AABB bounds = oldRoomScan.bounds();

        // Expand bounds slightly so entities right at the edge are included
        AABB searchBounds = bounds.inflate(1.0);

        for (Entity entity : level.getEntities((Entity) null, searchBounds, this::shouldAffect)) {
            // Chunk pre-filter
            ChunkPos chunk = new ChunkPos(entity.getBlockX(), entity.getBlockZ());
            if (!oldRoomScan.touchedChunks().contains(chunk))
                continue;

            // Check if entity overlaps with the room interior.
            // Use the entity's bounding box instead of just feet position,
            // so tall entities partially inside the room are affected.
            if (!entityInRoom(entity))
                continue;

            applyForce(entity, target, timeScale);
        }

        elapsed++;
        return true;
    }

    /**
     * Check if any block position overlapping the entity's bounding box is in the room interior.
     */
    private boolean entityInRoom(Entity entity) {
        AABB box = entity.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (oldRoomScan.containsInterior(new BlockPos(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyForce(Entity entity, Vec3 target, double timeScale) {
        Vec3 offset = target.subtract(entity.position());
        double distance = offset.length();

        if (distance < 0.1)
            return;

        Vec3 direction = offset.normalize();

        // Inverse-square with distance, scaled by time decay
        double force = BASE_FORCE / (distance * distance) * timeScale;
        force = Mth.clamp(force, 0, MAX_FORCE);

        if (force < MIN_FORCE)
            return;

        entity.push(direction.x * force, direction.y * force, direction.z * force);

        // Damage entities very close to the breach
        if (distance < DAMAGE_DISTANCE) {
            float damage = (float) (DAMAGE_PER_TICK * timeScale * (1.0 - distance / DAMAGE_DISTANCE));
            if (damage > 0.1f) {
                entity.hurt(entity.damageSources().generic(), damage);
            }
        }

        if (entity instanceof ServerPlayer player) {
            player.hurtMarked = true;
        }
    }

    private boolean shouldAffect(Entity entity) {
        // TODO: Tags
        if (entity instanceof Player player &&
                (player.isCreative() || player.isSpectator()))
            return false;
        return true;
    }

    /**
     * Cancel this decompression event early (e.g. room re-sealed, machine removed).
     */
    public void cancel() {
        elapsed = durationTicks;
    }

    public boolean isExpired() {
        return elapsed >= durationTicks;
    }

    public BlockPos breachPoint() {
        return breachPoint;
    }
}
