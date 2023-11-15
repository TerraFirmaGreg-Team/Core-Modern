package exception.terrafirmagreg.compat.jei;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.compat.tfc.TFCCompat;
import exception.terrafirmagreg.objects.recipe.TapExtractRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

import static exception.terrafirmagreg.TerraFirmaGreg.MOD_ID;

public class TapExtractRecipeCategory implements IRecipeCategory<TapExtractRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public TapExtractRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation(MOD_ID, "textures/gui/jei/tap_extract.png");
        this.background = guiHelper.createDrawable(location, 0, 0, 126, 70);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TerraFirmaGreg.TAP_ITEM.get()));
    }

    @Override
    public RecipeType<TapExtractRecipe> getRecipeType() {
        return TreeTapJeiPlugin.TAP_EXTRACT_TYPE;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("jei.treetap.tap_extract");
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TapExtractRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 18, 27)
                .addIngredients(VanillaTypes.ITEM_STACK, List.of(recipe.input.getItems()))
                .setSlotName("log");
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 27)
                .addIngredients(VanillaTypes.ITEM_STACK, List.of(recipe.itemOutput.copy(), recipe.woodenItemOutput.copy()))
                .setSlotName("output");

        recipe.itemOutput.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(h -> {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                    .addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(h.getFluidInTank(0), h.getTankCapacity(0)));
        });
    }

    @Override
    public void draw(TapExtractRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        TFCCompat.showRecipeText(guiGraphics, recipe);
    }
}
