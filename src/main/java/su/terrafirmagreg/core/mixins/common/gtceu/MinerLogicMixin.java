package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.LinkedList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Makes small miners not mine anything in the Beneath below a certain Y level.
 */

@Mixin(value = MinerLogic.class, remap = false)
public abstract class MinerLogicMixin {

    @Final
    @Shadow
    protected IMiner miner;

    // True for large miners that are not on silk touch mode, false for single block ones
    @Shadow
    protected abstract boolean hasPostProcessing();

    @Shadow
    private int minBuildHeight;

    @Inject(method = "getBlocksToMine", at = @At("HEAD"), remap = false)
    private void tfg$getBlocksToMine(CallbackInfoReturnable<LinkedList<BlockPos>> cir) {
        var level = miner.self().getLevel();
        assert level != null;

        // True for large miners that are not on silk touch mode
        if (!hasPostProcessing()) {
            var dim = level.dimension();

            // Don't mine below Y=80 in the beneath
            if (dim == Level.NETHER && TFGConfig.SERVER.enableBeneathMiningRestrictions.get()) {
                minBuildHeight = TFGConfig.SERVER.disabledBeneathMiningYLevel.get();
            }
            // Don't mine at all on venus/mercury
            else if ((dim == Planet.VENUS || dim == Planet.MERCURY) && TFGConfig.SERVER.enableHotPlanetMiningRestrictions.get()) {
                minBuildHeight = level.getMaxBuildHeight();
            }
        }
    }
}
