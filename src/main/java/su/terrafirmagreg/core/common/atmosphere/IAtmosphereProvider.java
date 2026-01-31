package su.terrafirmagreg.core.common.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for machines that provide atmosphere via flood fill (sealed rooms).
 * Implemented by Oxygen Distributors and similar machines.
 */
public interface IAtmosphereProvider {

    /**
     * @return Position of the machine
     */
    BlockPos getPosition();

    /**
     * @return The level this provider is in
     */
    Level getLevel();

    /**
     * @return Whether the machine is currently active and providing atmosphere
     */
    boolean isActive();

    /**
     * @return Maximum room size this machine can handle (in blocks)
     */
    int getMaxRoomSize();

    /**
     * @return Maximum horizontal dimension for the room
     */
    default int getMaxHorizontalDimension() {
        return 128;
    }

    /**
     * Called when the room state changes (sealed/unsealed/size change).
     *
     * @param room The current room state (may be null if removed)
     */
    default void onRoomStateChanged(AtmosphereRoom room) {
    }

    /**
     * Called when the room becomes unsealed (breach detected).
     * Machines can use this to spawn vortex effects, etc.
     *
     * @param room The room that was breached
     */
    default void onRoomBreached(AtmosphereRoom room) {
    }
}
