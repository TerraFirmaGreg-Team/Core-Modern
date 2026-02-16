package su.terrafirmagreg.core.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Interface for machines that provide environmental control (oxygen, temperature, etc.)
 */
public interface IEnvironmentMachine {

    BlockPos getPos();

    Level getLevel();

    /**
     * @return Whether the machine is currently active and providing its environment effect
     */
    boolean isWorking();
}
