package su.terrafirmagreg.core.common.entity.projectile;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

/**
 * Interface for javelins that can be leashed.
 */
public interface ILeashedJavelin {
    boolean tfg$isLeashed();

    void tfg$setLeashed(@Nullable Player player);

    @Nullable
    Player tfg$getLeasher();

    void tfg$setRecalling(boolean recalling);

    boolean tfg$isRecalling();
}
