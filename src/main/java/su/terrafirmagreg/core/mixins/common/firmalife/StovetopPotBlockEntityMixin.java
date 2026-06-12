/*
 * This file mixins code from Firmalife (https://github.com/eerussianguy/firmalife?tab=MIT-1-ov-file)
 * MIT License
 * Copyright (c) 2022 eerussianguy
 */
package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.eerussianguy.firmalife.common.blockentities.StovetopPotBlockEntity;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.food.DynamicBowlHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.SoupPotRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

// Temporary mixin to fix firmalife soup pot recipe crashes when passing in ingredients with custom nutrient types.
// Copy of the original SoupPotRecipe class with changes commented.
// Why did firmalife copy the recipe logic into the block entity class instead of just calling the tfc recipe????
@Mixin(value = StovetopPotBlockEntity.class, remap = false)
public abstract class StovetopPotBlockEntityMixin extends InventoryBlockEntity<StovetopPotBlockEntity.StovetopPotInventory> {

    @Shadow
    private ItemStack soupStack;

    protected StovetopPotBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state, InventoryFactory<StovetopPotBlockEntity.StovetopPotInventory> inventoryFactory,
            Component defaultName) {
        super(type, pos, state, inventoryFactory, defaultName);
    }

    /**
     * @author Redeix
     * @reason Fix soup pot recipe crashes when passing in ingredients with custom nutrient types.
     */
    @Overwrite
    public void assembleSoup() {
        int ingredientCount = 0;
        float water = 20, saturation = 2;
        float[] nutrition = new float[Nutrient.TOTAL];
        ItemStack soupStack = ItemStack.EMPTY;
        for (int i = 0; i < StovetopPotBlockEntity.SLOTS; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            IFood food = stack.getCapability(FoodCapability.CAPABILITY).resolve().orElse(null);
            if (food != null) {
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
            soupStack.getCapability(FoodCapability.CAPABILITY)
                    .filter(food -> food instanceof DynamicBowlHandler)
                    .ifPresent(food -> {
                        DynamicBowlHandler handler = (DynamicBowlHandler) food;
                        handler.setCreationDate(created);
                        handler.setFood(data);
                    });
        }

        if (!soupStack.isEmpty()) {
            this.soupStack = soupStack;
        }
    }
}
