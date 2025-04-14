package su.terrafirmagreg.core.mixins.common.firmalife;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.eerussianguy.firmalife.common.blockentities.SprinklerBlockEntity;
import com.eerussianguy.firmalife.common.blocks.greenhouse.SprinklerBlock;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Exception
 * Позволяет спринклеру кушать воду из труб GregTech.
 * */
@Mixin(value = SprinklerBlockEntity.class, remap = false)
public abstract class SprinklerBlockEntityMixin extends TFCBlockEntity implements FluidTankCallback, ClimateReceiver {
    protected SprinklerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

//    @Unique
//    private static final Fluid tfg$CACHED_WATER = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:water"));
//
//    @Unique
//    private final FluidTank tfg$fluidTank = tfg$createFluidTank();
//
//    @Unique
//    private final LazyOptional<IFluidHandler> tfg$fluidHandler = LazyOptional.of(() -> tfg$fluidTank);
//
//    protected SprinklerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
//        super(type, pos, state);
//    }
//
//    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lcom/eerussianguy/firmalife/common/blockentities/SprinklerBlockEntity;searchForFluid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/level/material/Fluid;"), remap = false)
//    private static Fluid serverTickSearch(Level l1, BlockPos pos1, Direction dir1, Level level, BlockPos pos, BlockState state, SprinklerBlockEntity sprinkler) {
//        if (level.getBlockEntity(pos) instanceof SprinklerBlockEntity sprinklerBlockEntity) {
//            var fluidCapability = sprinklerBlockEntity
//                    .getCapability(ForgeCapabilities.FLUID_HANDLER, tfg$getDirectionForFluidOperating(state))
//                    .orElse(null);
//
//            if (fluidCapability != null) {
//                var drainedFluidStack = fluidCapability.drain(new FluidStack(tfg$CACHED_WATER, 10), IFluidHandler.FluidAction.EXECUTE);
//
//                if (drainedFluidStack.isEmpty()) {
//                    return null;
//                }
//
//                return drainedFluidStack.getFluid();
//            }
//
//            return null;
//        }
//
//        return null;
//    }
//
//    @Unique
//    private static Direction tfg$getDirectionForFluidOperating(BlockState state) {
//        return state.getBlock() instanceof SprinklerBlock ? Direction.UP : Direction.DOWN;
//    }
//
//    @Unique
//    public void tfg$loadClient(CompoundTag tag) {
//        if (tag.contains("Fluid")) {
//            tfg$fluidTank.readFromNBT(tag.getCompound("Fluid"));
//        }
//    }
//
//    @Unique
//    public void tfg$saveClient(CompoundTag tag) {
//        CompoundTag fluid = new CompoundTag();
//        tfg$fluidTank.writeToNBT(fluid);
//        tag.put("Fluid", fluid);
//    }
//
//    @NotNull
//    @Override
//    public CompoundTag getUpdateTag() {
//        CompoundTag tag = super.getUpdateTag();
//        tfg$saveClient(tag);
//        return tag;
//    }
//
//    @Override
//    public void handleUpdateTag(CompoundTag tag) {
//        if (tag != null) {
//            tfg$loadClient(tag);
//        }
//    }
//
//    @Unique
//    private FluidTank tfg$createFluidTank() {
//        return new FluidTank(1000, (fluid -> fluid.getFluid().is(FluidTags.WATER))) {
//            @Override
//            protected void onContentsChanged() {
//                setChanged();
//            }
//        };
//    }
//
//    @NotNull
//    @Override
//    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
//        if (cap == ForgeCapabilities.FLUID_HANDLER) {
//            if (side == Direction.UP || side == Direction.DOWN) {
//                return tfg$fluidHandler.cast();
//            }
//            return LazyOptional.empty();
//        }
//        return super.getCapability(cap, side);
//    }
}