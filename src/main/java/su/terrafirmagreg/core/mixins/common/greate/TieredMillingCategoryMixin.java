package su.terrafirmagreg.core.mixins.common.greate;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.api.GTValues;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import electrolyte.greate.compat.jei.category.GreateRecipeCategory;
import electrolyte.greate.compat.jei.category.TieredMillingCategory;
import electrolyte.greate.compat.jei.category.animations.TieredAnimatedMillstone;
import electrolyte.greate.content.kinetics.crusher.TieredAbstractCrushingRecipe;
import electrolyte.greate.registry.Millstones;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

@Mixin(value = TieredMillingCategory.class, remap = false)
public abstract class TieredMillingCategoryMixin extends GreateRecipeCategory<TieredAbstractCrushingRecipe> {

    public TieredMillingCategoryMixin(Info<TieredAbstractCrushingRecipe> info) {
        super(info);
    }

    /**
     * @author Pyritie
     * @reason Needed to change other parts of the render
     */
    @Overwrite
    public void draw(@NotNull TieredAbstractCrushingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
            @NotNull GuiGraphics graphics, double x, double y) {
        super.draw(recipe, recipeSlotsView, graphics, 1, 57);
        AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);
        new TieredAnimatedMillstone(Millstones.MILLSTONES[recipe.getRecipeTier()].get()).draw(graphics, 48, 27);

        if (recipe.getRecipeTier() < GTValues.HV) {
            graphics.drawWordWrap(Minecraft.getInstance().font, Component.translatable("tfg.recipe.macerator_warning"),
                    87, 50, 90, 0x555555);
        }
    }
}
