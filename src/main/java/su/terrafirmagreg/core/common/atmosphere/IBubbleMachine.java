package su.terrafirmagreg.core.common.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for machines that provide atmosphere in a spherical bubble.
 * No flood fill required - just a simple radius check.
 * Used for Gravity Normalizers, Temperature Regulators, etc.
 */
public interface IBubbleMachine extends IAtmosphereMachine {

    /**
     * @return Radius of the bubble in blocks
     */
    double getRadius();

    /**
     * Checks if a position is within this provider's bubble.
     *
     * @param pos Position to check
     * @return true if the position is within the bubble
     */
    default boolean containsPosition(BlockPos pos) {
        if (!isWorking()) {
            return false;
        }

        BlockPos center = getPosition();
        double radius = getRadius();

        // Use squared distance for efficiency
        double dx = pos.getX() - center.getX();
        double dy = pos.getY() - center.getY();
        double dz = pos.getZ() - center.getZ();

        return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
    }
}
