package su.terrafirmagreg.core.utils;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.mixins.common.create.RotationPropagatorAccessor;

public final class CreateKineticsHelper {

    private CreateKineticsHelper() {
    }

    /**
     * Gets the rotation speed of a KineticBlockEntity on a specific face.
     * @param kbe The KineticBlockEntity to check.
     * @param direction The direction facing the machine (from the KBE's perspective).
     * @return The rotation speed for the given face.
     */
    public static float getActualSpeed(KineticBlockEntity kbe, Direction direction) {
        if (!isConnected(kbe, direction) || kbe.isOverStressed()) {
            return 0;
        }
        return kbe.getTheoreticalSpeed() * RotationPropagatorAccessor.callGetAxisModifier(kbe, direction);
    }

    /**
     * Checks if a KineticBlockEntity is connected to a machine on a specific face.
     * @param kbe The KineticBlockEntity to check.
     * @param direction The direction facing the machine (from the KBE's perspective).
     * @return True if connected.
     */
    public static boolean isConnected(KineticBlockEntity kbe, Direction direction) {
        if (kbe == null) {
            return false;
        }
        BlockState state = kbe.getBlockState();
        if (state.getBlock() instanceof IRotate rotate) {
            return rotate.hasShaftTowards(kbe.getLevel(), kbe.getBlockPos(), state, direction);
        }
        return false;
    }
}
