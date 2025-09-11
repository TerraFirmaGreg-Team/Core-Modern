package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

@Mixin(value = BasinCategory.class)
public abstract class BasinCategoryMixin {

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"), cancellable = true, remap = false)
    public void tfg$setRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        HeatCondition requiredHeat = recipe.getRequiredHeat();

        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            builder
                    .addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81)
                    .addItemStack(new ItemStack(TFCBlocks.FIREPIT.get()));
        }

        ci.cancel();
    }

    @Inject(method = "draw(Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"), cancellable = true)
    private void tfg$draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
            double mouseY, CallbackInfo ci) {
        HeatCondition requiredHeat = recipe.getRequiredHeat();

        if (requiredHeat != HeatCondition.NONE) {
            int heat = requiredHeat == HeatCondition.HEATED ? 7 : 10;
            MutableComponent color = TFCConfig.CLIENT.heatTooltipStyle.get()
                    .formatColored(Heat.values()[heat].getMin());

            if (color != null) {
                graphics.drawString(Minecraft.getInstance().font, color, 9,
                        86, requiredHeat.getColor(), false);
            }

            ci.cancel();
        }
    }

}
