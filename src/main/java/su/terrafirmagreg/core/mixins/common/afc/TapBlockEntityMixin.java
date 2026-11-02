package su.terrafirmagreg.core.mixins.common.afc;

import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.therighthon.afc.common.blockentities.AFCBlockEntities;
import com.therighthon.afc.common.blockentities.TapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.environment.IGreenhouseReceiver;

@Mixin(value = TapBlockEntity.class, remap = false)
public abstract class TapBlockEntityMixin extends BlockEntity implements IGreenhouseReceiver {

    @Unique
    protected boolean tfg$insideValidGreenhouse;

    public TapBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Unique
    public boolean tfg$isInsideValidGreenhouse() {
        return tfg$insideValidGreenhouse;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void tfg$injectGreenhouseValidityOnConstruction(CallbackInfo ci) {
        this.tfg$insideValidGreenhouse = false;
    }

    @WrapMethod(method = "isTempOkay")
    private static boolean tfg$wrapIsTempOkay(Level level, BlockPos pos, float minTemp, float maxTemp, Operation<Boolean> original) {
        IGreenhouseReceiver tap = (IGreenhouseReceiver) level.getBlockEntity(pos, AFCBlockEntities.TAP_BLOCK_ENTITY.get()).orElse(null);
        if (tap != null && tap.tfg$isInsideValidGreenhouse()) {
            TFGCore.LOGGER.debug("TapBlockEntity at {} is inside a valid greenhouse. Overriding temperature check to true", pos);
            return true; // Override temperature check to true when inside a valid greenhouse
        }
        return original.call(level, pos, minTemp, maxTemp);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        tfg$insideValidGreenhouse = nbt.getBoolean("inValidGreenhouse");
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putBoolean("inValidGreenhouse", tfg$insideValidGreenhouse);
    }

    @Override
    public void setValid(Level level, BlockPos pos, boolean valid, int tier, ClimateType climate) {
        if (climate == ClimateType.GREENHOUSE) {
            TFGCore.LOGGER.info("Setting greenhouse validity for TapBlockEntity at {} to {}", pos, valid);
            this.tfg$insideValidGreenhouse = valid;
            TFGCore.LOGGER.info("Greenhouse validity for TapBlockEntity at {} is now {}", pos, this.tfg$insideValidGreenhouse);
        }
        this.setChanged();
    }

}
