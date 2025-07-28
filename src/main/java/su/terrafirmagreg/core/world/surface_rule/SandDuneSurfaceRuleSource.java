package su.terrafirmagreg.core.world.surface_rule;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.NotNull;

public record SandDuneSurfaceRuleSource(BlockState sandBlockState, BlockState sandPileState) implements SurfaceRules.RuleSource {


	public static final KeyDispatchDataCodec<SandDuneSurfaceRuleSource> CODEC =
		KeyDispatchDataCodec.of(
			RecordCodecBuilder.create(instance -> instance.group(
				BlockState.CODEC.fieldOf("sand_block_state").forGetter(SandDuneSurfaceRuleSource::sandBlockState),
				BlockState.CODEC.fieldOf("sand_pile_state").forGetter(SandDuneSurfaceRuleSource::sandPileState))
				.apply(instance, SandDuneSurfaceRuleSource::new)));

	@Override
	public @NotNull KeyDispatchDataCodec<SandDuneSurfaceRuleSource> codec() {
		return CODEC;
	}

	@Override
	public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
		final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
		assert access != null;

		var chunk = access.kubejs_tfc$GetChunk();
		if (chunk != null)
		{
			return new SandDuneProcessingRule(sandBlockState, sandPileState, chunk);
		}

		return (x, y, z) -> sandBlockState;
	}

	private record SandDuneProcessingRule(BlockState sandBlockState, BlockState sandPileState, ChunkAccess chunk) implements SurfaceRules.SurfaceRule {
		@Override
		public BlockState tryApply(int x, int y, int z) {

			//chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, )
		}
	}
}
