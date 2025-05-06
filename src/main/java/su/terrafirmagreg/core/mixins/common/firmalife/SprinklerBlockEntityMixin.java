package su.terrafirmagreg.core.mixins.common.firmalife;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.eerussianguy.firmalife.common.blockentities.SprinklerBlockEntity;
import com.eerussianguy.firmalife.common.blocks.greenhouse.AbstractSprinklerBlock;
import com.eerussianguy.firmalife.common.blocks.greenhouse.FloorSprinklerBlock;
import com.eerussianguy.firmalife.common.blocks.greenhouse.SprinklerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SprinklerBlockEntity.class, remap = false)
public abstract class SprinklerBlockEntityMixin extends BlockEntity implements ClimateReceiver {

    public SprinklerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/eerussianguy/firmalife/common/blockentities/SprinklerBlockEntity;searchForFluid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/material/Fluid;"
            ),
            remap = false
    )
    private static Fluid redirectSearchForFluid(Level level, BlockPos pos, Direction direction, boolean drain,
                                                Level unused1, BlockPos unused2, BlockState state, SprinklerBlockEntity sprinkler) {
        if (!sprinkler.isValid()) {
            updateStasisState(level, pos, state, false);
            return null;
        }

        final Fluid water = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:water"));
        if (water == null) {
            updateStasisState(level, pos, state, false);
            return null;
        }

        Direction targetDirection = null;
        BlockState blockState = level.getBlockState(pos);

        if (blockState.getBlock() instanceof SprinklerBlock) {
            Direction.Axis axis = blockState.getValue(SprinklerBlock.AXIS);
            if (axis == Direction.Axis.X) targetDirection = Direction.UP;
        } else if (blockState.getBlock() instanceof FloorSprinklerBlock) {
            targetDirection = Direction.DOWN;
        }

        Fluid result = null;
        if (targetDirection != null) {
            BlockPos neighborPos = pos.relative(targetDirection);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor != null) {
                LazyOptional<IFluidHandler> handler = neighbor.getCapability(
                        ForgeCapabilities.FLUID_HANDLER,
                        targetDirection.getOpposite()
                );

                result = handler.map(h -> {
                    FluidStack simulated = h.drain(new FluidStack(water, 1), IFluidHandler.FluidAction.SIMULATE);
                    if (!simulated.isEmpty() && simulated.getAmount() >= 1) {
                        h.drain(new FluidStack(water, 1), IFluidHandler.FluidAction.EXECUTE);
                        return water;
                    }
                    return null;
                }).orElse(null);
            }
        }

        if (result == null) {
            result = SprinklerBlockEntity.searchForFluid(level, pos, direction, drain);
        }

        updateStasisState(level, pos, state, result != null);
        return result;
    }

    @Inject(
            method = "serverTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onServerTick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity sprinkler, CallbackInfo ci) {
        if (!sprinkler.isValid() && state.getValue(AbstractSprinklerBlock.STASIS)) {
            level.setBlockAndUpdate(pos, state.setValue(AbstractSprinklerBlock.STASIS, false));
            ci.cancel();
        }
    }

    @Unique
    private static void updateStasisState(Level level, BlockPos pos, BlockState state, boolean hasWater) {
        boolean currentStasis = state.getValue(AbstractSprinklerBlock.STASIS);
        if (currentStasis != hasWater) {
            level.setBlockAndUpdate(pos, state.setValue(AbstractSprinklerBlock.STASIS, hasWater));
        }
    }
}