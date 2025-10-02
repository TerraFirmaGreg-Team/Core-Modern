package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;

public class BlockInteractionRecipe implements EmiRecipe {

    private static final ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/gui/emi/arrow.png");

    private final List<EmiIngredient> INPUTS = new ArrayList<>();
    private final List<EmiStack> OUTPUTS = new ArrayList<>();
    //TOOL refers to the item(s) used to transform the block
    private final List<EmiIngredient> TOOL = new ArrayList<>();

    public BlockInteractionRecipe(TagKey<Item> INPUT, TagKey<Item> OUTPUT, TagKey<Item> TOOL) {
        INPUTS.add(EmiIngredient.of(INPUT));
        ForgeRegistries.ITEMS.tags().getTag(OUTPUT).forEach(i -> OUTPUTS.add(EmiStack.of(i)));
        this.TOOL.add(EmiIngredient.of(TOOL));
    }

    public BlockInteractionRecipe(TagKey<Item> INPUT, TagKey<Item> OUTPUT, Item CONSUMABLE) {
        INPUTS.add(EmiIngredient.of(INPUT));
        ForgeRegistries.ITEMS.tags().getTag(OUTPUT).forEach(i -> OUTPUTS.add(EmiStack.of(i)));
        this.TOOL.add(EmiIngredient.of(Ingredient.of(CONSUMABLE)));
    }

    public BlockInteractionRecipe(Item INPUT, Item OUTPUT, TagKey<Item> TOOL) {
        INPUTS.add(EmiIngredient.of(Ingredient.of(INPUT)));
        OUTPUTS.add(EmiStack.of(OUTPUT));
        this.TOOL.add(EmiIngredient.of(TOOL));
    }

    public BlockInteractionRecipe(Item INPUT, Item OUTPUT, Item CONSUMABLE) {
        INPUTS.add(EmiIngredient.of(Ingredient.of(INPUT)));
        OUTPUTS.add(EmiStack.of(OUTPUT));
        this.TOOL.add(EmiIngredient.of(Ingredient.of(CONSUMABLE)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.BLOCK_INTERACTION;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return TFGCore.id(INPUTS.toString() + "_" + OUTPUTS.toString() + "_block_interaction_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 28;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int itemOffsetY = 5;
        int itemOffsetX = 25;

        createItemWidget(widgetHolder, itemOffsetY, itemOffsetX, EmiIngredient.of(INPUTS));
        itemOffsetX += 20;

        createItemWidget(widgetHolder, itemOffsetY, itemOffsetX, EmiIngredient.of(TOOL));
        itemOffsetX += 20;

        itemOffsetX = createArrowWidget(widgetHolder, itemOffsetY, itemOffsetX, 30);
        createItemWidget(widgetHolder, itemOffsetY, itemOffsetX, EmiIngredient.of(OUTPUTS));

    }

    private int createItemWidget(WidgetHolder holder, int offsetY, int offsetX, EmiIngredient stack) {

        SlotWidget widget = new SlotWidget(stack, offsetX, offsetY);
        holder.add(widget);

        return widget.getBounds().bottom() + 2;
    }

    private int createArrowWidget(WidgetHolder holder, int offsetY, int offsetX, int length) {
        int image_height = 18;
        int image_width = 40;
        int u_start = image_width - length;

        TextureWidget widget = new TextureWidget(ARROW, offsetX, offsetY, length, image_height, u_start, 0, length, image_height - 1, image_width, image_height);
        holder.add(widget);
        return offsetX + 2 + length;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return INPUTS;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return OUTPUTS;
    }
}
