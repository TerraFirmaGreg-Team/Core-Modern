package su.terrafirmagreg.core.mixins.common.firmalife;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.firmalife.common.blockentities.HydroponicPlanterBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.SprinklerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

@Mixin(HydroponicPlanterBlockEntity.class)
public abstract class HydroponicPlanterBlockEntityMixin {

    @Unique
    private static final Field HAS_PIPE_FIELD;

    static {
        try {
            HAS_PIPE_FIELD = HydroponicPlanterBlockEntity.class.getDeclaredField("hasPipe");
            HAS_PIPE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to access hasPipe field", e);
        }
    }

    @Redirect(method = "hydroponicServerTick", at = @At(value = "INVOKE", target = "Lcom/eerussianguy/firmalife/common/blockentities/SprinklerBlockEntity;searchForFluid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/material/Fluid;"), remap = false)
    private static Fluid redirectSearchForFluid(
            Level level, BlockPos pos, Direction direction, boolean drain,
            Level unusedLevel, BlockPos unusedPos, BlockState state, HydroponicPlanterBlockEntity planter) {
        Fluid result = checkGTFluidHandler(level, pos.below());
        if (result == Fluids.EMPTY) {
            result = SprinklerBlockEntity.searchForFluid(level, pos, direction, drain);
        }
        try {
            boolean hasPipe = result == Fluids.WATER;
            boolean current = HAS_PIPE_FIELD.getBoolean(planter);
            if (current != hasPipe) {
                HAS_PIPE_FIELD.setBoolean(planter, hasPipe);
                planter.markForSync();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to update hasPipe state", e);
        }
        return result;
    }

    @Unique
    private static Fluid checkGTFluidHandler(Level level, BlockPos pipePos) {
        BlockEntity be = level.getBlockEntity(pipePos);
        if (be != null) {
            return be.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP)
                    .map(handler -> {
                        FluidStack waterStack = new FluidStack(Fluids.WATER, 1);
                        FluidStack drained = handler.drain(waterStack, IFluidHandler.FluidAction.SIMULATE);
                        if (!drained.isEmpty() && drained.getAmount() >= 1) {
                            handler.drain(waterStack, IFluidHandler.FluidAction.EXECUTE);
                            return Fluids.WATER;
                        }
                        return Fluids.EMPTY;
                    })
                    .orElse(Fluids.EMPTY);
        }
        return Fluids.EMPTY;
    }
}
