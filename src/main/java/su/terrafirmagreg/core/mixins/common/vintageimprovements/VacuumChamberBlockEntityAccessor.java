package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlockEntity;

@Mixin(value = VacuumChamberBlockEntity.class, remap = false)
public interface VacuumChamberBlockEntityAccessor {

    @Accessor("mode")
    boolean tfg$getMode();
}
