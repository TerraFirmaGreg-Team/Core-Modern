/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.dries007.tfc.util.Helpers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.dries007.tfc.util.Metal;
import su.terrafirmagreg.core.TFGCore;

import static net.dries007.tfc.compat.jei.category.AnvilRecipeCategory.getAnvilItemsForTier;

@SuppressWarnings("removal")
public class WeldingRecipeCategory extends BaseRecipeCategory<WeldingRecipe>
{
    private static final int WIDTH = 138;
    private static final int HEIGHT = 45;
    private static final int OFFSET = 6;
    private static final int SLOT_SIZE = 18;

    public static final ResourceLocation RAISED_SLOT = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/raised_slot.png");
    public static final int RAISED_SLOT_SIZE = 22;

    private final IDrawableStatic raisedSlot;

    private final IDrawableStatic hammerBg;
    private final IDrawableStatic hammerFg;

    public WeldingRecipeCategory(RecipeType<WeldingRecipe> type, IGuiHelper helper)
    {
        super(type, helper, helper.createBlankDrawable(WIDTH, HEIGHT), new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.WROUGHT_IRON).get(Metal.ItemType.HAMMER).get()));

        this.raisedSlot = helper.drawableBuilder(RAISED_SLOT, 0, 0, RAISED_SLOT_SIZE, RAISED_SLOT_SIZE)
                .setTextureSize(RAISED_SLOT_SIZE, RAISED_SLOT_SIZE)
                .build();

        this.hammerBg = helper.drawableBuilder(AnvilRecipeCategory.PROGRESS_BAR, 0, 0, AnvilRecipeCategory.PB_WIDTH, AnvilRecipeCategory.PB_HEIGHT)
                .setTextureSize(AnvilRecipeCategory.PB_WIDTH * 2, AnvilRecipeCategory.PB_HEIGHT)
                .build();
        this.hammerFg = helper.drawableBuilder(AnvilRecipeCategory.PROGRESS_BAR, AnvilRecipeCategory.PB_WIDTH, 0, AnvilRecipeCategory.PB_WIDTH, AnvilRecipeCategory.PB_HEIGHT)
                .setTextureSize(AnvilRecipeCategory.PB_WIDTH * 2, AnvilRecipeCategory.PB_HEIGHT)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WeldingRecipe recipe, IFocusGroup focuses)
    {

        int slotX = OFFSET;
        int slotY = (HEIGHT / 2) - (SLOT_SIZE / 2);
        int moveSlot = SLOT_SIZE + 2;

        // Anvil Slot.
        IRecipeSlotBuilder anvilSlot = builder.addSlot(RecipeIngredientRole.CATALYST, slotX, slotY);
        anvilSlot.addItemStacks(getAnvilItemsForTier(recipe.getTier()));
        anvilSlot.setBackground(slot, -1, -1);
        anvilSlot.addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("tfc.tooltip.anvil_tier_required", Helpers.translateEnum(Metal.Tier.valueOf(recipe.getTier())))));

        slotX += moveSlot + 3;

        // Input Slots.
        builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY - (SLOT_SIZE / 2) - 1)
            .addIngredients(recipe.getFirstInput())
            .setBackground(slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY + (SLOT_SIZE / 2) + 1)
            .addIngredients(recipe.getSecondInput())
            .setBackground(slot, -1, -1);

        slotX += moveSlot;

        // Flux Slot.
        builder.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
            .addIngredients(Ingredient.of(TFCTags.Items.FLUX))
            .setBackground(slot, -1, -1);

        slotX += (OFFSET * 3) + SLOT_SIZE + AnvilRecipeCategory.PB_WIDTH;

        // Output Slot.
        builder.addSlot(RecipeIngredientRole.OUTPUT, slotX, slotY)
            .addItemStack(recipe.getResultItem(registryAccess()))
            .setBackground(slot, -1, -1);
    }

    @Override
    public void draw(WeldingRecipe recipe, IRecipeSlotsView recipeSlots, GuiGraphics graphics, double mouseX, double mouseY)
    {
        // Raised Slot.
        this.raisedSlot.draw(graphics, OFFSET - 3, (HEIGHT / 2) - (SLOT_SIZE / 2) - 3);

        // Animated Hammer.
        long currentSecond = System.currentTimeMillis() / 1000L;
        int hammerX = (WIDTH / 2) + OFFSET;
        int hammerY = (HEIGHT / 2) - (AnvilRecipeCategory.PB_HEIGHT / 2) - 2;
        if (currentSecond % 2 == 0) {
            this.hammerBg.draw(graphics, hammerX, hammerY);
        } else {
            this.hammerFg.draw(graphics, hammerX, hammerY);
        }
    }
}
