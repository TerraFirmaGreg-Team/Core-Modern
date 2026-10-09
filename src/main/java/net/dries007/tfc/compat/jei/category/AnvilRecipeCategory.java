/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jei.category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;

import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings("removal")
public class AnvilRecipeCategory extends BaseRecipeCategory<AnvilRecipe>
{
    private static final int WIDTH = 138;
    private static final int HEIGHT = 56;
    private static final int OFFSET = 6;
    private static final int SLOT_SIZE = 18;

    public static final ResourceLocation ANVIl_BANNER = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/anvil_banner.png");
    public static final int BANNER_WIDTH = WIDTH;
    public static final int BANNER_HEIGHT = 26;

    public static final ResourceLocation PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/hammer_progress_bar.png");
    public static final int PB_WIDTH = 32;
    public static final int PB_HEIGHT = 20;

    private final IDrawableStatic anvilBanner;

    private final IDrawableStatic hammerBg;
    private final IDrawableStatic hammerFg;

    public AnvilRecipeCategory(RecipeType<AnvilRecipe> type, IGuiHelper helper)
    {
        super(type, helper, helper.createBlankDrawable(WIDTH, HEIGHT), new ItemStack(TFCBlocks.METALS.get(Metal.Default.BRONZE).get(Metal.BlockType.ANVIL).get()));

        this.anvilBanner = helper.drawableBuilder(ANVIl_BANNER, 0, 0, BANNER_WIDTH, BANNER_HEIGHT)
                .setTextureSize(BANNER_WIDTH, BANNER_HEIGHT)
                .build();

        this.hammerBg = helper.drawableBuilder(PROGRESS_BAR, 0, 0, PB_WIDTH, PB_HEIGHT)
                .setTextureSize(PB_WIDTH * 2, PB_HEIGHT)
                .build();
        this.hammerFg = helper.drawableBuilder(PROGRESS_BAR, PB_WIDTH, 0, PB_WIDTH, PB_HEIGHT)
                .setTextureSize(PB_WIDTH * 2, PB_HEIGHT)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilRecipe recipe, IFocusGroup focuses)
    {
        // Anvil Slot.
        IRecipeSlotBuilder anvilSlot = builder.addSlot(RecipeIngredientRole.CATALYST, OFFSET * 2 + 5, OFFSET);
        anvilSlot.addItemStacks(getAnvilItemsForTier(recipe.getMinTier()));
        anvilSlot.setBackground(slot, -1, -1);
        anvilSlot.addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("tfc.tooltip.anvil_tier_required", Helpers.translateEnum(Metal.Tier.valueOf(recipe.getMinTier())))));

        // Input Slot.
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, OFFSET, SLOT_SIZE * 2);
        inputSlot.addIngredients(recipe.getInput());
        inputSlot.setBackground(slot, -1, -1);

        // Output Slot.
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - SLOT_SIZE - OFFSET, SLOT_SIZE * 2);
        outputSlot.addItemStack(recipe.getResultItem(registryAccess()));
        outputSlot.setBackground(slot, -1, -1);
    }

    @Override
    public void draw(AnvilRecipe recipe, IRecipeSlotsView recipeSlots, GuiGraphics graphics, double mouseX, double mouseY)
    {
        // Anvil Banner Image.
        this.anvilBanner.draw(graphics, 0, 2);

        // Anvil Rule Icons.
        AnvilScreen.drawRules(graphics, recipe.getRules(), (WIDTH / 2) - 29, (OFFSET / 2) + 3);

        // Animated Hammer.
        long currentSecond = System.currentTimeMillis() / 1000L;
        int hammerX = (WIDTH / 2) - (PB_WIDTH / 2);
        int hammerY = BANNER_HEIGHT + OFFSET;
        if (currentSecond % 2 == 0) {
            this.hammerBg.draw(graphics, hammerX, hammerY);
        } else {
            this.hammerFg.draw(graphics, hammerX, hammerY);
        }
    }

    // Add tooltips for each rule.
    @Override
    public List<Component> getTooltipStrings(AnvilRecipe recipe, IRecipeSlotsView recipeSlots, double mouseX, double mouseY)
    {
        final ForgeRule rule = AnvilScreen.getHoveredRule(recipe.getRules(), (WIDTH / 2) - 29, (OFFSET / 2) + 3, mouseX, mouseY);
        if (rule != null)
        {
            return List.of(rule.getDescriptionId());
        }
        return List.of();
    }

    /**
     * Returns a list of anvils for the given tier.
     * @param targetTier Integer tier for {@link Metal.Tier}. If <= 0, returns stone anvils.
     * @return ItemStack list of anvils belonging to that tier.
     */
    public static List<ItemStack> getAnvilItemsForTier(int targetTier)
    {
        List<ItemStack> anvils = new ArrayList<>();
        if (targetTier <= 0)
        {
            var tags = ForgeRegistries.BLOCKS.tags();
            if (tags != null) {
                return tags.getTag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "rock_anvils")))
                        .stream()
                        .filter(block -> block.asItem() != Items.AIR)
                        .map(ItemStack::new)
                        .collect(Collectors.toList());
            }
        }

        Metal.Tier metalTier = Metal.Tier.valueOf(targetTier);

        for (Metal.Default defaultMetal : Metal.Default.values())
        {
            if (defaultMetal.metalTier() == metalTier && Metal.BlockType.ANVIL.has(defaultMetal))
            {
                var metalMap = TFCBlocks.METALS.get(defaultMetal);
                if (metalMap != null)
                {
                    var anvilBlockSupplier = metalMap.get(Metal.BlockType.ANVIL);
                    if (anvilBlockSupplier != null)
                    {
                        anvils.add(new ItemStack(anvilBlockSupplier.get().asItem()));
                    }
                }
            }
        }

        return anvils;
    }
}
