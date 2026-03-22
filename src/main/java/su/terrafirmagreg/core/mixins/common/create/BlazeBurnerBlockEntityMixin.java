package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bawnorton.mixinsquared.TargetHandler;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = BlazeBurnerBlockEntity.class, priority = 1500, remap = false)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity {

    public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Cancel CLF's tryUpdateFuel entirely. Fluid fuel insertion is now handled
     * with proper Forge fluid API in {@link BlazeBurnerBlockMixin}.
     */
    @SuppressWarnings("CancellableInjectionUsage")
    @TargetHandler(mixin = "com.forsteri.createliquidfuel.mixin.MixinBlazeBurnerTileEntity", name = "tryUpdateFuel")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void tfg$cancelLiquidFuelTryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> originalCir, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * Prevent inserted liquid fuel from being burned
     * MixinSquared, targeting Create Liquid Fuel's tick method
     * Exits out of their mixin if we're not in the overworld/beneath.
     */
    @SuppressWarnings("CancellableInjectionUsage") // Idea gets confused about the two CallbackInfos
    @TargetHandler(mixin = "com.forsteri.createliquidfuel.mixin.MixinBlazeBurnerTileEntity", name = "tick")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void tfg$cancelLiquidFuelTick(CallbackInfo originalCi, CallbackInfo ci) {
        assert level != null;
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
            ci.cancel();
        }
    }
}
