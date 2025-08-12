package su.terrafirmagreg.core.compat.emi;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.registry.EmiTags;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.TFGCore;

import java.util.ArrayList;
import java.util.List;

public class OreVeinInfoRecipe implements EmiRecipe {
    public record WeightedBlock(String ore, int weightPercent) {}
    public record WeightedItem(Item ore, int weightPercent) {}

    private final String ID;
    private final ResourceLocation dimension;
    private final int rarity, minY, maxY, size, height, radius;
    private final double density;
    private final String[] rockTypes;
    private final WeightedBlock[] ores;
    private final WeightedItem[] oreItems;
    public OreVeinInfoRecipe(String ID, String dimension, int rarity, double density, int minY, int maxY, int size, int height, int radius, String[] types, WeightedBlock[] blocks) {
        this.ID = ID;
        this.dimension = ResourceLocation.parse(dimension);
        this.rarity = rarity;
        this.density = density;
        this.minY = minY;
        this.maxY = maxY;
        this.size = size;
        this.height = height;
        this.radius = radius;
        this.rockTypes = types;
        this.ores = blocks;

        var tagRegistry = ForgeRegistries.ITEMS.tags();
        if (tagRegistry == null) {
            oreItems = new WeightedItem[0];
            return;
        }

        List<WeightedItem> rawOres = new ArrayList<>();
        for (var ore: ores) {
            List<Item> validRawOres = new ArrayList<>();
            var normalTag = tagRegistry.getTag(tagRegistry.createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "raw_materials/" + ore.ore)));
            normalTag.forEach(v -> {
                if (!tagRegistry.getTag(tagRegistry.createTagKey(EmiTags.HIDDEN_FROM_RECIPE_VIEWERS)).contains(v)) validRawOres.add(v);
            });
            if (validRawOres.isEmpty()) continue;
            rawOres.add(new WeightedItem(validRawOres.get(0), ore.weightPercent));
        }

        oreItems = rawOres.toArray(WeightedItem[]::new);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.ORE_VEIN_INFO;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return TFGCore.id(ID + "_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 180;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        int offsetY = 0;
        offsetY = createLabelWidget(widgets, offsetY);
        offsetY = createOreItemWidgets(widgets, offsetY);
        offsetY = createVeinInfoText(widgets, offsetY);
        offsetY = createRockTypesWidget(widgets, offsetY);
        createDimensionMarker(widgets, offsetY);
    }

    private int createLabelWidget(WidgetHolder holder, int offsetY) {
        var oreVeinLabelComp = Component.translatable("ore_vein.tfg." + ID).getVisualOrderText();
        var width = Minecraft.getInstance().font.width(oreVeinLabelComp);
        var offsetX = (getDisplayWidth()- width)/2;
        holder.addText(oreVeinLabelComp, offsetX, 0, 0, false);
        return offsetY + Minecraft.getInstance().font.lineHeight + 2;
    }

    private int createOreItemWidgets(WidgetHolder holder, int offsetY) {
        var offsetX = (getDisplayWidth()-(oreItems.length*20))/2;;
        var font = Minecraft.getInstance().font;
        for (WeightedItem oreItem : oreItems) {

            var widget = new SlotWidget(EmiIngredient.of(Ingredient.of(oreItem.ore), 1), offsetX+1, offsetY);
            widget.large(false);
            widget.drawBack(true);
            widget.recipeContext(this);
            holder.add(widget);

            var oreChance = Component.literal( (oreItem.weightPercent == 0 ? 1 : oreItem.weightPercent) + "%").getVisualOrderText();
            var textOffset = (20-font.width(oreChance))/2;
            holder.addText(oreChance, offsetX + textOffset, offsetY + 18, 0, false);

            offsetX += 20;
        }
        return offsetY + 18 + font.lineHeight + 5;
    }

    private int createVeinInfoText(WidgetHolder holder, int offsetY) {
        var lineH = Minecraft.getInstance().font.lineHeight;
        holder.addText(Component.translatable("tfg.emi.ore_veins.rarity", rarity), 2, offsetY, 0, false);
        offsetY += lineH;
        holder.addText(Component.translatable("tfg.emi.ore_veins.density", density), 2, offsetY, 0, false);
        offsetY += lineH;
        holder.addText(Component.translatable("tfg.emi.ore_veins.y_ranges", minY, maxY), 2, offsetY, 0, false);
        offsetY += lineH;
        if (size != 0) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.size", size), 2, offsetY, 0, false);
            offsetY += lineH;
        }
        if (height != 0) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.height", height), 2, offsetY, 0, false);
            offsetY += lineH;
        }
        if (radius != 0) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.radius", radius), 2, offsetY, 0, false);
            offsetY += lineH;
        }
        return offsetY;
    }

    private int createRockTypesWidget(WidgetHolder holder, int offsetY) {

        holder.addText(Component.translatable("tfg.emi.ore_veins.rock_types"), 2, offsetY, 0, false);
        offsetY += Minecraft.getInstance().font.lineHeight;

        var perLine = Math.floorDiv(getDisplayWidth(), 18);
        perLine = Math.min(perLine, rockTypes.length);
        var offsetStart = (getDisplayWidth()-(perLine*18))/2;
        var offsetX = offsetStart;
        var currentDrawPos = 1;
        for (String rockBlock : rockTypes) {
            var block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(rockBlock));
            if (block == null) continue;
            var blockItem = block.asItem();

            if (currentDrawPos > perLine) {
                offsetY += 18;
                currentDrawPos = 1;
                offsetX = offsetStart;
            }

            var widget = new SlotWidget(EmiIngredient.of(Ingredient.of(blockItem), 1), offsetX, offsetY);
            widget.large(false);
            widget.drawBack(true);
            widget.recipeContext(this);
            holder.add(widget);

            offsetX += 18;
            currentDrawPos += 1;
        }
        return offsetY;
    }

    private void createDimensionMarker(WidgetHolder holder, int offsetY) {
        var marker = GTRegistries.DIMENSION_MARKERS.get(dimension);
        if (marker == null) return;
        var icon = marker.getIcon();
        var slot = new SlotWidget(EmiIngredient.of(Ingredient.of(icon)), getDisplayWidth() - 26, getDisplayHeight() - 26);
        slot.large(true);
        slot.drawBack(false);
        slot.recipeContext(this);
        holder.add(slot);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> oreList = new ArrayList<>();
        var tagRegistry = ForgeRegistries.ITEMS.tags();
        if (tagRegistry == null) return oreList;
        for (var ore: ores) {
            var poorTag = tagRegistry.getTag(tagRegistry.createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "poor_raw_materials/" + ore.ore)));
            var normalTag = tagRegistry.getTag(tagRegistry.createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "raw_materials/" + ore.ore)));
            var richTag = tagRegistry.getTag(tagRegistry.createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "rich_raw_materials/" + ore.ore)));

            poorTag.forEach(v -> oreList.add(EmiStack.of(v)));
            normalTag.forEach(v -> oreList.add(EmiStack.of(v)));
            richTag.forEach(v -> oreList.add(EmiStack.of(v)));
        }
        return oreList;
    }
}
