package su.terrafirmagreg.core.world;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.feature.*;

public class TFGFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE,
            TFGCore.MOD_ID);

    public static final RegistryObject<LunarChorusPlantFeature> LUNAR_CHORUS_PLANT = register("lunar_chorus_plant",
            LunarChorusPlantFeature::new, NoneFeatureConfiguration.CODEC);
    public static final RegistryObject<DeadCoralClawFeature> DEAD_CORAL_CLAW = register("dead_coral_claw",
            DeadCoralClawFeature::new, NoneFeatureConfiguration.CODEC);
    public static final RegistryObject<DeadCoralMushroomFeature> DEAD_CORAL_MUSHROOM = register("dead_coral_mushroom",
            DeadCoralMushroomFeature::new, NoneFeatureConfiguration.CODEC);
    public static final RegistryObject<DeadCoralTreeFeature> DEAD_CORAL_TREE = register("dead_coral_tree",
            DeadCoralTreeFeature::new, NoneFeatureConfiguration.CODEC);

    public static final RegistryObject<TallDecorativePlantFeature> TALL_DECORATIVE_PLANT = register(
            "tall_decorative_plant", TallDecorativePlantFeature::new, TallDecorativePlantConfig.CODEC);
    public static final RegistryObject<AttachedDecorativePlantFeature> ATTACHED_DECORATIVE_PLANT = register(
            "attached_decorative_plant", AttachedDecorativePlantFeature::new, AttachedDecorativePlantConfig.CODEC);

    public static final RegistryObject<MartianPolesFeature> MARTIAN_POLES = register("martian_poles",
            MartianPolesFeature::new, MartianPolesConfig.CODEC);

    private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name,
            Function<Codec<C>, F> factory, Codec<C> codec) {
        return FEATURES.register(name, () -> factory.apply(codec));
    }
}
