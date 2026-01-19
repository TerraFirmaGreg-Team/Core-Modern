package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.events.ComputerEvent;
import com.simibubi.create.compat.computercraft.events.StationTrainPresenceEvent;
import com.simibubi.create.content.trains.station.StationBlockEntity;

/**
 * Prevents firing a DEPARTURE ComputerEvent when a train is disassembled, preventing a NPE to the disassembled train
 * @see <a href="https://github.com/Creators-of-Create/Create/issues/9720">Create issue #9720</a>
 */
@Mixin(value = StationBlockEntity.class, remap = false)
public class StationBlockEntityMixin {

    @SuppressWarnings("ConstantConditions") // event.train is decorated @NotNull so the IDE incorrectly believes it's never null
    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;prepareComputerEvent(Lcom/simibubi/create/compat/computercraft/events/ComputerEvent;)V", ordinal = 2))
    private boolean skipComputerEventIfTrainNull(AbstractComputerBehaviour instance, ComputerEvent event) {
        if (event instanceof StationTrainPresenceEvent stpe) {
            return stpe.train != null;
        }
        return true;
    }
}
