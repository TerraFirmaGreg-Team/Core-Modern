package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blockentities.BloomeryBlockEntity;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.api.systems.OxygenApi;

/**
 * Prevents the bloomery from being lit in dimensions with no oxygen
 */

@Mixin(value = BloomeryBlockEntity.class)
public abstract class BloomeryBlockEntityMixin extends BlockEntity {

    public BloomeryBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Inject(method = "light", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$light(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (level != null && !OxygenApi.API.hasOxygen(level, worldPosition)) {
            Helpers.playSound(level, worldPosition, SoundEvents.FIRE_EXTINGUISH);
            cir.setReturnValue(false);
        }
    }
}
