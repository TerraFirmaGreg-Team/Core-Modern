package su.terrafirmagreg.core.world;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.registries.DeferredRegister;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.density_functions.Cellular3DDensityFunction;

public class TFGDensityFunctions {

    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, TFGCore.MOD_ID);

    static {
        DENSITY_FUNCTIONS.register("cellular3d", Cellular3DDensityFunction.CODEC::codec);
    }
}
