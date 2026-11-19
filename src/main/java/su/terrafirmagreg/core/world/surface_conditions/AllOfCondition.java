// Copied from Lithostitched 1.21

package su.terrafirmagreg.core.world.surface_conditions;

import java.util.List;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record AllOfCondition(List<SurfaceRules.ConditionSource> conditions) implements SurfaceRules.ConditionSource {
    public static final KeyDispatchDataCodec<AllOfCondition> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
            SurfaceRules.ConditionSource.CODEC.listOf().fieldOf("conditions").forGetter(AllOfCondition::conditions)).apply(instance, AllOfCondition::new)));

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        return new Condition(this.conditions.stream().map(source -> source.apply(context)).toList());
    }

    private record Condition(List<SurfaceRules.Condition> conditions) implements SurfaceRules.Condition {
        @Override
        public boolean test() {
            for (SurfaceRules.Condition condition : this.conditions) {
                if (!condition.test())
                    return false;
            }
            return true;
        }
    }
}
