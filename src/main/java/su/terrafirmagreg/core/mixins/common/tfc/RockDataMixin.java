package su.terrafirmagreg.core.mixins.common.tfc;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.chunkdata.ChunkDataGenerator;
import net.dries007.tfc.world.chunkdata.RockData;
import net.dries007.tfc.world.settings.RockSettings;

/**
 * Fixes a crash where `RockData.getRock()` is called before surfaceHeight is initialized.
 * This can happen during world generation when features like ErosionFeature or LooseRockFeature
 * run on a chunk whose ChunkData hasn't had `generateFull()` called yet.
 */
@Mixin(value = RockData.class, remap = false)
public class RockDataMixin {

    @Shadow
    private int @Nullable [] surfaceHeight;

    @Final
    @Shadow
    private @Nullable ChunkDataGenerator generator;

    /**
     * Intercepts `getRock()` calls when `surfaceHeight` is null and uses the generator
     * to compute a fallback rock instead of crashing with a NullPointerException.
     */
    @Inject(method = "getRock(III)Lnet/dries007/tfc/world/settings/RockSettings;", at = @At("HEAD"), cancellable = true)
    private void tfg$getRockNullSafe(int x, int y, int z, CallbackInfoReturnable<RockSettings> cir) {
        if (this.surfaceHeight == null && this.generator != null) {
            cir.setReturnValue(this.generator.generateRock(x, y, z, 0, null));
        }
    }
}
