package su.terrafirmagreg.core.mixins.common.tfc.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.outputs.MealModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to extend MealModifier.apply() to support custom nutrients.
 */
@Mixin(MealModifier.class)
public abstract class MealModifierMixin {

    @Shadow(remap = false)
    @Final
    private FoodData baseFood;

    @Shadow(remap = false)
    @Final
    private List<MealModifier.MealPortion> portions;

    /**
     * Overwritten apply() that handles both positive and negative nutrients.
     * @author Redeix
     * @reason Support negative nutrients in meals.
     */
    @Overwrite(remap = false)
    public ItemStack apply(ItemStack stack, ItemStack input) {
        final @Nullable IFood inputFood = FoodCapability.get(stack);
        if (!(inputFood instanceof FoodHandler.Dynamic handler)) {
            return stack;
        }

        final List<ItemStack> itemIngredients = new ArrayList<>();
        for (final ItemStack item : RecipeHelpers.getCraftingInput()) {
            if (FoodCapability.has(item)) {
                boolean alreadyAdded = false;
                for (ItemStack existing : itemIngredients) {
                    if (existing.getItem() == item.getItem()) {
                        existing.grow(1);
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    final ItemStack tooltipItem = item.copyWithCount(1);
                    FoodCapability.setNeverExpires(tooltipItem);
                    HeatCapability.setTemperature(tooltipItem, 0);
                    itemIngredients.add(tooltipItem);
                }
            }
        }

        if (itemIngredients.isEmpty()) {
            return stack;
        }

        itemIngredients.sort(Comparator.comparing(ItemStack::getCount)
                .thenComparing(item -> BuiltInRegistries.ITEM.getKey(item.getItem())));

        float[] nutrition = baseFood.nutrients();
        float saturation = baseFood.saturation();
        float water = baseFood.water();

        float[] negativeNutrients = new float[TFGNutrients.getExtendedCount()];
        float[] baseExtended = FoodDataExtension.getExtendedNutrients(baseFood);
        if (baseExtended.length > 0) {
            System.arraycopy(baseExtended, 0, negativeNutrients, 0, Math.min(baseExtended.length, negativeNutrients.length));
        }

        final Map<ItemStack, MealModifier.MealPortion> map = new HashMap<>();
        for (ItemStack ingredient : itemIngredients) {
            MealModifier.MealPortion selected = null;
            for (MealModifier.MealPortion portion : portions) {
                if (portion.test(ingredient)) {
                    selected = portion;
                    break;
                }
            }
            if (selected != null)
                map.put(ingredient, selected);
        }

        for (Map.Entry<ItemStack, MealModifier.MealPortion> entry : map.entrySet()) {
            final ItemStack item = entry.getKey();
            final MealModifier.MealPortion portion = entry.getValue();
            final @Nullable IFood food = FoodCapability.get(item);
            if (food != null) {
                final var data = food.getData();
                for (Nutrient nutrient : Nutrient.VALUES) {
                    float value = data.nutrient(nutrient) * portion.nutrientModifier() * item.getCount();

                    if (TFGNutrients.isPositive(nutrient)) {
                        // Positive nutrients go to the array.
                        nutrition[nutrient.ordinal()] += value;
                    } else {
                        // Negative nutrients accumulate separately.
                        int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
                        if (index >= 0 && index < negativeNutrients.length) {
                            negativeNutrients[index] += value;
                        }
                    }
                }
                water += data.water() * portion.waterModifier() * item.getCount();
                saturation += data.saturation() * portion.saturationModifier() * item.getCount();
            }
        }

        FoodData createdFood = FoodData.create(baseFood.hunger(), water, saturation, nutrition, baseFood.decayModifier());

        // Always set extended nutrients if there are any.
        if (negativeNutrients.length > 0) {
            FoodDataExtension.setExtendedNutrients(createdFood, negativeNutrients);
        }

        handler.setFood(createdFood);
        handler.setIngredients(itemIngredients);
        handler.setCreationDate(FoodCapability.getRoundedCreationDate());
        return stack;
    }
}
