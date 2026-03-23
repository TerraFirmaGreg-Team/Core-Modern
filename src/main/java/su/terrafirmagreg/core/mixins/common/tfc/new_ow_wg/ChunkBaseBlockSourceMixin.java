package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.world.ChunkBaseBlockSource;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.chunk.IChunkBaseBlockSource;

@Mixin(value = ChunkBaseBlockSource.class, remap = false)
public class ChunkBaseBlockSourceMixin implements IChunkBaseBlockSource {
    @Shadow
    @Final
    private BlockState[] cachedFluidStates;
    @Shadow
    @Final
    private BlockState freshWater, saltWater;

    @Unique
    private static int tfg$index(int x, int z) {
        return (x & 15) | ((z & 15) << 4);
    }

    /**
     * Getting saltwater from only the biome has issues at shores, where sometimes the original land biome will influence the water
     * on the edge of the ocean biome, but the shore biome will influence the water at the shore proper, leading to freshwater "rings"
     * around the shore. This can be addressed by only placing freshwater when the biome weight is sufficiently high, but that causes
     * rivers to be salty as they do not ever have high biome weights, so that is then special cased. With both of those checks, there is
     * still an edge case where if we only check the primary biome's weight, at intersections between 3 non-salty biomes saltwater will
     * generate because the highest weight is still low. Thus, before running those checks we see if there are any nearby biomes that are salty
     */
    @Override
    public void tfg$useAccurateBiome(int localX, int localZ, BiomeExtension biome, double weight, boolean couldBeSalty) {
        cachedFluidStates[tfg$index(localX, localZ)] = !couldBeSalty || (!biome.isSalty() && (weight > 0.5 || biome == TFCBiomes.RIVER)) ? freshWater : saltWater;
    }
}
