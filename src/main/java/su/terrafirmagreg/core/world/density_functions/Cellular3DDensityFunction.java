package su.terrafirmagreg.core.world.density_functions;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.noise.Cellular3D;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import lombok.Getter;

public class Cellular3DDensityFunction implements DensityFunction.SimpleFunction {

    private Cellular3D cellularNoise;
    @Getter
    private final double scaleFactor;

    public Cellular3DDensityFunction(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.cellularNoise = new Cellular3D(678965856).spread(scaleFactor);
    }

    public static final KeyDispatchDataCodec<Cellular3DDensityFunction> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.create(
                    inst -> inst.group(
                            Codec.DOUBLE.fieldOf("scale_factor").forGetter(Cellular3DDensityFunction::getScaleFactor))
                            .apply(inst, Cellular3DDensityFunction::new)));

    @Override
    public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    @Override
    public double compute(FunctionContext functionContext) {
        return cellularNoise.noise(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());
    }

    @Override
    public double minValue() {
        return -1;
    }

    @Override
    public double maxValue() {
        return 1;
    }
}
