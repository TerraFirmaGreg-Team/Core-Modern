package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreVeinUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = OreGenerator.class, remap = false)
public class OreGeneratorMixin {

//    @Inject(method = "computeVeinOrigin", at = @At("RETURN"), remap = false, cancellable = true)
//    private static void computeVeinOrigin(WorldGenLevel level, ChunkGenerator generator, ChunkPos pos, RandomSource random, BlockPos veinCenter, GTOreDefinition entry, CallbackInfoReturnable<Optional<BlockPos>> cir) {
//        int layerSeed = WorldGeneratorUtils.getWorldGenLayerKey(entry.layer())
//                .map(String::hashCode)
//                .orElse(0);
//
//        var layeredRandom = new XoroshiroRandomSource(random.nextLong() ^ ((long) layerSeed));
//
//        veinCenter = OreVeinUtil.getVeinCenter(pos, layeredRandom).orElse(veinCenter);
//
//        System.out.println(entry.layer().getSerializedName() + " " + veinCenter);
//
//        var res = entry.range().getPositions(
//                new PlacementContext(level, generator, Optional.empty()),
//                layeredRandom, veinCenter).findFirst();
//
//        cir.setReturnValue(res);
//    }

}
