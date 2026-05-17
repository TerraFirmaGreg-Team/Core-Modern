package su.terrafirmagreg.core.common.food.nutrient;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.minecraft.server.level.ServerPlayer;
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

    public static final UUID DAIRY_SPEED_MODIFIER_UUID = UUID.fromString("49b6a7a4-42da-4b0b-979c-86f0ebb0eb25");
    private static final String DAIRY_SPEED_MODIFIER_NAME = "tfg:dairy_speed_boost";

    /**
     * Tracks the last nutrition update.
     */
    private static final Map<NutritionData, float[]> LAST_NUTRITION_SNAPSHOT = new IdentityHashMap<>();

    /**
     * Processes transient nutrients.
     * @param player the server player.
     * @param nutritionData the player's NutritionData.
     */
    public static void tick(ServerPlayer player, NutritionData nutritionData) {
        processTransientNutrients(player, nutritionData);
        applyPositiveNutrientEffects(player, nutritionData);
    }

    /**
     * Processes transient nutrients.
     */
    private static void processTransientNutrients(ServerPlayer player, NutritionData nutritionData) {
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (!TFGNutrients.isTransient(nutrient))
                continue;

            float value = NutritionDataExtension.getExtendedNutrient(nutritionData, nutrient);
            if (value <= 0)
                continue;

            if (nutrient.getSerializedName().equals("deadly")) {
                int duration = Math.max(1, Math.round(value * 2000));
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 3, false, true));
            }

            // Reset the transient nutrient to 0 immediately.
            NutritionDataExtension.setExtendedNutrient(nutritionData, nutrient, 0f);
            // Invalidate cache so positive effects are re-evaluated.
            LAST_NUTRITION_SNAPSHOT.remove(nutritionData);
        }
    }

    /**
     * Applies effects based on positive nutrients.
     */
    private static void applyPositiveNutrientEffects(ServerPlayer player, NutritionData nutritionData) {
        if (!hasNutritionChanged(nutritionData))
            return;

        updateSnapshot(nutritionData);

        float dairy = nutritionData.getNutrient(Nutrient.DAIRY);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null)
            return;

        boolean hasModifier = speedAttr.getModifier(DAIRY_SPEED_MODIFIER_UUID) != null;

        if (dairy > 0.7f) {
            if (!hasModifier) {
                speedAttr.addTransientModifier(new AttributeModifier(
                        DAIRY_SPEED_MODIFIER_UUID,
                        DAIRY_SPEED_MODIFIER_NAME,
                        1.0,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else {
            if (hasModifier) {
                speedAttr.removeModifier(DAIRY_SPEED_MODIFIER_UUID);
            }
        }
    }

    /**
     * Returns true if the nutrition values have changed since the last update.
     */
    private static boolean hasNutritionChanged(NutritionData nutritionData) {
        float[] snapshot = LAST_NUTRITION_SNAPSHOT.get(nutritionData);
        if (snapshot == null)
            return true;

        for (Nutrient nutrient : Nutrient.VALUES) {
            if (!TFGNutrients.isPositive(nutrient))
                continue;
            int idx = nutrient.ordinal();
            if (idx >= snapshot.length)
                return true;
            if (Math.abs(snapshot[idx] - nutritionData.getNutrient(nutrient)) > 1e-5f)
                return true;
        }
        return false;
    }

    /**
     * Updates the cached nutrition update for the NutritionData.
     */
    private static void updateSnapshot(NutritionData nutritionData) {
        float[] snapshot = new float[TFGNutrients.POSITIVE_COUNT];
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (!TFGNutrients.isPositive(nutrient))
                continue;
            int idx = nutrient.ordinal();
            if (idx < snapshot.length) {
                snapshot[idx] = nutritionData.getNutrient(nutrient);
            }
        }
        LAST_NUTRITION_SNAPSHOT.put(nutritionData, snapshot);
    }

    /**
     * Removes modifiers from a player.
     */
    public static void removePlayer(Player player) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(DAIRY_SPEED_MODIFIER_UUID);
        }
    }

    /**
     * Cleans up cached data when nutrition data is no longer needed.
     */
    public static void remove(NutritionData nutritionData) {
        LAST_NUTRITION_SNAPSHOT.remove(nutritionData);
    }

    private NutrientEffectsHandler() {
    }
}
