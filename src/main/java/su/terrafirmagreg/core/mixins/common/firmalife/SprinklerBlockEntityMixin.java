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
        final Fluid water = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:water"));
        if (water == null) {
            tfg$updateStasisState(level, pos, state, false);
            return null;
        }

        BlockState blockState = level.getBlockState(pos);
        Direction targetDirection = null;

        if (blockState.getBlock() instanceof SprinklerBlock) {
            Direction.Axis axis = blockState.getValue(SprinklerBlock.AXIS);
            if (axis == Direction.Axis.X) targetDirection = Direction.UP;
        } else if (blockState.getBlock() instanceof FloorSprinklerBlock) {
            targetDirection = Direction.DOWN;
        }

        if (targetDirection != null) {
            BlockPos neighborPos = pos.relative(targetDirection);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor != null) {
                LazyOptional<IFluidHandler> handler = neighbor.getCapability(
                        ForgeCapabilities.FLUID_HANDLER,
                        targetDirection.getOpposite()
                );
                if (handler.isPresent()) {
                    FluidStack drained = handler.map(h -> {
                        if (h.drain(1, IFluidHandler.FluidAction.SIMULATE).getAmount() >= 1) {
                            return h.drain(1, IFluidHandler.FluidAction.EXECUTE);
                        }
                        return FluidStack.EMPTY;
                    }).orElse(FluidStack.EMPTY);
                    if (!drained.isEmpty()) {
                        tfg$updateStasisState(level, pos, state, true);
                        return water;
                    }
                }
            }
        }

        Fluid fallback = SprinklerBlockEntity.searchForFluid(level, pos, direction, drain);
        tfg$updateStasisState(level, pos, state, fallback != null);
        return fallback;
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
    private static void tfg$updateStasisState(Level level, BlockPos pos, BlockState state, boolean hasWater) {
        if (state.hasProperty(AbstractSprinklerBlock.STASIS)) {
            boolean currentStasis = state.getValue(AbstractSprinklerBlock.STASIS);
            if (currentStasis != hasWater) {
                level.setBlockAndUpdate(pos, state.setValue(AbstractSprinklerBlock.STASIS, hasWater));
            }
        }
    }
}