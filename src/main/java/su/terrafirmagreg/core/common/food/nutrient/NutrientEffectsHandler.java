package su.terrafirmagreg.core.common.food.nutrient;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.dries007.tfc.common.TFCEffects;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.data.TFGEffects;
import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Handles applying effects to players based on their nutrition values.
 * Effects based on positive & extended nutrients are cached until the next nutrition value change.
 * Transient nutrients are consumed immediately and trigger instant effects.
 */
public final class NutrientEffectsHandler {

    public static final UUID GRAIN_SPEED_MODIFIER_UUID = UUID.fromString("49b6a7a4-42da-4b0b-979c-86f0ebb0eb25");

    private static final String GRAIN_SPEED_MODIFIER_NAME = "tfg:grain_speed_boost";

    private static final Map<UUID, Float> THIRST_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> MICROPLASTICS_THIRST_DECAY_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> PROTEIN_EXHAUSTION_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> PARASITES_EXHAUSTION_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> HEALING_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> DAIRY_FEATHER_FALLING = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> VEGETABLE_AQUA_AFFINITY = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> VEGETABLE_RESPIRATION = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> FRUIT_MINING_SPEED = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> PROTEIN_HEAVY_ITEM_BOOST = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> MEDICAL_CONDITION_PROGRESSION_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> MEDICAL_CONDITION_HEALING_MODIFIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_DAILY_UPDATE = new ConcurrentHashMap<>();

    private static final boolean ENABLE_FOOD_DEBUFFS = TFGConfig.SERVER.enableTFGFoodDebuffs.get();
    private static final boolean ENABLE_FOOD_BUFFS = TFGConfig.SERVER.enableTFGFoodBuffs.get();

    /**
     * List of hazard effects that can be applied from toxins.
     */
    private static final MobEffect[] HAZARDOUS_EFFECTS = {
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.CONFUSION,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.BLINDNESS,
            MobEffects.DIG_SLOWDOWN
    };

    /**
     * Tracks the last nutrition update to avoid re-applying unchanged effects.
     */
    private static final Map<NutritionData, float[]> LAST_NUTRITION_SNAPSHOT = new IdentityHashMap<>();

    /**
     * Returns the thirst modifier multiplier for the player.
     */
    public static float getThirstModifierMultiplier(UUID playerUuid) {
        return THIRST_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Returns the microplastics high-temperature thirst decay modifier for the player.
     */
    public static float getMicroplasticsThirstTemperatureModifier(UUID playerUuid) {
        return MICROPLASTICS_THIRST_DECAY_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Returns the protein exhaustion modifier multiplier for the player.
     */
    public static float getProteinExhaustionMultiplier(UUID playerUuid) {
        return PROTEIN_EXHAUSTION_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Returns the parasites passive exhaustion modifier for the player.
     */
    public static float getParasitesPassiveExhaustionModifier(UUID playerUuid) {
        return PARASITES_EXHAUSTION_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Returns the passive healing multiplier for the player.
     */
    public static float getHealingModifierMultiplier(UUID playerUuid) {
        return HEALING_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Returns the fake Feather Falling enchantment level for the player.
     */
    public static int getFeatherFallingLevel(UUID playerUuid) {
        return DAIRY_FEATHER_FALLING.getOrDefault(playerUuid, 0);
    }

    /**
     * Returns the fake Aqua Affinity enchantment level for the player.
     */
    public static int getAquaAffinityLevel(UUID playerUuid) {
        return VEGETABLE_AQUA_AFFINITY.getOrDefault(playerUuid, 0);
    }

    /**
     * Returns the fake Respiration enchantment level for the player.
     */
    public static int getRespirationLevel(UUID playerUuid) {
        return VEGETABLE_RESPIRATION.getOrDefault(playerUuid, 0);
    }

    /**
     * Returns whether the player has the fruit mining speed boost active.
     */
    public static boolean hasFruitMiningSpeedBoost(UUID playerUuid) {
        return FRUIT_MINING_SPEED.getOrDefault(playerUuid, false);
    }

    /**
     * Returns whether the player has the protein heavy item limit boost active.
     */
    public static boolean hasProteinHeavyItemBoost(UUID playerUuid) {
        return PROTEIN_HEAVY_ITEM_BOOST.getOrDefault(playerUuid, false);
    }

    /**
     * Returns the medical condition progression modifier for the player.
     */
    public static float getMedicalConditionProgressionModifier(UUID playerUuid) {
        return MEDICAL_CONDITION_PROGRESSION_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Returns the medical condition healing modifier for the player.
     */
    public static float getMedicalConditionHealingModifier(UUID playerUuid) {
        return MEDICAL_CONDITION_HEALING_MODIFIER.getOrDefault(playerUuid, 1.0f);
    }

    /**
     * Tick call from TFCFoodDataMixin.
     * @param player the server player.
     * @param nutritionData the player's NutritionData.
     */
    public static void tick(ServerPlayer player, NutritionData nutritionData) {
        processTransientNutrients(player, nutritionData);
        applyNutrientEffects(player, nutritionData);
        handleDailyEffects(player, nutritionData);
    }

    /**
     * Handles once per day effects.
     * @param player the player to apply effects to.
     * @param nutritionData the nutrition data to evaluate.
     */
    private static void handleDailyEffects(ServerPlayer player, NutritionData nutritionData) {
        long currentDay = Calendars.get(player.level()).getTotalDays();
        UUID uuid = player.getUUID();
        Long lastUpdateDay = LAST_DAILY_UPDATE.get(uuid);

        if (lastUpdateDay == null || lastUpdateDay != currentDay) {
            LAST_DAILY_UPDATE.put(uuid, currentDay);

            // Toxins
            // Decrease by 3% each day.
            Nutrient toxinsNutrient = TFGNutrients.getByName("TOXINS");
            if (toxinsNutrient != null) {
                float toxins = NutritionDataExtension.getExtendedNutrient(nutritionData, toxinsNutrient);
                if (toxins > 0) {
                    NutritionDataExtension.setExtendedNutrient(nutritionData, toxinsNutrient, toxins - 0.03f);
                }
            }

            // Microplastics
            // Decrease by 1% each day.
            Nutrient microplasticsNutrient = TFGNutrients.getByName("MICROPLASTICS");
            if (microplasticsNutrient != null) {
                float microplastics = NutritionDataExtension.getExtendedNutrient(nutritionData, microplasticsNutrient);
                if (microplastics > 0) {
                    NutritionDataExtension.setExtendedNutrient(nutritionData, microplasticsNutrient, microplastics - 0.01f);
                }
            }

            // Parasites
            // >10% -> 20% chance to increase by 10% daily to a max of 60% parasites.
            Nutrient parasitesNutrient = TFGNutrients.getByName("PARASITES");
            if (parasitesNutrient != null) {
                float parasites = NutritionDataExtension.getExtendedNutrient(nutritionData, parasitesNutrient);
                if (parasites > 0.1f) {
                    if (player.getRandom().nextFloat() < 0.20f) {
                        float newParasites = Math.min(0.6f, parasites + 0.1f);
                        if (newParasites != parasites) {
                            NutritionDataExtension.setExtendedNutrient(nutritionData, parasitesNutrient, newParasites);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called on the client side when nutrients are updated.
     */
    public static void onClientUpdate(Player player, NutritionData nutritionData) {
        applyNutrientEffects(player, nutritionData);
    }

    // ---- Transient Nutrients ----

    /**
     * Applies effects based on transient nutrients.
     * @param player the player to apply effects to.
     * @param nutritionData the nutrition data to evaluate.
     */
    private static void processTransientNutrients(ServerPlayer player, NutritionData nutritionData) {
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (!TFGNutrients.isTransient(nutrient))
                continue;

            float value = NutritionDataExtension.getExtendedNutrient(nutritionData, nutrient);
            if (value <= 0)
                continue;

            // Food value to seconds of effect duration.
            int effectDuration = Math.max(1, Math.round(value * 1200));

            switch (nutrient.getSerializedName()) {
                case "deadly" -> {
                    if (!player.getAbilities().invulnerable && ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.FINAL_MOMENTS.get(), Math.max((int) (9600 / (value)), 600), 0, true, true));
                    }
                }
                case "cooling" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.COOLING.get(), Math.min(effectDuration, 36000), 0, false, false));
                    }
                }

                case "warming" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.WARMING.get(), Math.min(effectDuration, 36000), 0, false, false));
                    }
                }

                case "freezing" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.FREEZING.get(), Math.min(effectDuration / 12, 600), 0, false, true));
                    }
                }

                case "blazing" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.BLAZING.get(), Math.min(effectDuration / 12, 600), 0, false, true));
                    }
                }

                case "radiating" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 480, 0, false, true));
                        player.addEffect(new MobEffectInstance(TFGEffects.INSTANT_RADIATION.get(), 480, (int) ((value * 5) / 100), false, true));
                    }
                }
                case "nauseating" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(effectDuration / 12, 240), 0, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, Math.min(effectDuration / 6, 600), 6, false, true));
                    }
                }
                case "parching" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(TFCEffects.THIRST.get(), Math.min(effectDuration, 9600), 0, false, true));
                    }
                }

                case "quenching" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.QUENCHED.get(), Math.min(effectDuration, 36000), 0, false, false));
                    }
                }

                case "bolstering" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 9600, (int) ((value * 5) / 100), false, false));
                    }
                }

                case "hearty" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 9600, (int) ((value * 5) / 100), false, false));
                    }
                }

                case "rejuvenating" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Math.min(effectDuration, 3600), 0, false, false));
                    }
                }

                case "sugary" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, Math.min(effectDuration / 4, 6000), 0, false, false));
                    }
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Math.min(effectDuration, 36000), 0, false, false));
                    }
                }
                case "spicy" -> {
                    if (ENABLE_FOOD_DEBUFFS) {
                        player.addEffect(new MobEffectInstance(TFCEffects.THIRST.get(), Math.min(effectDuration / 4, 6000), 0, false, false));
                    }
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(TFGEffects.WARMING.get(), Math.min(effectDuration, 36000), 0, false, false));
                    }
                }
                case "fulfilling" -> {
                    if (ENABLE_FOOD_BUFFS) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Math.min(effectDuration, 18000), 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, (int) ((value * 2) / 100), false, false));
                    }
                }
                default -> {
                }
            }

            // Reset the transient nutrient to 0 immediately.
            NutritionDataExtension.setExtendedNutrient(nutritionData, nutrient, 0f);
        }
    }

    // ---- Nutrient Effects ----

    /**
     * Applies effects based on positive and extended nutrients.
     * @param player the player to apply effects to.
     * @param nutritionData the nutrition data to evaluate.
     */
    private static void applyNutrientEffects(Player player, NutritionData nutritionData) {
        float[] oldNutrients = LAST_NUTRITION_SNAPSHOT.get(nutritionData);
        if (!hasNutritionChanged(nutritionData))
            return;

        updateSnapshot(nutritionData);

        float grain = nutritionData.getNutrient(Nutrient.GRAIN);
        float dairy = nutritionData.getNutrient(Nutrient.DAIRY);
        float vegetables = nutritionData.getNutrient(Nutrient.VEGETABLES);
        float fruit = nutritionData.getNutrient(Nutrient.FRUIT);
        float protein = nutritionData.getNutrient(Nutrient.PROTEIN);

        // Average nutrition across all positive nutrients.
        float avgNutrition = 0f;
        int positiveCount = 0;
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isPositive(nutrient)) {
                avgNutrition += nutritionData.getNutrient(nutrient);
                positiveCount++;
            }
        }
        if (positiveCount > 0)
            avgNutrition /= positiveCount;

        if (ENABLE_FOOD_BUFFS) {
            applyGrainEffects(player, grain);
            applyDairyEffects(player, dairy);
            applyVegetableEffects(player, vegetables);
            applyFruitEffects(player, fruit);
            applyProteinEffects(player, protein);
            applyAverageEffects(player, avgNutrition);
        }

        // Negative Nutrients
        float toxins = 0;
        float parasites = 0;
        float microplastics = 0;
        Nutrient toxinsNutrient = TFGNutrients.getByName("TOXINS");
        Nutrient parasitesNutrient = TFGNutrients.getByName("PARASITES");
        Nutrient microplasticsNutrient = TFGNutrients.getByName("MICROPLASTICS");

        float oldToxins = 0;
        float oldMicroplastics = 0;

        if (toxinsNutrient != null) {
            toxins = nutritionData.getNutrient(toxinsNutrient);
            oldToxins = toxins;
            if (oldNutrients != null && toxinsNutrient.ordinal() < oldNutrients.length) {
                oldToxins = oldNutrients[toxinsNutrient.ordinal()];
            }
        }

        if (parasitesNutrient != null) {
            parasites = nutritionData.getNutrient(parasitesNutrient);
        }

        if (microplasticsNutrient != null) {
            microplastics = nutritionData.getNutrient(microplasticsNutrient);
            oldMicroplastics = microplastics;
            if (oldNutrients != null && microplasticsNutrient.ordinal() < oldNutrients.length) {
                oldMicroplastics = oldNutrients[microplasticsNutrient.ordinal()];
            }
        }

        if (ENABLE_FOOD_DEBUFFS) {
            if (toxinsNutrient != null) {
                applyToxinsEffects(player, toxins, oldToxins, toxinsNutrient, nutritionData);
            }
            applyParasitesEffects(player, parasites);
            applyMicroplasticsEffects(player, microplastics, oldMicroplastics);
        }
    }

    /**
     * Grain
     * >55% -> 5% speed buff.
     * >85% -> 15% speed buff.
     * @param player the player to apply effects to.
     * @param grain the grain nutrition value.
     */
    private static void applyGrainEffects(Player player, float grain) {
        if (player.level().isClientSide()) {
            return;
        }
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null)
            return;

        speedAttr.removeModifier(GRAIN_SPEED_MODIFIER_UUID);

        if (grain > 0.85f) {
            speedAttr.addTransientModifier(new AttributeModifier(
                    GRAIN_SPEED_MODIFIER_UUID,
                    GRAIN_SPEED_MODIFIER_NAME,
                    0.15,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (grain > 0.55f) {
            speedAttr.addTransientModifier(new AttributeModifier(
                    GRAIN_SPEED_MODIFIER_UUID,
                    GRAIN_SPEED_MODIFIER_NAME,
                    0.05,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    /**
     * Dairy
     * >55% -> feather falling 1.
     * >85% -> feather falling 4.
     * @param player the player to apply effects to.
     * @param dairy the dairy nutrition value.
     */
    private static void applyDairyEffects(Player player, float dairy) {
        UUID uuid = player.getUUID();
        if (dairy > 0.85f) {
            DAIRY_FEATHER_FALLING.put(uuid, 4);
        } else if (dairy > 0.55f) {
            DAIRY_FEATHER_FALLING.put(uuid, 1);
        } else {
            DAIRY_FEATHER_FALLING.remove(uuid);
        }
    }

    /**
     * Vegetables
     * >55% -> aqua affinity 1.
     * >85% -> aqua affinity 1 + respiration 2.
     * @param player the player to apply effects to.
     * @param vegetables the vegetable nutrition value.
     */
    private static void applyVegetableEffects(Player player, float vegetables) {
        UUID uuid = player.getUUID();
        if (vegetables > 0.85f) {
            VEGETABLE_AQUA_AFFINITY.put(uuid, 1);
            VEGETABLE_RESPIRATION.put(uuid, 2);
        } else if (vegetables > 0.55f) {
            VEGETABLE_AQUA_AFFINITY.put(uuid, 1);
            VEGETABLE_RESPIRATION.remove(uuid);
        } else {
            VEGETABLE_AQUA_AFFINITY.remove(uuid);
            VEGETABLE_RESPIRATION.remove(uuid);
        }
    }

    /**
     * Fruit
     * >55% -> decrease thirst modifier by 25%.
     * >85% -> increases base mining speed by 30%.
     * @param player the player to apply effects to.
     * @param fruit the fruit nutrition value.
     */
    private static void applyFruitEffects(Player player, float fruit) {
        UUID uuid = player.getUUID();
        if (fruit > 0.55f) {
            THIRST_MODIFIER.put(uuid, 0.75f);
        } else {
            THIRST_MODIFIER.remove(uuid);
        }
        if (fruit > 0.85f) {
            FRUIT_MINING_SPEED.put(uuid, true);
        } else {
            FRUIT_MINING_SPEED.remove(uuid);
        }
    }

    /**
     * Protein
     * >55% -> decrease exhaustion multiplier by 25%.
     * >85% -> increase hugeHeavy item limit by 1.
     * @param player the player to apply effects to.
     * @param protein the protein nutrition value.
     */
    private static void applyProteinEffects(Player player, float protein) {
        UUID uuid = player.getUUID();
        if (protein > 0.55f) {
            PROTEIN_EXHAUSTION_MODIFIER.put(uuid, 0.75f);
        } else {
            PROTEIN_EXHAUSTION_MODIFIER.remove(uuid);
        }
        if (protein > 0.85f) {
            PROTEIN_HEAVY_ITEM_BOOST.put(uuid, true);
        } else {
            PROTEIN_HEAVY_ITEM_BOOST.remove(uuid);
        }
    }

    /**
     * Toxins
     * >0% -> On any new increase -> 25% chance to get a random hazardous mob effect for 10sec.
     * >99% -> Death.
     * @param player the player to apply effects to.
     * @param toxins the toxins nutrition value.
     * @param oldToxins the toxins nutrition value from the last update.
     */
    private static void applyToxinsEffects(Player player, float toxins, float oldToxins, Nutrient toxinsNutrient, NutritionData nutritionData) {
        if (player.level().isClientSide()) {
            return;
        }

        if (toxins > oldToxins && toxins > 0) {
            if (player.getRandom().nextFloat() < 0.25f) {
                MobEffect effect = HAZARDOUS_EFFECTS[player.getRandom().nextInt(HAZARDOUS_EFFECTS.length)];
                player.addEffect(new MobEffectInstance(effect, 200, 0));
            }
        }

        if (toxins > 0.99f && !player.getAbilities().invulnerable) {
            NutritionDataExtension.setExtendedNutrient(nutritionData, toxinsNutrient, 0.90f);
            player.addEffect(new MobEffectInstance(TFGEffects.FINAL_MOMENTS.get(), 18000, 0, false, true));
        }
    }

    /**
     * Parasites
     * >33% -> Increases passive exhaustion by 50%.
     * >66% -> Increases passive exhaustion by 100%.
     * @param player the player to apply effects to.
     * @param parasites the parasites nutrition value.
     */
    private static void applyParasitesEffects(Player player, float parasites) {
        UUID uuid = player.getUUID();
        if (parasites > 0.66f) {
            PARASITES_EXHAUSTION_MODIFIER.put(uuid, 10.0f);
        } else if (parasites > 0.33f) {
            PARASITES_EXHAUSTION_MODIFIER.put(uuid, 5.0f);
        } else {
            PARASITES_EXHAUSTION_MODIFIER.remove(uuid);
        }
    }

    /**
     * Microplastics
     * >33% -> Increase high-temperature thirst decay multiplier by 25%
     * >66% -> Increase high-temperature thirst decay multiplier by 50%. On any new increase -> 5% chance to get the cancer condition from GTCEU.
     * @param player the player to apply effects to.
     * @param microplastics the microplastics nutrition value.
     * @param oldMicroplastics the microplastics nutrition value from the last update.
     */
    private static void applyMicroplasticsEffects(Player player, float microplastics, float oldMicroplastics) {
        UUID uuid = player.getUUID();
        if (microplastics > 0.66f) {
            MICROPLASTICS_THIRST_DECAY_MODIFIER.put(uuid, 1.50f);
            if (microplastics > oldMicroplastics && player.getRandom().nextFloat() < 0.05f) {
                var tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
                if (tracker != null) {
                    tracker.progressCondition(GTMedicalConditions.CARCINOGEN, 20000.0f);
                }
            }
        } else if (microplastics > 0.33f) {
            MICROPLASTICS_THIRST_DECAY_MODIFIER.put(uuid, 1.25f);
        } else {
            MICROPLASTICS_THIRST_DECAY_MODIFIER.remove(uuid);
        }
    }

    /**
     * Average
     * >55% -> increase passive health regen by 50%. Decrease medical condition progression rate by 25%. Increase medical condition healing rate by 50%.
     * >85% -> increase passive health regen by 100%. Decrease medical condition progression rate by 50%. Increase medical condition healing rate by 100%.
     * @param player the player to apply effects to.
     * @param avg the average nutrition across all positive nutrients.
     */
    private static void applyAverageEffects(Player player, float avg) {
        UUID uuid = player.getUUID();

        if (!ENABLE_FOOD_BUFFS) {
            return;
        }

        if (avg > 0.85f) {
            HEALING_MODIFIER.put(uuid, 2.0f);
            MEDICAL_CONDITION_PROGRESSION_MODIFIER.put(uuid, 0.5f);
            MEDICAL_CONDITION_HEALING_MODIFIER.put(uuid, 2.0f);
        } else if (avg > 0.55f) {
            HEALING_MODIFIER.put(uuid, 1.5f);
            MEDICAL_CONDITION_PROGRESSION_MODIFIER.put(uuid, 0.75f);
            MEDICAL_CONDITION_HEALING_MODIFIER.put(uuid, 1.5f);
        } else {
            HEALING_MODIFIER.remove(uuid);
            MEDICAL_CONDITION_PROGRESSION_MODIFIER.remove(uuid);
            MEDICAL_CONDITION_HEALING_MODIFIER.remove(uuid);
        }
    }

    /**
     * Cache helper to check if the nutrition values have changed since the last update.
     * @param nutritionData the player's NutritionData.
     * @return true if the nutrition values have changed since the last update.
     */
    private static boolean hasNutritionChanged(NutritionData nutritionData) {
        float[] snapshot = LAST_NUTRITION_SNAPSHOT.get(nutritionData);
        if (snapshot == null)
            return true;

        for (Nutrient nutrient : Nutrient.VALUES) {
            int idx = nutrient.ordinal();
            if (idx >= snapshot.length)
                return true;
            if (Math.abs(snapshot[idx] - nutritionData.getNutrient(nutrient)) > 1e-5f)
                return true;
        }
        return false;
    }

    /**
     * Cache helper to update the nutrition snapshot for a player.
     * @param nutritionData the player's NutritionData.
     */
    private static void updateSnapshot(NutritionData nutritionData) {
        float[] snapshot = new float[Nutrient.VALUES.length];
        for (Nutrient nutrient : Nutrient.VALUES) {
            int idx = nutrient.ordinal();
            if (idx < snapshot.length) {
                snapshot[idx] = nutritionData.getNutrient(nutrient);
            }
        }
        LAST_NUTRITION_SNAPSHOT.put(nutritionData, snapshot);
    }

    /**
     * Removes all attribute modifiers and cached multipliers for a player.
     * @param player the player to remove modifiers from.
     */
    public static void removeFromPlayer(Player player) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(GRAIN_SPEED_MODIFIER_UUID);
        }

        UUID uuid = player.getUUID();
        THIRST_MODIFIER.remove(uuid);
        MICROPLASTICS_THIRST_DECAY_MODIFIER.remove(uuid);
        PROTEIN_EXHAUSTION_MODIFIER.remove(uuid);
        PARASITES_EXHAUSTION_MODIFIER.remove(uuid);
        HEALING_MODIFIER.remove(uuid);
        DAIRY_FEATHER_FALLING.remove(uuid);
        VEGETABLE_AQUA_AFFINITY.remove(uuid);
        VEGETABLE_RESPIRATION.remove(uuid);
        FRUIT_MINING_SPEED.remove(uuid);
        PROTEIN_HEAVY_ITEM_BOOST.remove(uuid);
        MEDICAL_CONDITION_PROGRESSION_MODIFIER.remove(uuid);
        MEDICAL_CONDITION_HEALING_MODIFIER.remove(uuid);
        LAST_DAILY_UPDATE.remove(uuid);
    }

    /**
     * Cleans up cached snapshot data when nutrition data is no longer needed.
     * @param nutritionData the NutritionData to remove snapshot for.
     */
    public static void remove(NutritionData nutritionData) {
        LAST_NUTRITION_SNAPSHOT.remove(nutritionData);
    }

    private NutrientEffectsHandler() {
    }
}
