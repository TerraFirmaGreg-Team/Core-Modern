package su.terrafirmagreg.core.world.surface_conditions;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.noise.Cellular3D;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import lombok.Getter;

import su.terrafirmagreg.core.mixins.common.minecraft.SurfaceRulesContextAccessor;

public class MercurySurfaceCondition implements SurfaceRules.ConditionSource {

    private Cellular3D cellularNoise;
    @Getter
    private final ResourceKey<NormalNoise.NoiseParameters> noise;
    @Getter
    private final double minThreshold;
    @Getter
    private final double maxThreshold;

    public static final KeyDispatchDataCodec<MercurySurfaceCondition> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.create(
                    inst -> inst.group(
                            ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(MercurySurfaceCondition::getNoise),
                            Codec.DOUBLE.fieldOf("min_threshold").forGetter(MercurySurfaceCondition::getMinThreshold),
                            Codec.DOUBLE.fieldOf("max_threshold").forGetter(MercurySurfaceCondition::getMaxThreshold))
                            .apply(inst, MercurySurfaceCondition::new)));

    public MercurySurfaceCondition(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold) {
        this.noise = noise;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;

        this.cellularNoise = new Cellular3D(678965856).spread(0.03);
    }

    @Override
    public @NotNull KeyDispatchDataCodec<MercurySurfaceCondition> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
        assert access != null;
        //final NormalNoise normalnoise = access.tfg$getRandomState().getOrCreateNoise(this.noise);

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
                //final var cell = cellularNoise.cell(access.tfg$getBlockX(), access.tfg$getBlockY(), access.tfg$getBlockZ());

                double noiseVal = cellularNoise.noise(access.tfg$getBlockX(), access.tfg$getBlockY(), access.tfg$getBlockZ());

                return noiseVal >= minThreshold && noiseVal <= maxThreshold;
            }
        }

        return new NoiseThresholdCondition(context);
    }
}
