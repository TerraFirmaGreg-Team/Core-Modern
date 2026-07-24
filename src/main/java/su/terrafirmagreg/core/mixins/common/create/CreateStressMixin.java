package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.api.stress.BlockStressValues;

import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.dries007.tfc.common.blocks.devices.QuernBlock;
import net.minecraft.world.level.block.Block;

@Mixin(value = BlockStressValues.class, remap = false)
public abstract class CreateStressMixin {
    @Inject(method = "getImpact", at = @At("HEAD"), cancellable = true)
    private static void onGetImpact(Block block, CallbackInfoReturnable<Double> cir) {
        if (block instanceof QuernBlock)
            cir.setReturnValue(0.25);
        if (block instanceof BellowsBlock)
            cir.setReturnValue(0.5);
    }
}
