package su.terrafirmagreg.core.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.effects.InstantDamageEffect;
import su.terrafirmagreg.core.common.data.effects.TemperatureChangeEffect;

import java.util.function.Supplier;

public class TFGEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TFGCore.MOD_ID);

    public static final RegistryObject<MobEffect> COOLING = register("cooling", () -> new TemperatureChangeEffect(MobEffectCategory.BENEFICIAL, 0xAEBDD)) ;
    public static final RegistryObject<MobEffect> WARMING = register("warming", () -> new TemperatureChangeEffect(MobEffectCategory.BENEFICIAL, 0xEDA02D));
    public static final RegistryObject<MobEffect> INSTANT_RADIATION = register("instant_radiation", () -> new InstantDamageEffect(MobEffectCategory.HARMFUL, 0x94fc03));


    public static <T extends MobEffect> RegistryObject<T> register(String name, Supplier<T> supplier)
    {
        return EFFECTS.register(name, supplier);
    }

}
