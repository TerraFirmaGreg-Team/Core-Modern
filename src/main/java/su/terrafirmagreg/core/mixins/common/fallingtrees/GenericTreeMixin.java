package su.terrafirmagreg.core.mixins.common.fallingtrees;

import java.util.OptionalInt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(targets = "me.pandamods.fallingtrees.trees.GenericTree", remap = false)
public class GenericTreeMixin {
    @Redirect(method = "gatherLeavesAroundLog", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;orElse(I)I"), remap = false)
    private int tfg$useDistancePropertyForModdedLeaves(OptionalInt optionalInt, int defaultValue, @Local BlockState currentState) {
        // If vanilla method returned empty, check for TFC's DISTANCE_10 property
        if (optionalInt.isEmpty() && currentState.hasProperty(TFCBlockStateProperties.DISTANCE_10)) {
            return currentState.getValue(TFCBlockStateProperties.DISTANCE_10);
        }

        return optionalInt.orElse(defaultValue);
    }
}
