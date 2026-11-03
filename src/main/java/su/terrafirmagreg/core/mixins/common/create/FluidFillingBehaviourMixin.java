package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.dries007.tfc.common.fluids.FluidPropertyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

// Mixins to help hose pulley filling correctly handle TFC fluid-logging
// (Sent to me by Allmoz from ATFC)

@Mixin(value = FluidFillingBehaviour.class)
public abstract class FluidFillingBehaviourMixin extends FluidManipulationBehaviour {

    public FluidFillingBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Shadow(remap = false)
    protected abstract BlockState updatePostWaterlogging(BlockState state);

    @Inject(method = "getAtPos", at = @At("RETURN"), cancellable = true, remap = false)
    private void tfg$isTFCFluidFillable(Level world, BlockPos pos, Fluid toFill, @Coerce CallbackInfoReturnable<Enum<?>> cir, @Local BlockState blockState, @Local FluidState fluidState) {
        if (FluidPropertyHelper.canFill(blockState, toFill)) {
            Enum<?> originalEnum = cir.getReturnValue();
            String value = "BLOCKING";
            Fluid fluid = fluidState.getType();
            if (toFill.isSame(fluid)) {
                value = "FILLED";
            } else if (fluid.isSame(Fluids.EMPTY)) {
                value = "FILLABLE";
            }
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> newEnum = Enum.valueOf((Class<Enum>) originalEnum.getDeclaringClass(), value);
            cir.setReturnValue(newEnum);
        }
    }

    @WrapOperation(method = "tryDeposit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasProperty(Lnet/minecraft/world/level/block/state/properties/Property;)Z", remap = true), remap = false)
    private boolean tfg$fillTFCFluidPartOne(BlockState instance, Property<Boolean> property, Operation<Boolean> original) {
        return original.call(instance, property) || FluidPropertyHelper.hasProperty(instance);
    }

    @WrapOperation(method = "tryDeposit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z", remap = true, ordinal = 1), remap = false)
    private boolean tfg$fillTFCFluidPartTwo(Fluid instance, Fluid pFluid, Operation<Boolean> original, @Local(name = "blockState") BlockState blockState) {
        if (FluidPropertyHelper.hasProperty(blockState)) {
            return FluidPropertyHelper.canContain(blockState, instance);
        }
        return original.call(instance, pFluid);
    }

    @WrapOperation(method = "tryDeposit", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;isVirtual()Z", ordinal = 0), remap = false)
    private boolean tfg$fillTFCFluidPartTree(SmartBlockEntity instance, Operation<Boolean> original, @Local(name = "world") Level world, @Local(name = "currentPos") BlockPos currentPos,
            @Local(name = "blockState") BlockState blockState,
            @Local(argsOnly = true) Fluid fluid) {
        if (original.call(instance)) {
            return true;
        } else if (FluidPropertyHelper.hasProperty(blockState)) {
            world.setBlock(
                    currentPos,
                    updatePostWaterlogging(FluidPropertyHelper.set(blockState, fluid)),
                    2 | 16);
            return true;
        }
        return false;
    }
}
