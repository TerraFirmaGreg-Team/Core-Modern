package su.terrafirmagreg.core.mixins.common.greate;

import com.gregtechceu.gtceu.api.GTValues;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import electrolyte.greate.compat.jei.category.GreateRecipeCategory;
import electrolyte.greate.compat.jei.category.TieredCrushingCategory;
import electrolyte.greate.compat.jei.category.animations.TieredAnimatedCrushingWheels;
import electrolyte.greate.content.kinetics.crusher.TieredAbstractCrushingRecipe;
import electrolyte.greate.registry.CrushingWheels;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = TieredCrushingCategory.class, remap = false)
public abstract class TieredCrushingCategoryMixin extends GreateRecipeCategory<TieredAbstractCrushingRecipe> {

	public TieredCrushingCategoryMixin(Info<TieredAbstractCrushingRecipe> info) {
		super(info);
	}

	/**
	 * @author Pyritie
	 * @reason Needed to change other parts of the render
	 */
	@Overwrite
	public void draw(@NotNull TieredAbstractCrushingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double x, double y) {
		super.draw(recipe, recipeSlotsView, graphics, 1, 103);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 72, 7);
		new TieredAnimatedCrushingWheels(CrushingWheels.CRUSHING_WHEELS[recipe.getRecipeTier()].get()).draw(graphics, 62, 59);

		if (recipe.getRecipeTier() < GTValues.HV) {
			graphics.drawWordWrap(Minecraft.getInstance().font, Component.translatable("tfg.recipe.macerator_warning"), 2, 2, 50, 0xFF5555);
		}
	}
}
