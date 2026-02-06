package su.terrafirmagreg.core.common.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Interface for machines that provide atmosphere.
 */
public interface IAtmosphereMachine {

    /**
     * @return Position of the machine
     */
    BlockPos getPos();

    /**
     * @return The level this provider is in
     */
    Level getLevel();

    /**
     * @return Whether the machine is currently active and providing atmosphere
     */
    boolean isWorking();
}
