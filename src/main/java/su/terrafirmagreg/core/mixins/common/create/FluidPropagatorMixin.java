package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FluidPropagator;

import net.dries007.tfc.common.fluids.FluidPropertyHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@Mixin(value = FluidPropagator.class, remap = false)
public class FluidPropagatorMixin {

    @WrapOperation(method = "isOpenEnd", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasProperty(Lnet/minecraft/world/level/block/state/properties/Property;)Z", remap = true))
    private static boolean tfg$isTFCOpenEnded(BlockState state, Property<?> property, Operation<Boolean> original) {
        return original.call(state, property) || FluidPropertyHelper.canOpenPipe(state);
    }
}
