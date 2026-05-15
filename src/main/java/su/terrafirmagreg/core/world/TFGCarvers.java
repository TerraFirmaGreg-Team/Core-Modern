package su.terrafirmagreg.core.world;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.carvers.LavaTubeWorldCarver;

public class TFGCarvers {

    public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(Registries.CARVER, TFGCore.MOD_ID);

    public static final RegistryObject<LavaTubeWorldCarver> LAVA_TUBE = register(
            "lava_tube", LavaTubeWorldCarver::new, CaveCarverConfiguration.CODEC);

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> RegistryObject<F> register(String name, Function<Codec<C>, F> factory, Codec<C> codec) {
        return CARVERS.register(name, () -> factory.apply(codec));
    }
}
