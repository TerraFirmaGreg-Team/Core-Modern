package su.terrafirmagreg.core.world.surface_conditions;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.noise.Cellular3D;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import lombok.Getter;

import su.terrafirmagreg.core.mixins.common.minecraft.SurfaceRulesContextAccessor;

public class Cellular3DSurfaceCondition implements SurfaceRules.ConditionSource {

    private final Cellular3D cellularNoise;
	@Getter
	private final double scaleFactor;
    @Getter
    private final double minThreshold;
    @Getter
    private final double maxThreshold;

    public static final KeyDispatchDataCodec<Cellular3DSurfaceCondition> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.create(
                    inst -> inst.group(
                            Codec.DOUBLE.fieldOf("min_threshold").forGetter(Cellular3DSurfaceCondition::getMinThreshold),
                            Codec.DOUBLE.fieldOf("max_threshold").forGetter(Cellular3DSurfaceCondition::getMaxThreshold),
							Codec.DOUBLE.fieldOf("scale_factor").forGetter(Cellular3DSurfaceCondition::getScaleFactor))
                            .apply(inst, Cellular3DSurfaceCondition::new)));

    public Cellular3DSurfaceCondition(double minThreshold, double maxThreshold, double scaleFactor) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
		this.scaleFactor = scaleFactor;

        this.cellularNoise = new Cellular3D(678965856).spread(this.scaleFactor);
    }

    @Override
    public @NotNull KeyDispatchDataCodec<Cellular3DSurfaceCondition> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        final SurfaceRulesContextAccessor access = (SurfaceRulesContextAccessor) (Object) context;
        assert access != null;

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
                double noiseVal = cellularNoise.noise(access.tfg$getBlockX(), access.tfg$getBlockY(), access.tfg$getBlockZ());
                return noiseVal >= minThreshold && noiseVal <= maxThreshold;
            }
        }

        return new NoiseThresholdCondition(context);
    }
}
