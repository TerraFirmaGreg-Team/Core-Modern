package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.feature.ErosionFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Fixes a crash where `ErosionFeature` runs on chunks that aren't fully initialized.
 */
@Mixin(value = ErosionFeature.class)
public class ErosionFeatureMixin {

    /**
     * If the chunk data status is not FULL, we skip the feature to prevent NullPointerExceptions.
     */
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void tfg$checkChunkDataStatus(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        // Check if the chunk generator is a TFC generator.
        if (!(context.chunkGenerator() instanceof ChunkGeneratorExtension)) {
            cir.setReturnValue(false);
            return;
        }

        BlockPos pos = context.origin();
        ChunkAccess chunk = context.level().getChunk(pos);
        ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        ChunkData chunkData = provider.get(chunk);

        // Check if chunk data is fully initialized.
        if (chunkData.status() != ChunkData.Status.FULL) {
            cir.setReturnValue(false);
        }
    }
}
