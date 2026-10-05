package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.LinkedList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Makes small miners not mine anything in the Beneath below a certain Y level.
 * Also fixes miners skipping ore after a chunk unload / server restart (see tfg$rewindCursor).
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

    @Shadow
    @Final
    private LinkedList<BlockPos> blocksToMine;

    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    protected int z;
    @Shadow
    protected int mineX;
    @Shadow
    protected int mineY;
    @Shadow
    protected int mineZ;

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

    /**
     * blocksToMine is not persisted, but the scan cursor (x/y/z) is, and it runs ahead of the queue
     * by up to ~30 layers. After a chunk unload or server restart the queue is empty and the next scan
     * starts from the cursor. If there is ore below the cursor, the miner mines it, the cursor then
     * moves past it, and every queued-but-unmined block above it is never found again: the miner
     * reports "Done" with ore left in its area. Same root cause as Modpack-Modern #4600.
     * Rewind the cursor to the last mined block before refilling an empty queue — the same thing
     * serverTick already does between batches, just before the scan instead of after it.
     * Upstream fix: GregTech-Modern #4528 / PR #5226 (persists the queue); not in a 1.20.1 release yet.
     */
    @Inject(method = "checkBlocksToMine", at = @At("HEAD"), remap = false)
    private void tfg$rewindCursor(CallbackInfo ci) {
        if (blocksToMine.isEmpty() && mineY != Integer.MAX_VALUE) {
            x = mineX;
            y = mineY;
            z = mineZ;
        }
    }
}
