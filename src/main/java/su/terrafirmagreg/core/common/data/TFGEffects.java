package su.terrafirmagreg.core.common.data;

import java.util.function.Supplier;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.effects.InstantDamageEffect;
import su.terrafirmagreg.core.common.data.effects.MedicalConditionEffect;
import su.terrafirmagreg.core.common.data.effects.TemperatureChangeEffect;

public class TFGEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
            TFGCore.MOD_ID);

    public static final RegistryObject<MobEffect> COOLING = register("cooling",
            () -> new TemperatureChangeEffect(MobEffectCategory.BENEFICIAL, 0xAEBDD));
    public static final RegistryObject<MobEffect> WARMING = register("warming",
            () -> new TemperatureChangeEffect(MobEffectCategory.BENEFICIAL, 0xEDA02D));
    public static final RegistryObject<MobEffect> INSTANT_RADIATION = register("instant_radiation",
            () -> new InstantDamageEffect(MobEffectCategory.HARMFUL, 0x94fc03));

    public static final RegistryObject<MobEffect> CHEMICAL_BURNS_WARNING = register("chemical_burns_warning",
            () -> new MedicalConditionEffect(0xfa8723));
    public static final RegistryObject<MobEffect> POISON_WARNING = register("poison_warning",
            () -> new MedicalConditionEffect(0x81de35));
    public static final RegistryObject<MobEffect> WEAK_POISON_WARNING = register("weak_poison_warning",
            () -> new MedicalConditionEffect(0x88bd5c));
    public static final RegistryObject<MobEffect> IRRITANT_WARNING = register("irritant_warning",
            () -> new MedicalConditionEffect(0xe0ce3f));
    public static final RegistryObject<MobEffect> NAUSEA_WARNING = register("nausea_warning",
            () -> new MedicalConditionEffect(0xa864d9));
    public static final RegistryObject<MobEffect> CARCINOGEN_WARNING = register("carcinogen_warning",
            () -> new MedicalConditionEffect(0xffffff));
    public static final RegistryObject<MobEffect> ASBESTOSIS_WARNING = register("asbestosis_warning",
            () -> new MedicalConditionEffect(0xed5142));
    public static final RegistryObject<MobEffect> ARSENICOSIS_WARNING = register("arsenicosis_warning",
            () -> new MedicalConditionEffect(0x3f66f2));
    public static final RegistryObject<MobEffect> SILICOSIS_WARNING = register("silicosis_warning",
            () -> new MedicalConditionEffect(0x80828c));
    public static final RegistryObject<MobEffect> BERYLLIOSIS_WARNING = register("berylliosis_warning",
            () -> new MedicalConditionEffect(0x39b885));
    public static final RegistryObject<MobEffect> METHANOL_POISONING_WARNING = register("methanol_poisoning_warning",
            () -> new MedicalConditionEffect(0xde599e));
    public static final RegistryObject<MobEffect> CARBON_MONOXIDE_POISONING_WARNING = register("carbon_monoxide_poisoning_warning",
            () -> new MedicalConditionEffect(0x45bad1));

    public static <T extends MobEffect> RegistryObject<T> register(String name, Supplier<T> supplier) {
        return EFFECTS.register(name, supplier);
    }

}
