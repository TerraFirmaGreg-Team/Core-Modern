package su.terrafirmagreg.core.world.surface_conditions;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import su.terrafirmagreg.core.mixins.common.minecraft.SurfaceRulesContextAccessor;

public record Noise3DThresholdSurfaceConditionSource(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold) implements SurfaceRules.ConditionSource {

    public static final KeyDispatchDataCodec<Noise3DThresholdSurfaceConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.create(
                    inst -> inst.group(
                            ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(Noise3DThresholdSurfaceConditionSource::noise),
                            Codec.DOUBLE.fieldOf("min_threshold").forGetter(Noise3DThresholdSurfaceConditionSource::minThreshold),
                            Codec.DOUBLE.fieldOf("max_threshold").forGetter(Noise3DThresholdSurfaceConditionSource::maxThreshold))
                            .apply(inst, Noise3DThresholdSurfaceConditionSource::new)));

    @Override
    public @NotNull KeyDispatchDataCodec<Noise3DThresholdSurfaceConditionSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(final SurfaceRules.Context context) {
        final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
        assert access != null;
        final NormalNoise normalnoise = access.tfg$getRandomState().getOrCreateNoise(this.noise);

        class NoiseThresholdCondition extends SurfaceRules.LazyCondition {
            NoiseThresholdCondition(SurfaceRules.Context context) {
                super(context);
            }

            @Override
            protected long getContextLastUpdate() {
                return access.tfg$getLastUpdateXZ() ^ access.tfg$getLastUpdateY();
            }

            @Override
            protected boolean compute() {
                double d0 = normalnoise.getValue(access.tfg$getBlockX(), access.tfg$getBlockY(), access.tfg$getBlockZ());
                return d0 >= minThreshold && d0 <= maxThreshold;
            }
        }

        return new NoiseThresholdCondition(context);
    }

}
