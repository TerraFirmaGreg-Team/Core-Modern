package su.terrafirmagreg.core.world.surface_rule;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notenoughmail.kubejs_tfc.util.implementation.worldgen.ChunkGenAwareWorldGenerationContext;
import com.notenoughmail.kubejs_tfc.util.implementation.worldgen.KubeChunkDataGenerator;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.RockData;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.blocks.FluorapatiteBlockType;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Venus;
import su.terrafirmagreg.core.mixins.common.minecraft.SurfaceRulesContextAccessor;

public final class FluorapatiteSurfaceRuleSource implements SurfaceRules.RuleSource {
    @Getter
    private final boolean isSand;
    @Getter
    private final int offset;

    private final Map<Block, FluorapatiteBlockType> colorMap;

    private FluorapatiteSurfaceRuleSource(boolean isSand, int offset) {
        this.isSand = isSand;
        this.offset = offset;
        this.colorMap = new HashMap<>(14);

        colorMap.put(TFCBlocks.SAND.get(SandBlockType.WHITE).get(), FluorapatiteBlockType.WHITE);
        colorMap.put(TFCBlocks.SAND.get(SandBlockType.YELLOW).get(), FluorapatiteBlockType.YELLOW);
        colorMap.put(TFCBlocks.SAND.get(SandBlockType.RED).get(), FluorapatiteBlockType.ORANGE);
        colorMap.put(TFCBlocks.SAND.get(SandBlockType.BROWN).get(), FluorapatiteBlockType.BROWN);
        colorMap.put(TFCBlocks.SAND.get(SandBlockType.BLACK).get(), FluorapatiteBlockType.BLUE);
        colorMap.put(TFCBlocks.SAND.get(SandBlockType.GREEN).get(), FluorapatiteBlockType.GREEN);
        colorMap.put(TFCBlocks.SAND.get(SandBlockType.PINK).get(), FluorapatiteBlockType.ORANGE);
    }

    public static final KeyDispatchDataCodec<FluorapatiteSurfaceRuleSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("is_sand").forGetter(FluorapatiteSurfaceRuleSource::isSand),
            Codec.INT.fieldOf("offset").forGetter(FluorapatiteSurfaceRuleSource::getOffset)).apply(inst, FluorapatiteSurfaceRuleSource::new)));

    @Override
    public @NotNull KeyDispatchDataCodec<FluorapatiteSurfaceRuleSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        // Most of this is copied from KJS-TFC's RockSurfaceRuleSource::apply
        final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
        assert access != null;
        if (access.tfg$GetWorldCtx() instanceof ChunkGenAwareWorldGenerationContext aware && aware.chunkGenerator instanceof ChunkGeneratorExtension ext) {
            if (ext.chunkDataProvider().generator() instanceof KubeChunkDataGenerator gen) {
                final ChunkData data = ext.chunkDataProvider().get(access.tfg$GetChunk());
                gen.generateFullIfNot(data, access.tfg$GetChunk()); // Guarantee the RockRule has the surface y available. WORLD_SURFACE_WG and OCEAN_FLOOR_WG are available here
            }
            final RockData rocks = ext.chunkDataProvider().get(access.tfg$GetChunk()).getRockData();
            if (rocks.getCache() == null) {
                rocks.useCache(access.tfg$GetChunk().getPos());
            }
            return new FluorapatiteSurfaceRuleSource.FluorapatiteRule(rocks, isSand, offset, colorMap);
        }

        return (x, y, z) -> isSand
                ? TFGBlocks_Venus.FLUORAPATITE_SAND.get(FluorapatiteBlockType.WHITE).getDefaultState()
                : TFGBlocks_Venus.FLUORAPATITE_RAW_SANDSTONE.get(FluorapatiteBlockType.WHITE).getDefaultState();
    }

    private record FluorapatiteRule(RockData rockData, boolean isSand, int offset, Map<Block, FluorapatiteBlockType> colorMap) implements SurfaceRules.SurfaceRule {
        @Override
        public @Nullable BlockState tryApply(int x, int y, int z) {

            var color = colorMap.get(rockData.getRock(x, y, z).sand());

            if (offset > 0) {
                color = FluorapatiteBlockType.valueOf(color.ordinal() + offset);
            }

            if (isSand) {
                return TFGBlocks_Venus.FLUORAPATITE_SAND.get(color).getDefaultState();
            } else {
                return TFGBlocks_Venus.FLUORAPATITE_RAW_SANDSTONE.get(color).getDefaultState();
            }
        }
    }
}
