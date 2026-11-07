package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.OpenEndedPipe;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.fluids.FluidPropertyHelper;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.TFGCore;

@Mixin(value = OpenEndedPipe.class, remap = false)
public class OpenEndedPipeMixin {
    @Shadow
    private Level world;

    @Shadow
    private BlockPos outputPos;

    // Converts river water into minecraft water when it's pumped

    @Inject(method = "removeFluidFromSpace", at = @At("RETURN"), cancellable = true, remap = false)
    private void tfg$removeFluidFromSpace(CallbackInfoReturnable<FluidStack> cir) {
        FluidStack stack = cir.getReturnValue();

        if (stack.getFluid() == TFCFluids.RIVER_WATER.get()) {
            cir.setReturnValue(new FluidStack(Fluids.WATER.getSource(), stack.getAmount()));
        }

        if (stack.getFluid() == Fluids.LAVA) {

        }
    }

    // Fixes compat with TFC fluid-logging
    // (Sent to me by Allmoz from ATFC)

    @WrapOperation(method = "removeFluidFromSpace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasProperty(Lnet/minecraft/world/level/block/state/properties/Property;)Z", remap = true))
    private boolean tfg$isTFCFluidLog(BlockState state, Property<?> property, Operation<Boolean> original) {
        return original.call(state, property) || FluidPropertyHelper.hasProperty(state);
    }

    @Inject(method = "removeFluidFromSpace", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/fluid/FluidHelper;isWater(Lnet/minecraft/world/level/material/Fluid;)Z"), cancellable = true, remap = false)
    private void tfg$removeTFCFluidFromSpace(CallbackInfoReturnable<FluidStack> cir, @Local(name = "state") BlockState state) {
        if (FluidPropertyHelper.hasProperty(state)) {
            Fluid fluid = FluidPropertyHelper.getFluidBlockState(state).getFluidState().getType();

            // Don't drain lava aqueducts
            if (fluid.isSame(Fluids.LAVA) && state.is(TFCTags.Blocks.AQUEDUCTS)) {
                TFGCore.LOGGER.info("lava aqueduct");
                cir.setReturnValue(FluidStack.EMPTY);
            } else {
                if (FluidPropertyHelper.needScheduledTick(state)) {
                    TFGCore.LOGGER.info("normal aqueduct");
                    world.scheduleTick(outputPos, state.getBlock(), 20);
                } else {
                    TFGCore.LOGGER.info("not aqueduct");
                }
                world.setBlock(outputPos, FluidPropertyHelper.empty(state), 3);
                world.scheduleTick(outputPos, fluid, 1);
                cir.setReturnValue(new FluidStack(fluid, 1000));
            }
        }
    }
}
