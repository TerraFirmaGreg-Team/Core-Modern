package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;

public class BlockInteractionRecipe implements EmiRecipe {

    private static final ResourceLocation HAND_HOLD = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/hand_hold.png");
    private static final ResourceLocation HAND_POINT_BG = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/hand_point_bg.png");
    private static final ResourceLocation HAND_POINT_FG = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/hand_point_fg.png");

    private final List<EmiIngredient> INPUTS = new ArrayList<>();
    private final List<EmiStack> OUTPUTS = new ArrayList<>();
    //TOOL refers to the item(s) used to transform the block
    private final List<EmiIngredient> TOOL = new ArrayList<>();
    private final String recipeID;

    public BlockInteractionRecipe(String id, TagKey<Item> INPUT, TagKey<Item> OUTPUT, TagKey<Item> TOOL) {
        recipeID = id;
        INPUTS.add(EmiIngredient.of(INPUT));
        ForgeRegistries.ITEMS.tags().getTag(OUTPUT).forEach(i -> OUTPUTS.add(EmiStack.of(i)));
        this.TOOL.add(EmiIngredient.of(TOOL));
    }

    public BlockInteractionRecipe(String id, TagKey<Item> INPUT, TagKey<Item> OUTPUT, Item CONSUMABLE) {
        recipeID = id;
        INPUTS.add(EmiIngredient.of(INPUT));
        ForgeRegistries.ITEMS.tags().getTag(OUTPUT).forEach(i -> OUTPUTS.add(EmiStack.of(i)));
        this.TOOL.add(EmiIngredient.of(Ingredient.of(CONSUMABLE)));
    }

    public BlockInteractionRecipe(String id, TagKey<Item> INPUT, TagKey<Item> OUTPUT, ItemStack CONSUMABLE) {
        recipeID = id;
        INPUTS.add(EmiIngredient.of(INPUT));
        ForgeRegistries.ITEMS.tags().getTag(OUTPUT).forEach(i -> OUTPUTS.add(EmiStack.of(i)));
        this.TOOL.add(EmiIngredient.of(Ingredient.of(CONSUMABLE)));
    }

    public BlockInteractionRecipe(String id, Item INPUT, Item OUTPUT, Item CONSUMABLE) {
        recipeID = id;
        INPUTS.add(EmiIngredient.of(Ingredient.of(INPUT)));
        OUTPUTS.add(EmiStack.of(OUTPUT));
        this.TOOL.add(EmiIngredient.of(Ingredient.of(CONSUMABLE)));
    }

    public BlockInteractionRecipe(String id, Item INPUT, Item OUTPUT, TagKey<Item> TOOL) {
        recipeID = id;
        INPUTS.add(EmiIngredient.of(Ingredient.of(INPUT)));
        OUTPUTS.add(EmiStack.of(OUTPUT));
        this.TOOL.add(EmiIngredient.of(TOOL));
    }

    public BlockInteractionRecipe(String id, Item INPUT, Item OUTPUT, ItemStack CONSUMABLE) {
        recipeID = id;
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
        return TFGCore.id("/" + recipeID + "_block_interaction_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int itemOffsetY = 2;
        int itemOffsetX = 2;
        int handSizeX = 40;
        int handSizeY = 32;

        widgetHolder.addTexture(HAND_HOLD, itemOffsetX, itemOffsetY, handSizeX, handSizeY, 0, 0, handSizeX, handSizeY, 40, 32);
        itemOffsetX += 21;
        TFGEmiPlugin.createItemWidget(widgetHolder, itemOffsetY + 7, itemOffsetX, EmiIngredient.of(TOOL));
        itemOffsetX += 20;

        TFGEmiPlugin.createItemWidget(widgetHolder, itemOffsetY + 7, itemOffsetX, EmiIngredient.of(INPUTS));
        itemOffsetX += 28;

        widgetHolder.addTexture(HAND_POINT_BG, itemOffsetX, itemOffsetY, handSizeX, handSizeY, 0, 0, handSizeX, handSizeY, 40, 32);
        widgetHolder.addAnimatedTexture(HAND_POINT_FG, itemOffsetX, itemOffsetY, handSizeX, handSizeY, 0, 0, handSizeX, handSizeY, 40, 32, 5000, true, false, false);

        itemOffsetX += handSizeX + 5;
        widgetHolder.add(new SlotWidget(EmiIngredient.of(OUTPUTS), itemOffsetX, itemOffsetY + 7).recipeContext(this));
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Stream.concat(INPUTS.stream(), TOOL.stream()).collect(Collectors.toList());
    }

    @Override
    public List<EmiStack> getOutputs() {
        return OUTPUTS;
    }
}
