package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blocks.CompostTumblerBlock;
import com.simibubi.create.api.stress.BlockStressValues;

import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.dries007.tfc.common.blocks.devices.QuernBlock;
import net.minecraft.world.level.block.Block;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Mixin to add TFC stress values directly.
 * There might be a better way to do this, but this is the best I could come up with.
 */
@Mixin(value = BlockStressValues.class, remap = false)
public abstract class CreateStressMixin {
    @Inject(method = "getImpact", at = @At("HEAD"), cancellable = true)
    private static void onGetImpact(Block block, CallbackInfoReturnable<Double> cir) {
        if (block instanceof QuernBlock)
            cir.setReturnValue(TFGConfig.SERVER.QUERN_STRESS_IMPACT.get());
        if (block instanceof CompostTumblerBlock)
            cir.setReturnValue(TFGConfig.SERVER.COMPOSTER_STRESS_IMPACT.get());
        if (block instanceof BellowsBlock)
            cir.setReturnValue(TFGConfig.SERVER.BELLOWS_STRESS_IMPACT.get());
    }
}
