package su.terrafirmagreg.core.common.environment;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * Interface for machines that provide environmental control (oxygen, temperature, etc.)
 */
public interface IEnvironmentMachine {

    BlockPos getPos();

    Level getEnvironmentLevel();

    /**
     * @return Whether the machine is currently active and providing its environment effect
     */
    boolean isWorking();

    interface IAtmosphereMachine extends IEnvironmentMachine {

        @Nullable
        Fluid getAtmosphere();

        float getPressure();
    }
}
