package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.Direction;

/**
 * Accessor for {@link RotationPropagator}.
 */
@Mixin(value = RotationPropagator.class, remap = false)
public interface RotationPropagatorAccessor {

    @Invoker("getAxisModifier")
    static float callGetAxisModifier(KineticBlockEntity be, Direction direction) {
        throw new UnsupportedOperationException();
    }
}
