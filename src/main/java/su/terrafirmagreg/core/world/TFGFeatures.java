package su.terrafirmagreg.core.world;

import java.util.function.Function;

import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;
import com.mojang.serialization.Codec;

import net.dries007.tfc.world.feature.HotSpringConfig;
import net.dries007.tfc.world.feature.plant.CreepingPlantConfig;
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

    // Copy of vanilla's chorus plant feature but for our own blocks (because vanilla's are hardcoded to endstone)
    public static final RegistryObject<LunarChorusPlantFeature> LUNAR_CHORUS_PLANT = register(
            "lunar_chorus_plant", LunarChorusPlantFeature::new, NoneFeatureConfiguration.CODEC);
    // Copy of TFC's coral features but without the waterlogging
    public static final RegistryObject<DeadCoralClawFeature> DEAD_CORAL_CLAW = register(
            "dead_coral_claw", DeadCoralClawFeature::new, NoneFeatureConfiguration.CODEC);
    public static final RegistryObject<DeadCoralMushroomFeature> DEAD_CORAL_MUSHROOM = register(
            "dead_coral_mushroom", DeadCoralMushroomFeature::new, NoneFeatureConfiguration.CODEC);
    public static final RegistryObject<DeadCoralTreeFeature> DEAD_CORAL_TREE = register(
            "dead_coral_tree", DeadCoralTreeFeature::new, NoneFeatureConfiguration.CODEC);

    public static final RegistryObject<TallDecorativePlantFeature> TALL_DECORATIVE_PLANT = register(
            "tall_decorative_plant", TallDecorativePlantFeature::new, TallDecorativePlantConfig.CODEC);
    public static final RegistryObject<AttachedDecorativePlantFeature> ATTACHED_DECORATIVE_PLANT = register(
            "attached_decorative_plant", AttachedDecorativePlantFeature::new, AttachedDecorativePlantConfig.CODEC);
    // Used for glacian trees, so their trunks can be buried underground
    public static final RegistryObject<OffsetStackedTreeFeature> OFFSET_STACKED_TREE = register(
            "offset_stacked_tree", OffsetStackedTreeFeature::new, OffsetStackedTreeConfig.CODEC);

    // Variant of TFC's snow and ice feature but for Mars
    public static final RegistryObject<MartianPolesFeature> MARTIAN_POLES = register(
            "martian_poles", MartianPolesFeature::new, MartianPolesConfig.CODEC);
    // Variant of TFC's ice caves feature but for Mars
    public static final RegistryObject<MarsIceCaveFeature> MARS_ICE_CAVES = register(
            "mars_ice_caves", MarsIceCaveFeature::new, NoneFeatureConfiguration.CODEC);
    // Variant of TFC's hot spring feature but with any block as the wall block
    public static final RegistryObject<CustomSpringFeature> CUSTOM_SPRING = register(
            "custom_spring", CustomSpringFeature::new, HotSpringConfig.CODEC);

    // TFC 1.21 feature backport
    public static final RegistryObject<SeaStacksFeature> SEA_STACKS = register(
            "sea_stacks", SeaStacksFeature::new, NoneFeatureConfiguration.CODEC);
    // TFC 1.21 feature backport
    public static final RegistryObject<CreepingOceanPlantFeature> CREEPING_OCEAN_PLANT = register(
            "creeping_ocean_plant", CreepingOceanPlantFeature::new, CreepingPlantConfig.CODEC);
    // TFC 1.21 feature backport
    public static final RegistryObject<RotatableWaterPlantFeature> ROTATABLE_WATER_PLANT = register(
            "rotatable_water_plant", RotatableWaterPlantFeature::new, RotatableWaterPlantFeature.CODEC);
    // Modification of a TFC 1.21 feature backport
    public static final RegistryObject<CreepingUnderwaterPlantFeature> CREEPING_UNDERWATER_PLANT = register(
            "creeping_underwater_plant", CreepingUnderwaterPlantFeature::new, CreepingPlantConfig.CODEC);

    public static final RegistryObject<FluidPlugFeature> FLUID_PLUG = register(
            "fluid_plug", FluidPlugFeature::new, NoneFeatureConfiguration.CODEC);
    public static final RegistryObject<LargeLakeFeature> LARGE_LAKE = register(
            "large_lake", LargeLakeFeature::new, LargeLakeConfig.CODEC);

    // Used for lava in volcanoes
    public static final RegistryObject<EncasedSpoutFeature> ENCASED_SPOUT = register(
            "encased_spout", EncasedSpoutFeature::new, FluidSproutConfiguration.CODEC);
    // Used for GT fluid vein spouts
    public static final RegistryObject<BedrockSpoutFeature> BEDROCK_SPOUT = register(
            "bedrock_spout", BedrockSpoutFeature::new, BedrockSpoutConfig.CODEC);
    // Used for GT fluid vein fluid pools
    public static final RegistryObject<FluidPoolFeature> FLUID_POOL = register(
            "fluid_pool", FluidPoolFeature::new, FluidPoolConfig.CODEC);
    // Used for GT fluid vein gas vents
    public static final RegistryObject<FluidGasVentFeature> FLUID_GAS_VENT = register(
            "fluid_gas_vent", FluidGasVentFeature::new, FluidGasVentConfig.CODEC);

    private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name,
            Function<Codec<C>, F> factory, Codec<C> codec) {
        return FEATURES.register(name, () -> factory.apply(codec));
    }
}
