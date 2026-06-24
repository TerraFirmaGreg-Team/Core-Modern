package su.terrafirmagreg.core.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.loot.conditions.AverageRainfallLootCondition;
import su.terrafirmagreg.core.common.loot.conditions.AverageTemperatureLootCondition;
import su.terrafirmagreg.core.common.loot.conditions.GravityLootCondition;
import su.terrafirmagreg.core.common.loot.conditions.MonthLootCondition;
import su.terrafirmagreg.core.common.loot.conditions.OxygenatedLootCondition;
import su.terrafirmagreg.core.common.loot.conditions.SeasonLootCondition;

/**
 * Registry for custom loot conditions used in TFG.
 */
public class TFGLootConditions {

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, TFGCore.MOD_ID);

    public static final RegistryObject<LootItemConditionType> OXYGENATED_LOOT_CONDITION = LOOT_CONDITIONS.register("oxygenated",
            () -> new LootItemConditionType(new OxygenatedLootCondition.ConditionSerializer()));

    public static final RegistryObject<LootItemConditionType> MONTH_LOOT_CONDITION = LOOT_CONDITIONS.register("months",
            () -> new LootItemConditionType(new MonthLootCondition.ConditionSerializer()));

    public static final RegistryObject<LootItemConditionType> SEASON_LOOT_CONDITION = LOOT_CONDITIONS.register("seasons",
            () -> new LootItemConditionType(new SeasonLootCondition.ConditionSerializer()));

    public static final RegistryObject<LootItemConditionType> AVERAGE_TEMPERATURE_LOOT_CONDITION = LOOT_CONDITIONS.register("climate_avg_temperature",
            () -> new LootItemConditionType(new AverageTemperatureLootCondition.ConditionSerializer()));

    public static final RegistryObject<LootItemConditionType> AVERAGE_RAINFALL_LOOT_CONDITION = LOOT_CONDITIONS.register("climate_avg_rainfall",
            () -> new LootItemConditionType(new AverageRainfallLootCondition.ConditionSerializer()));

    public static final RegistryObject<LootItemConditionType> GRAVITY_LOOT_CONDITION = LOOT_CONDITIONS.register("gravity",
            () -> new LootItemConditionType(new GravityLootCondition.ConditionSerializer()));
}
