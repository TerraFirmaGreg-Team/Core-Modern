package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.region.RegionPartition;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.region.Units;
import net.minecraft.core.QuartPos;

import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

@Mixin(value = BiomeSourceExtension.class, remap = false)
public interface BiomeSourceExtensionMixin {

    @Shadow
    BiomeExtension getBiomeExtensionNoRiver(int quartX, int quartZ);

    @Shadow
    RegionPartition.Point getPartition(int blockX, int blockZ);

    /**
     * @author Pyritie
     * @reason it's difficult to inject into an interface
     */
    @Overwrite
    default BiomeExtension getBiomeExtension(int quartX, int quartZ) {
        final BiomeExtension biome = getBiomeExtensionNoRiver(quartX, quartZ);
        if (biome.hasRivers()) {
            final RegionPartition.Point partitionPoint = getPartition(QuartPos.toBlock(quartX), QuartPos.toBlock(quartZ));
            final double exactGridX = Units.quartToGridExact(quartX);
            final double exactGridZ = Units.quartToGridExact(quartZ);

            for (RiverEdge edge : partitionPoint.rivers()) {
                if (edge.fractal().intersect(exactGridX, exactGridZ, 0.08f)) {
                    return OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT ? TFGBiomes.RIVER : TFCBiomes.RIVER;
                }
            }
        }
        return biome;
    }
}
