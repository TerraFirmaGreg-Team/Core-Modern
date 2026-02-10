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

/**
 * Mixin to prevent fuel from being inserted in the Blaze Burner by hand or mechanical arm in airless dimensions.
 * Needs higher priority than CreateLiquidFuel's mixin to prevent liquid insertion with a bucket.
 * Automated liquid insertion with pipes etc still works, but won't burn.
 */
@Mixin(value = BlazeBurnerBlockEntity.class, priority = 900, remap = false)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity {

    public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "tryUpdateFuel", at = @At("HEAD"), cancellable = true)
    public void tfg$tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        assert level != null;
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
            cir.setReturnValue(false);
        }
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
