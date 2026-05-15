package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;

import su.terrafirmagreg.core.TFGCore;

public class OreVeinInfoRecipe implements EmiRecipe {
    public record WeightedBlock(String ore, int weightPercent) {
    }

    public record WeightedItem(Item ore, int weightPercent) {
    }

    private final String ID;
    @Nullable
    private final String[] emiInfo;
    private final ResourceLocation dimension;
    private final int rarity, minY, maxY, size, height, radius, indicatorDepth;
    private final boolean nearLava, project, projectOffset;
    private final double density;
    @Nullable
    private final String biomeTag;
    @Nullable
    private final String[] biomeList;
    private final String[] rockTypes;
    private final WeightedBlock[] ores;
    private final WeightedItem[] oreItems;
    @Nullable
    Integer minRainfall;
    @Nullable
    Integer maxRainfall;
    @Nullable
    Integer minTemperature;
    @Nullable
    Integer maxTemperature;

    public OreVeinInfoRecipe(String ID, String dimension, int rarity, double density, int minY, int maxY, int size,
            int height, int radius, boolean nearLava, boolean project, boolean projectOffset, int indicatorDepth,
            String[] types, WeightedBlock[] blocks, @Nullable String biomeTag, @Nullable String[] biomeList,
            @Nullable Integer minRainfall, @Nullable Integer maxRainfall, @Nullable Integer minTemperature, @Nullable Integer maxTemperature,
            @Nullable String[] emiInfo) {
        this.ID = ID;
        this.dimension = ResourceLocation.parse(dimension);
        this.rarity = rarity;
        this.density = density;
        this.minY = minY;
        this.maxY = maxY;
        this.size = size;
        this.height = height;
        this.radius = radius;
        this.nearLava = nearLava;
        this.project = project;
        this.projectOffset = projectOffset;
        this.indicatorDepth = indicatorDepth;
        this.rockTypes = types;
        this.ores = blocks;
        this.emiInfo = emiInfo;
        this.biomeTag = biomeTag;
        this.biomeList = biomeList;
        this.minRainfall = minRainfall;
        this.maxRainfall = maxRainfall;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;

        var itemTagRegistry = ForgeRegistries.ITEMS.tags();
        if (itemTagRegistry == null) {
            oreItems = new WeightedItem[0];
        } else {
            List<WeightedItem> rawOres = new ArrayList<>();
            for (var ore : ores) {
                List<Item> validRawOres = new ArrayList<>();

                var normalTag = itemTagRegistry.getTag(itemTagRegistry
                        .createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "raw_materials/" + ore.ore)));
                normalTag.forEach(v -> {
                    if (!itemTagRegistry.getTag(itemTagRegistry.createTagKey(EmiTags.HIDDEN_FROM_RECIPE_VIEWERS)).contains(v))
                        validRawOres.add(v);
                });

                // No raw materials? Try again with gems, to handle coals
                if (validRawOres.isEmpty()) {
                    normalTag = itemTagRegistry.getTag(itemTagRegistry
                            .createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "gems/" + ore.ore)));
                    normalTag.forEach(v -> {
                        if (!itemTagRegistry.getTag(itemTagRegistry.createTagKey(EmiTags.HIDDEN_FROM_RECIPE_VIEWERS)).contains(v))
                            validRawOres.add(v);
                    });
                }
                // No raw ores or gems
                if (validRawOres.isEmpty())
                    continue;

                rawOres.add(new WeightedItem(validRawOres.get(0), ore.weightPercent));
            }

            oreItems = rawOres.toArray(WeightedItem[]::new);
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.ORE_VEIN_INFO;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return TFGCore.id("/" + ID + "_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 200;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        int offsetY = 0;
        offsetY = createLabelWidget(widgets, offsetY);
        offsetY = createOreItemWidgets(widgets, offsetY);
        createVeinInfoTooltip(widgets, offsetY);
        offsetY = createVeinInfoText(widgets, offsetY);
        offsetY = createBiomeText(widgets, offsetY);
        offsetY = createClimateText(widgets, offsetY);
        offsetY = createRockTypesWidget(widgets, offsetY);
        offsetY = createInfoWidget(widgets, offsetY);
        createDimensionMarker(widgets, offsetY);
    }

    private int createLabelWidget(WidgetHolder holder, int offsetY) {
        var formText = Component.translatable("tfg.ore_vein." + ID).getVisualOrderText();
        var font = Minecraft.getInstance().font;

        var textWidget = new TextWidget(formText, getDisplayWidth() / 2, offsetY, 0, false) {

            @Override
            public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                EmiDrawContext context = EmiDrawContext.wrap(draw);
                context.push();
                float scaler = 1;
                if (font.width(this.text) >= 140) {
                    scaler = (float) (140 - 10) / font.width(this.text);
                    context.matrices().scale(scaler, scaler, scaler);
                }

                int xOff = (int) (this.horizontalAlignment.offset(font.width(this.text)) * scaler);
                int yOff = this.verticalAlignment.offset(font.lineHeight);
                context.matrices().translate((float) xOff, (float) yOff, 300.0F);
                if (this.shadow) {
                    context.drawTextWithShadow(this.text, this.x, this.y, this.color);
                } else {
                    context.drawText(this.text, this.x, this.y, this.color);
                }

                context.pop();
            }
        };

        textWidget.horizontalAlign(TextWidget.Alignment.CENTER);

        holder.add(textWidget);
        return offsetY + font.lineHeight;
    }

    private int createOreItemWidgets(WidgetHolder holder, int offsetY) {
        var offsetX = (getDisplayWidth() - (oreItems.length * 20)) / 2;

        var font = Minecraft.getInstance().font;
        for (WeightedItem oreItem : oreItems) {

            var widget = new SlotWidget(EmiIngredient.of(Ingredient.of(oreItem.ore), 1), offsetX + 1, offsetY);
            widget.large(false);
            widget.drawBack(true);
            widget.recipeContext(this);
            holder.add(widget);

            var oreChance = Component.literal((oreItem.weightPercent == 0 ? 1 : oreItem.weightPercent) + "%").getVisualOrderText();
            var textOffset = (20 - font.width(oreChance)) / 2;
            holder.addText(oreChance, offsetX + textOffset, offsetY + 18, 0, false);

            offsetX += 20;
        }
        return offsetY + 18 + font.lineHeight + 5;
    }

    private int createVeinInfoText(WidgetHolder holder, int offsetY) {
        var lineH = Minecraft.getInstance().font.lineHeight;
        holder.addText(Component.translatable("tfg.emi.ore_veins.rarity", rarity).append(rarityText()), 2, offsetY, 0, false);
        offsetY += lineH;
        holder.addText(Component.translatable("tfg.emi.ore_veins.density", (int) (density * 100)).append("%"), 2, offsetY, 0, false);
        offsetY += lineH;

        if (project) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.projected", minY, maxY), 2, offsetY, 0, false);
        } else {
            holder.addText(Component.translatable("tfg.emi.ore_veins.y_ranges", minY, maxY), 2, offsetY, 0, false);
        }
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
        if (nearLava) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.near_lava"), 2, offsetY, 0xFC6900, false);
            offsetY += lineH;
        }
        if (indicatorDepth > 1) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.indicator_depth").append(depthText()), 2, offsetY, 0, false);
            offsetY += lineH;
        }
        return offsetY;
    }

    private int createBiomeText(WidgetHolder holder, int offsetY) {
        var lineH = Minecraft.getInstance().font.lineHeight;

        holder.addText(Component.translatable("tfg.emi.ore_veins.biomes"), 2, offsetY, 0, false);
        offsetY += lineH;

        if (biomeTag == null) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.biome_any"), 8, offsetY, 0, false);
        } else {
            int i = 0;
            MutableComponent tooltip = Component.empty();

            for (String biome : biomeList) {
                tooltip.append(Component.translatable(biome)).append(++i == biomeList.length ? "" : ", ");
            }

            var overflowText = new TextWidget(Component.translatable(biomeTag.replace("tfg:", "tfg.ore_vein_tag.").replace('/', '.')).getVisualOrderText(), 8, offsetY, 0x00AA00, false) {
                @Override
                public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                    return List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
                }
            };
            holder.add(overflowText);
        }
        offsetY += lineH;
        return offsetY;
    }

    private int createClimateText(WidgetHolder holder, int offsetY) {
        var lineH = Minecraft.getInstance().font.lineHeight;

        if (minRainfall != null && maxRainfall != null) {
            holder.addText(
                    Component.translatable("tfg.emi.ore_veins.rainfall").append(Component.translatable("tfg.emi.ore_veins.rainfall_range", minRainfall, maxRainfall).withStyle(ChatFormatting.BLUE)), 2,
                    offsetY, 0,
                    false);
            offsetY += lineH;
        }

        if (minTemperature == null && maxTemperature != null) {
            holder.addText(
                    Component.translatable("tfg.emi.ore_veins.temperature").append(Component.translatable("tfg.emi.ore_veins.temperature_and_below", maxTemperature).withStyle(ChatFormatting.AQUA)), 2,
                    offsetY, 0, false);
            offsetY += lineH;
        } else if (minTemperature != null && maxTemperature == null) {
            holder.addText(
                    Component.translatable("tfg.emi.ore_veins.temperature").append(Component.translatable("tfg.emi.ore_veins.temperature_and_above", minTemperature).withStyle(ChatFormatting.RED)),
                    2, offsetY, 0, false);
            offsetY += lineH;
        } else if (minTemperature != null && maxTemperature != null) {
            holder.addText(Component.translatable("tfg.emi.ore_veins.temperature")
                    .append(Component.translatable("tfg.emi.ore_veins.temperature_range", minTemperature, maxTemperature).withStyle(ChatFormatting.DARK_GREEN)), 2, offsetY, 0, false);
            offsetY += lineH;
        }

        return offsetY;
    }

    private Component rarityText() {
        Component rarityKey;

        if (rarity <= 100)
            rarityKey = Component.translatable("tfg.emi.ore_veins.rarity.common").withStyle(ChatFormatting.DARK_GREEN);
        else if (rarity <= 200)
            rarityKey = Component.translatable("tfg.emi.ore_veins.rarity.uncommon").withStyle(ChatFormatting.YELLOW);
        else if (rarity <= 300)
            rarityKey = Component.translatable("tfg.emi.ore_veins.rarity.rare").withStyle(ChatFormatting.GOLD);
        else
            rarityKey = Component.translatable("tfg.emi.ore_veins.rarity.very_rare").withStyle(ChatFormatting.DARK_RED);

        return Component.empty().append(" [").append(rarityKey).append("]");
    }

    private Component depthText() {
        if (indicatorDepth < 20)
            return Component.literal(String.valueOf(indicatorDepth)).withStyle(ChatFormatting.DARK_RED);
        else if (indicatorDepth < 50)
            return Component.literal(String.valueOf(indicatorDepth)).withStyle(ChatFormatting.GOLD);
        else if (indicatorDepth < 70)
            return Component.literal(String.valueOf(indicatorDepth)).withStyle(ChatFormatting.YELLOW);
        else if (indicatorDepth < 100)
            return Component.literal(String.valueOf(indicatorDepth)).withStyle(ChatFormatting.DARK_GREEN);
        else
            return Component.literal(String.valueOf(indicatorDepth)).withStyle(ChatFormatting.GREEN);
    }

    private int createRockTypesWidget(WidgetHolder holder, int offsetY) {

        holder.addText(Component.translatable("tfg.emi.ore_veins.rock_types"), 2, offsetY, 0, false);
        offsetY += Minecraft.getInstance().font.lineHeight;

        var perLine = Math.floorDiv(getDisplayWidth(), 18);
        perLine = Math.min(perLine, rockTypes.length);
        var offsetStart = (getDisplayWidth() - (perLine * 18)) / 2;
        var offsetX = offsetStart;
        var currentDrawPos = 1;
        for (String rockBlock : rockTypes) {
            var blockItem = parseRockBlock(rockBlock);
            if (blockItem == null)
                continue;

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

    private Item parseRockBlock(String rockBlock) {
        var block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(rockBlock));
        if (block == null)
            return null;

        return block.asItem();
    }

    private int createInfoWidget(WidgetHolder holder, int offsetY) {
        if (emiInfo == null)
            return offsetY;

        var lineH = Minecraft.getInstance().font.lineHeight;
        offsetY += (lineH * 2) + 2;

        for (String part : emiInfo) {
            holder.addText(Component.translatable(part), 2, offsetY, 0, false);
            offsetY += lineH;
        }

        return offsetY + lineH + 2;
    }

    private void createDimensionMarker(WidgetHolder holder, int offsetY) {
        var marker = GTRegistries.DIMENSION_MARKERS.get(dimension);
        if (marker == null)
            return;
        var icon = marker.getIcon();
        var slot = new SlotWidget(EmiIngredient.of(Ingredient.of(icon)), getDisplayWidth() - 26, getDisplayHeight() - 26);
        slot.large(true);
        slot.drawBack(false);
        slot.recipeContext(this);
        holder.add(slot);
    }

    private void createVeinInfoTooltip(WidgetHolder holder, int offsetY) {
        holder.addTooltip(List.of(
                ClientTooltipComponent.create(Component.translatable("tfg.emi.ore_veins.rarity.tooltip", rarity).getVisualOrderText())),
                0, offsetY, 120, 10);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> rockList = new ArrayList<>();

        for (String rockBlock : rockTypes) {
            var rockItem = parseRockBlock(rockBlock);
            if (rockItem == null)
                continue;
            rockList.add(EmiStack.of(rockItem));
        }

        return rockList;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> oreList = new ArrayList<>();
        var tagRegistry = ForgeRegistries.ITEMS.tags();
        if (tagRegistry == null)
            return oreList;
        for (var ore : ores) {
            var poorTag = tagRegistry.getTag(tagRegistry
                    .createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "poor_raw_materials/" + ore.ore)));
            var normalTag = tagRegistry.getTag(tagRegistry
                    .createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "raw_materials/" + ore.ore)));
            var richTag = tagRegistry.getTag(tagRegistry
                    .createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "rich_raw_materials/" + ore.ore)));
            var gemTag = tagRegistry.getTag(tagRegistry
                    .createTagKey(ResourceLocation.fromNamespaceAndPath("forge", "gems/" + ore.ore)));

            poorTag.forEach(v -> oreList.add(EmiStack.of(v)));
            normalTag.forEach(v -> oreList.add(EmiStack.of(v)));
            richTag.forEach(v -> oreList.add(EmiStack.of(v)));
            gemTag.forEach(v -> oreList.add(EmiStack.of(v)));
        }
        return oreList;
    }
}
