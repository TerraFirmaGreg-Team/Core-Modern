package su.terrafirmagreg.core.common.food.nutrient;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Handles applying effects to players based on their nutrition values.
 * Effects based on positive & negative nutrients are cached until the next nutrition value change.
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
     * Tick call from TFCFoodDataMixin.
     * @param player the server player.
     * @param nutritionData the player's NutritionData.
     */
    public static void tick(ServerPlayer player, NutritionData nutritionData) {
        processTransientNutrients(player, nutritionData);
        applyNutrientEffects(player, nutritionData);
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

            if (nutrient.getSerializedName().equals("deadly")) {
                player.kill();
            }

            // Reset the transient nutrient to 0 immediately.
            NutritionDataExtension.setExtendedNutrient(nutritionData, nutrient, 0f);
            // Invalidate cache so positive effects are re-evaluated.
            LAST_NUTRITION_SNAPSHOT.remove(nutritionData);
        }
    }

    // ---- Nutrient Effects ----

    /**
     * Applies effects based on positive and negative nutrients.
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

        applyGrainEffects(player, grain);
        applyDairyEffects(player, dairy);
        applyVegetableEffects(player, vegetables);
        applyFruitEffects(player, fruit);
        applyProteinEffects(player, protein);
        applyAverageEffects(player, avgNutrition);

        // Negative Nutrients
        float toxins = 0;
        float parasites = 0;
        float microplastics = 0;
        float oldToxins = 0;
        float oldMicroplastics = 0;
        Nutrient toxinsNutrient = null;

        for (Nutrient nutrient : Nutrient.VALUES) {
            String name = nutrient.name();
            switch (name) {
                case "TOXINS" -> {
                    toxinsNutrient = nutrient;
                    toxins = nutritionData.getNutrient(nutrient);
                    if (oldNutrients != null && nutrient.ordinal() < oldNutrients.length) {
                        oldToxins = oldNutrients[nutrient.ordinal()];
                    }
                }
                case "PARASITES" -> parasites = nutritionData.getNutrient(nutrient);
                case "MICROPLASTICS" -> {
                    microplastics = nutritionData.getNutrient(nutrient);
                    if (oldNutrients != null && nutrient.ordinal() < oldNutrients.length) {
                        oldMicroplastics = oldNutrients[nutrient.ordinal()];
                    }
                }
            }
        }

        if (toxinsNutrient != null) {
            applyToxinsEffects(player, toxins, oldToxins, toxinsNutrient, nutritionData);
        }
        applyParasitesEffects(player, parasites);
        applyMicroplasticsEffects(player, microplastics, oldMicroplastics);
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
            player.kill();
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
     * >55% -> increase passive health regen by 50%.
     * >85% -> increase passive health regen by 100%.
     * @param player the player to apply effects to.
     * @param avg the average nutrition across all positive nutrients.
     */
    private static void applyAverageEffects(Player player, float avg) {
        UUID uuid = player.getUUID();
        if (avg > 0.85f) {
            HEALING_MODIFIER.put(uuid, 2.0f);
        } else if (avg > 0.55f) {
            HEALING_MODIFIER.put(uuid, 1.5f);
        } else {
            HEALING_MODIFIER.remove(uuid);
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
    public static void removePlayer(Player player) {
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
