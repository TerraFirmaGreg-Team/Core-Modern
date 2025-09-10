package su.terrafirmagreg.core.world.surface_rule;

import org.jetbrains.annotations.NotNull;

import com.notenoughmail.kubejs_tfc.util.implementation.mixin.accessor.SurfaceRulesContextAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record NeedsPostProcessingSurfaceRuleSource(BlockState state,
        SurfaceRules.SurfaceRule fallbackRule) implements SurfaceRules.RuleSource {

    private NeedsPostProcessingSurfaceRuleSource(BlockState state) {
        this(state, (pX, pY, pZ) -> state);
    }

    public static final KeyDispatchDataCodec<NeedsPostProcessingSurfaceRuleSource> CODEC = KeyDispatchDataCodec
            .of(BlockState.CODEC
                    .xmap(NeedsPostProcessingSurfaceRuleSource::new, NeedsPostProcessingSurfaceRuleSource::state)
                    .fieldOf("state"));

    @Override
    public @NotNull KeyDispatchDataCodec<NeedsPostProcessingSurfaceRuleSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
        assert access != null;

        var chunk = access.kubejs_tfc$GetChunk();
        if (chunk != null) {
            return new NeedsPostProcessingRule(state, chunk);
        }

        return fallbackRule;
    }

    private record NeedsPostProcessingRule(BlockState state, ChunkAccess chunk) implements SurfaceRules.SurfaceRule {
        @Override
        public BlockState tryApply(int x, int y, int z) {
            chunk.markPosForPostprocessing(new BlockPos(x, y, z));
            return state;
        }
    }
}
