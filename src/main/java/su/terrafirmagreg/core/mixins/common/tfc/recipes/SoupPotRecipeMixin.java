/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.common.tfc.recipes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.capabilities.food.DynamicBowlHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.SoupPotRecipe;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

// Temporary mixin to fix soup pot recipe crashes when passing in ingredients with custom nutrient types.
// Copy of the original SoupPotRecipe class with changes commented.
@Mixin(value = SoupPotRecipe.class, remap = false)
public abstract class SoupPotRecipeMixin {

    /**
     * @author Redeix
     * @reason Fix soup pot recipe crashes when passing in ingredients with custom nutrient types.
     */
    @Overwrite
    public SoupPotRecipe.Output getOutput(PotBlockEntity.PotInventory inventory) {
        int ingredientCount = 0;
        float water = 20, saturation = 2;
        float[] nutrition = new float[Nutrient.TOTAL];
        ItemStack soupStack = ItemStack.EMPTY;
        final List<ItemStack> itemIngredients = new ArrayList<>();
        for (int i = PotBlockEntity.SLOT_EXTRA_INPUT_START; i <= PotBlockEntity.SLOT_EXTRA_INPUT_END; i++) {
            final ItemStack stack = inventory.getStackInSlot(i);
            final @Nullable IFood food = FoodCapability.get(stack);
            if (food != null) {
                itemIngredients.add(stack);
                if (food.isRotten()) {
                    ingredientCount = 0;
                    break;
                }
                final FoodData data = food.getData();
                water += data.water();
                saturation += data.saturation();
                for (Nutrient nutrient : Nutrient.VALUES) {
                    nutrition[nutrient.ordinal()] += data.nutrient(nutrient);
                }
                ingredientCount++;
            }
        }
        if (ingredientCount > 0) {
            float multiplier = 1 - (0.05f * ingredientCount);
            water *= multiplier;
            saturation *= multiplier;
            Nutrient maxNutrient = Nutrient.GRAIN;
            float maxNutrientValue = 0;
            for (Nutrient nutrient : Nutrient.VALUES) {
                final int idx = nutrient.ordinal();
                nutrition[idx] *= multiplier;
                // Change nutrient check to original 5 nutrients.
                if (TFGNutrients.isOriginal(nutrient) && nutrition[idx] > maxNutrientValue) {
                    maxNutrientValue = nutrition[idx];
                    maxNutrient = nutrient;
                }
            }
            FoodData data = FoodData.create(SoupPotRecipe.SOUP_HUNGER_VALUE, water, saturation, nutrition, SoupPotRecipe.SOUP_DECAY_MODIFIER);

            // Handle extended nutrients.
            int extendedCount = TFGNutrients.getExtendedCount();
            if (extendedCount > 0) {
                float[] extended = new float[extendedCount];
                System.arraycopy(nutrition, TFGNutrients.ORIGINAL_COUNT, extended, 0, Math.min(nutrition.length - TFGNutrients.ORIGINAL_COUNT, extended.length));
                FoodDataExtension.setExtendedNutrients(data, extended);
            }

            int servings = (int) (ingredientCount / 2f) + 1;
            long created = FoodCapability.getRoundedCreationDate();

            soupStack = new ItemStack(TFCItems.SOUPS.get(maxNutrient).get(), servings);
            final @Nullable IFood food = FoodCapability.get(soupStack);
            if (food instanceof DynamicBowlHandler handler) {
                handler.setCreationDate(created);
                handler.setIngredients(itemIngredients);
                handler.setFood(data);
            }
        }

        return new SoupPotRecipe.SoupOutput(soupStack);
    }
}
