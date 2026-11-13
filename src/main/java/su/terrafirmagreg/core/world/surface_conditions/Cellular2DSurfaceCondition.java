package su.terrafirmagreg.core.world.surface_conditions;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.noise.Cellular2D;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import lombok.Getter;

import su.terrafirmagreg.core.mixins.common.minecraft.SurfaceRulesContextAccessor;

public class Cellular2DSurfaceCondition implements SurfaceRules.ConditionSource {

    private final Cellular2D cellularNoise;
    @Getter
    private final double scaleFactor;
    @Getter
    private final double minThreshold;
    @Getter
    private final double maxThreshold;

    public static final KeyDispatchDataCodec<Cellular2DSurfaceCondition> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.create(
                    inst -> inst.group(
                            Codec.DOUBLE.fieldOf("min_threshold").forGetter(Cellular2DSurfaceCondition::getMinThreshold),
                            Codec.DOUBLE.fieldOf("max_threshold").forGetter(Cellular2DSurfaceCondition::getMaxThreshold),
                            Codec.DOUBLE.fieldOf("scale_factor").forGetter(Cellular2DSurfaceCondition::getScaleFactor))
                            .apply(inst, Cellular2DSurfaceCondition::new)));

    public Cellular2DSurfaceCondition(double minThreshold, double maxThreshold, double scaleFactor) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.scaleFactor = scaleFactor;

        this.cellularNoise = new Cellular2D(16583746).spread(this.scaleFactor);
    }

    @Override
    public @NotNull KeyDispatchDataCodec<Cellular2DSurfaceCondition> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
        assert access != null;

        class NoiseThresholdCondition extends SurfaceRules.LazyXZCondition {
            NoiseThresholdCondition(SurfaceRules.Context context) {
                super(context);
            }

            @Override
            protected boolean compute() {
                var cell = cellularNoise.cell(access.tfg$getBlockX(), access.tfg$getBlockZ());
                double noiseVal = cell.f2() - cell.f1();
                return noiseVal >= minThreshold && noiseVal <= maxThreshold;
            }
        }

        return new NoiseThresholdCondition(context);
    }
}
