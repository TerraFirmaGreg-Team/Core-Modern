package su.terrafirmagreg.core.mixins.common.firmalife;

import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.eerussianguy.firmalife.common.blockentities.FLBeehiveBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.environment.IGreenhouseReceiver;

@Mixin(value = FLBeehiveBlockEntity.class, remap = false)
public abstract class FLBeehiveBlockEntityMixin implements IGreenhouseReceiver {

    @Unique
    protected boolean tfg$insideValidGreenhouse;

    @Override
    public boolean tfg$isInsideValidGreenhouse() {
        return tfg$insideValidGreenhouse;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void tfg$injectGreenhouseValidityOnConstruction(CallbackInfo ci) {
        this.tfg$insideValidGreenhouse = false;
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"), remap = false) // loadAdditional is a TFC method, does not need to be remapped
    public void tfg$injectGreenhouseValidityOnLoad(CallbackInfo ci, @Local(argsOnly = true) CompoundTag nbt) {
        tfg$insideValidGreenhouse = nbt.getBoolean("inValidGreenhouse");
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"), remap = true)
    public void tfg$injectGreenhouseValidityOnSave(CallbackInfo ci, @Local(argsOnly = true) CompoundTag nbt) {
        nbt.putBoolean("inValidGreenhouse", tfg$insideValidGreenhouse);
    }

    @WrapOperation(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"))
    public float tfg$overrideTemperature(Level level, BlockPos pos, Operation<Float> original) {
        if (tfg$insideValidGreenhouse) {
            TFGCore.LOGGER.debug("FLBeehiveBlockEntity at {} is inside a valid greenhouse. Overriding temperature to 15.0", pos);
            return 15.0f; // Override temperature to 15.0 when inside a valid greenhouse
        }
        return original.call(level, pos);
    }

    @Override
    public void setValid(Level level, BlockPos pos, boolean valid, int tier, ClimateType climate) {
        if (climate == ClimateType.GREENHOUSE) {
            TFGCore.LOGGER.debug("Setting greenhouse validity for FLBeehiveBlockEntity at {} to {}", pos, valid);
            this.tfg$insideValidGreenhouse = valid;
            TFGCore.LOGGER.debug("Greenhouse validity for FLBeehiveBlockEntity at {} is now {}", pos, this.tfg$insideValidGreenhouse);
        }
        ((TickableInventoryBlockEntity<?>) (Object) this).markForSync();
    }
}
