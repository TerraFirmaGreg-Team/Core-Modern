package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mixin to prevent any fuel from being burned in the Blaze Burner, by hand, liquid, or mechanical arm.
 * Needs higher priority than CreateLiquidFuel's mixin
 * Fuel still enters, but won't be burned.
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
}
