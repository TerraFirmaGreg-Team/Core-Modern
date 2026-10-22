package su.terrafirmagreg.core.mixins.common.opposing_force;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.unusualmodding.opposing_force.entity.utils.EliteVariant;

@Mixin(value = EliteVariant.class, remap = false)
public interface EliteVariantMixin {

    /**
     * @author Pyritie
     * @reason Inject doesn't work inside interfaces
     * Bump the elite spawn chance from 1/50 to 1/20
     */
    @Overwrite
    default int getEliteSpawnChance() {
        return 20;
    }
}
