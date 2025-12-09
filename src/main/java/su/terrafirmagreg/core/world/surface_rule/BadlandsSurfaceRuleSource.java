package su.terrafirmagreg.core.world.surface_rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.notenoughmail.kubejs_tfc.util.implementation.worldgen.RockSurfaceRuleSource;

import net.dries007.tfc.world.Codecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class BadlandsSurfaceRuleSource implements SurfaceRules.RuleSource {
    private final int rawRockWeight;
    private final List<BlockState> fullPalette;
    private final BlockState fallback;
    private final RockSurfaceRuleSource rockRuleSource;

    public BadlandsSurfaceRuleSource(int rawRockWeight, List<BlockState> palette, BlockState fallback) {
        this.rawRockWeight = rawRockWeight;
        this.fallback = fallback;

        this.rockRuleSource = new RockSurfaceRuleSource(RockSurfaceRuleSource.RockType.RAW, fallback, (x, y, z) -> fallback);

        // Build up a much bigger palette by creating multiple shuffles of the smaller one, so the blocks look more evenly distributed
        this.fullPalette = new ArrayList<>();
        Random random = new Random(732489239423L);

        for (int j = 0; j < (512 / rawRockWeight + palette.size()); j++) {
            var tempPalette = new ArrayList<>(palette);
            for (int i = 0; i < rawRockWeight; i++) {
                // add nulls to represent raw rock
                tempPalette.add(null);
            }

            Collections.shuffle(tempPalette, random);
            fullPalette.addAll(tempPalette);
        }
    }

    public static final KeyDispatchDataCodec<BadlandsSurfaceRuleSource> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("raw_rock_weight").forGetter(BadlandsSurfaceRuleSource::rawRockWeight),
            Codecs.BLOCK_STATE.listOf().fieldOf("palette").forGetter(BadlandsSurfaceRuleSource::palette),
            Codecs.BLOCK_STATE.fieldOf("fallback").forGetter(BadlandsSurfaceRuleSource::fallback)).apply(inst, BadlandsSurfaceRuleSource::new)));

    @Override
    public @NotNull KeyDispatchDataCodec<BadlandsSurfaceRuleSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        return new BadlandsRule(rawRockWeight, fullPalette, () -> rockRuleSource.apply(context));
    }

    private record BadlandsRule(int rawRockWeight, List<BlockState> fullPalette, Supplier<SurfaceRules.SurfaceRule> rockRule) implements SurfaceRules.SurfaceRule {
        @Override
        public @Nullable BlockState tryApply(int x, int y, int z) {

            BlockState state = fullPalette.get((y + 64) % 512);
            if (state != null) {
                return state;
            } else {
                return rockRule.get().tryApply(x, y, z);
            }
        }
    }

    // Getters for the codec
    public int rawRockWeight() {
        return rawRockWeight;
    }

    public List<BlockState> palette() {
        return fullPalette;
    }

    public BlockState fallback() {
        return fallback;
    }
}
