package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.fluids.forge.GTFluidImpl;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.fluids.FluidPropertyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@Mixin(value = FluidDrainingBehaviour.class, remap = false)
public class FluidDrainingBehaviourMixin {

    @Shadow(remap = false)
    Fluid fluid;

    // Fixes the hose pulley collecting in-world GT fluids as separate "flowing" and "source" fluids.
    // This converts them all into "source" ones

    @ModifyReturnValue(method = "getDrainableFluid", at = @At("RETURN"), remap = false)
    private FluidStack tfg$getDrainableFluid(FluidStack original) {
        if (original.getFluid() instanceof FlowingFluid flowingFluid
                && flowingFluid.getFlowing() instanceof GTFluidImpl
                && flowingFluid.getBucket() instanceof BucketItem bucket) {

            return new FluidStack(bucket.getFluid(), original.getAmount());
        }

        return original;
    }

    // Fixes the hose pulley not being able to drain TFC fluid-logged blocks
    // (Sent to me by Allmoz from ATFC)

    @Inject(method = "canPullFluidsFrom", at = @At("RETURN"), cancellable = true, remap = false)
    private void tfg$isTFCFluidDrainable(BlockState blockState, BlockPos pos, @Coerce CallbackInfoReturnable<Enum<?>> cir) {
        if (FluidPropertyHelper.canDrain(blockState)) {
            Enum<?> originalEnum = cir.getReturnValue();
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Enum<?> newEnum = Enum.valueOf((Class<Enum>) originalEnum.getDeclaringClass(), "SOURCE");
            cir.setReturnValue(newEnum);
        }
    }

    @WrapOperation(method = "pullNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z", remap = true), remap = false)
    private boolean tfg$drainTFCFluid(Fluid instance, Fluid pFluid, Operation<Boolean> original, @Local(ordinal = 0) BlockState blockState, @Local(ordinal = 1) LocalRef<BlockState> emptied,
            @Local LocalRef<Fluid> fluid) {
        Fluid logged = FluidPropertyHelper.getFluid(blockState).orElse(Fluids.EMPTY);
        if (!logged.isSame(Fluids.EMPTY) && !blockState.is(TFCTags.Blocks.PLANTS)) {
            fluid.set(logged);
            emptied.set(FluidPropertyHelper.empty(blockState));
            if (this.fluid == null) {
                this.fluid = logged;
            }
            pFluid = logged;
        }
        return original.call(instance, pFluid);
    }
}
