package su.terrafirmagreg.core.common.tfgt.ui;

import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widgets.layout.Flow;

import su.terrafirmagreg.core.api.pattern.TFGPredicates;

public class TFGReipeUIModifiers {

    public static final RecipeUIModifier ME_ASSEMBLER = (recipe, widget) -> {

        Flow row = Flow.row().coverChildren().childPadding(2);

        for (int t = 1; t <= 4; t++) {
            Block block = TFGPredicates.getBuddingBlockForTier(t);
            row.child(RecipeViewerSlotWidget.create(ItemStack.class).recipeSlotRole(RecipeSlotRole.RENDER_ONLY).value(block.asItem().getDefaultInstance()));
        }
        widget.textComponents.child(row);
    };

    public static final RecipeUIModifier BUDDING_CHARGER = (recipe, widget) -> {
        if (!recipe.data.contains("budding_max_tier"))
            return;
        int tier = recipe.data.getInt("budding_max_tier");

        Flow row = Flow.row();
        row.child(Text.lang("tfg.recipe.budding_max_tier_label",
                Component.translatable("tfg.budding_tier." + tier)).asWidget());

        Block block = TFGPredicates.getBuddingBlockForTier(tier);

        row.child(RecipeViewerSlotWidget.create(ItemStack.class)
                .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                .value(block.asItem().getDefaultInstance()));
    };
}
