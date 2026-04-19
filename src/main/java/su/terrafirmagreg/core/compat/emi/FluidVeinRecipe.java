package su.terrafirmagreg.core.compat.emi;

import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.tfgt.worldgen.ClimateWeightModifier;
import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidDefinition;
import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidRegistry;

public class FluidVeinRecipe implements EmiRecipe {

    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static final HashMap<ResourceLocation, Integer> HAS_SURFACE_INDICATOR = new HashMap<>();

    //Initializing this large of a map at the definition makes me angry
    static {
        HAS_SURFACE_INDICATOR.put(TFGCore.id("heavy_oil_spout_hot"), 2);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("heavy_oil_spout_ocean"), 3);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("raw_oil_spout_hot"), 2);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("raw_oil_spout_ocean"), 3);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("light_oil_spout_hot"), 2);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("light_oil_spout_ocean"), 3);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("oil_spout_hot"), 2);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("oil_spout_ocean"), 3);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("natural_gas_surface_indicator"), 1);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("natural_gas_ocean"), 1);
        HAS_SURFACE_INDICATOR.put(TFGCore.id("spring_water"), 1);
    }

    private static final int INDENT = 6;
    private static final int NEW_LINE = 11;

    private final ResourceLocation veinID;
    private final Fluid fluid;
    private final int depChance;
    private final int depAmount;
    private final int depYield;
    private final int minYield;
    private final int maxYield;
    private final int weight;
    private final Set<ResourceKey<Level>> dimensions;
    private final HolderSet<Biome> biomeHolder;
    @Nullable
    private final List<ClimateWeightModifier> climateWeight;

    public FluidVeinRecipe(Map.Entry<ResourceLocation, BedrockFluidDefinition> entry) {

        BedrockFluidDefinition def = entry.getValue();
        BiomeWeightModifier biomeWeight = def.getBiomeWeightModifier();

        veinID = entry.getKey();
        fluid = def.getStoredFluid().get();

        depAmount = def.getDepletionAmount();
        depChance = def.getDepletionChance();
        depYield = def.getDepletedYield();

        minYield = def.getMinimumYield();
        maxYield = def.getMaximumYield();

        dimensions = def.getDimensionFilter();
        weight = def.getWeight();
        biomeHolder = biomeWeight.biomes.get();
        climateWeight = matchClimateDefinition();
    }

    private List<ClimateWeightModifier> matchClimateDefinition() {
        TFGBedrockFluidDefinition tfgFluidDef = TFGBedrockFluidRegistry.get(veinID);
        if (tfgFluidDef != null) {
            return tfgFluidDef.getClimateModifiers();
        }
        return null;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.FLUID_VEIN_INFO;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return veinID.withPrefix("/").withSuffix("_emi");
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(fluid));
    }

    @Override
    public int getDisplayWidth() {
        return 166;
    }

    @Override
    public int getDisplayHeight() {
        return 198;
    }

    private final int fluidSize = 24;

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int yPointer = 2;

        yPointer = addCenteredTextLine(widgets, Component.translatable("tfg.emi.fluid_veins." + veinID.getPath()), 0, yPointer);

        yPointer = addTank(widgets, this.getDisplayWidth() / 2 - fluidSize / 2, yPointer);

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.yield_range", minYield + "mB/s", maxYield + "mB/s"), 2, yPointer);

        yPointer = addTextLineTooltip(widgets, Component.translatable("tfg.emi.fluid_veins.depletion", depChance + "%"), 2, yPointer,
                Component.translatable("tfg.emi.fluid_veins.depletion.tooltip", depChance + "%", depAmount, BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS));

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.depleted_yield", depYield + "mB/s"), 2, yPointer);

        if (weight != 0) {
            yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.weight", weight), 2, yPointer);
        }

        //yPointer = addInlineDimensionIcons(widgets, 2, yPointer);
        //Still don't know which one looks better
        addCornerDimensionIcons(widgets);

        yPointer = addBiomes(widgets, 2, yPointer);

        yPointer = addClimate(widgets, 2, yPointer);

        yPointer = addIndicatorInfo(widgets, 2, yPointer);
    }

    private int addTank(WidgetHolder widgets, int x, int y) {
        var tankWidget = new TankWidget(EmiStack.of(fluid, 1000), x, y, fluidSize, fluidSize, 1000);
        tankWidget.drawBack(false);
        tankWidget.large(true);

        widgets.add(tankWidget);
        return y + fluidSize + 2;
    }

    private void addCornerDimensionIcons(WidgetHolder widgets) {
        for (var dimension : dimensions) {
            var slotWidget = new SlotWidget(EmiStack.of(Objects.requireNonNull(GTRegistries.DIMENSION_MARKERS.get(dimension.location())).getIcon()),
                    getDisplayWidth() - 27, getDisplayHeight() - 27);
            slotWidget.drawBack(false).large(true);

            widgets.add(slotWidget);
        }
    }

    private int addInlineDimensionIcons(WidgetHolder widgets, int x, int y) {
        int pointerX = x;
        var textWidget = new TextWidget(Component.translatable("tfg.emi.fluid_veins.dimension").getVisualOrderText(), pointerX, y, 0, false);
        widgets.add(textWidget);
        pointerX += (textWidget.getBounds().width() + 2);

        for (var dimension : dimensions) {
            //slot is 16px tall, text is 8px tall
            var slotWidget = new SlotWidget(EmiStack.of(Objects.requireNonNull(GTRegistries.DIMENSION_MARKERS.get(dimension.location())).getIcon()),
                    pointerX, y + 4 - 8);
            slotWidget.drawBack(false);

            widgets.add(slotWidget);
            pointerX += 20;
        }
        return newLineY(y);
    }

    private int addBiomeList(WidgetHolder widgets, Set<ResourceKey<Biome>> biomeKeys, int x, int y, boolean anyBiome) {
        int cutoff = 6;
        int new_line = NEW_LINE - 2;
        widgets.addText(Component.translatable("tfg.emi.fluid_veins.biomes"), x, y, 0, false);

        if (!anyBiome) {
            int i = 0;

            MutableComponent tooltip = Component.empty();
            List<ResourceKey<Biome>> sortedBiomeKeys = biomeKeys.stream().sorted().toList();

            for (ResourceKey<Biome> entry : sortedBiomeKeys) {
                i++;
                var langComp = Component.translatable("biome." + entry.location().toLanguageKey());

                if (i < cutoff) {
                    y += new_line;
                    addTextLine(widgets, langComp, x + INDENT, y);
                }

                String appendSuffix = i == biomeKeys.size() ? "" : ", ";
                tooltip.append(langComp).append(appendSuffix);
            }

            if (i >= cutoff) {
                y += new_line;
                var overflowText = new TextWidget(Component.translatable("tfg.emi.fluid_veins.biomes_overflow", "+" + (i - cutoff + 1)).getVisualOrderText(), x + INDENT, y, 0, false) {
                    @Override
                    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                        return List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
                    }
                };
                widgets.add(overflowText);
            }

            y = newLineY(y);
        } else {
            y += new_line;
            y = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.biome_any"), x + INDENT, y);
        }

        return y;
    }

    private int addBiomes(WidgetHolder widgets, int x, int y) {

        if (climateWeight == null) {
            y = addBiomeList(widgets, biomeHolder.stream().map(entry -> entry.unwrapKey().get()).collect(Collectors.toSet()), x, y, biomeHolder.size() == 0);
        }

        return y;
    }

    private int addClimate(WidgetHolder widgets, int x, int y) {
        if (climateWeight == null) {
            return y;
        }

        for (var climateDef : climateWeight) {
            y = addBiomeList(widgets, climateDef.getBiomes(), x, y, (climateDef.getBiomes() == null));

            for (var climateVar : climateDef.getClimates().entrySet()) {
                ClimateWeightModifier.Mode mode = climateVar.getKey();
                float min = climateVar.getValue().get(0).intValue();
                float max = climateVar.getValue().get(1).intValue();

                String numberSuffix = mode == ClimateWeightModifier.Mode.TEMPERATURE ? "°C" : "mm";

                y = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins." + mode.toString().toLowerCase(Locale.ROOT),
                        min + numberSuffix, max + numberSuffix), x, y);
            }
        }

        return y;
    }

    private int addIndicatorInfo(WidgetHolder widgets, int x, int y) {
        int new_line = NEW_LINE - 2;
        if (HAS_SURFACE_INDICATOR.containsKey(veinID)) {
            addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.indicator"), x, y);

            for (int i = 1; i <= HAS_SURFACE_INDICATOR.get(veinID); i++) {
                y += new_line;
                addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.indicator." + veinID.getPath() + "." + i), x + INDENT, y);
            }
        }
        return newLineY(y);
    }

    private int addTextLine(WidgetHolder widgets, Component text, int x, int y) {
        widgets.addText(text, x, y, 0, false);
        return newLineY(y);
    }

    private int addCenteredTextLine(WidgetHolder widgets, Component text, int x, int y) {
        var formText = text.getVisualOrderText();

        var textWidget = new TextWidget(formText, x + getDisplayWidth() / 2, y, 0, false) {

            @Override
            public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
                EmiDrawContext context = EmiDrawContext.wrap(draw);
                context.push();
                float scaler = 1;
                if (CLIENT.font.width(this.text) >= 166) {
                    scaler = (float) (166 - 10) / CLIENT.font.width(this.text);
                    context.matrices().scale(scaler, scaler, scaler);
                }

                int xOff = (int) (this.horizontalAlignment.offset(CLIENT.font.width(this.text)) * scaler);
                int yOff = this.verticalAlignment.offset(CLIENT.font.lineHeight);
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

        widgets.add(textWidget);
        return newLineY(y);
    }

    private int addTextLineTooltip(WidgetHolder widgets, Component text, int x, int y, Component tooltip) {
        var textWidget = new TextWidget(text.getVisualOrderText(), x, y, 0, false) {
            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                return List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
            }
        };

        widgets.add(textWidget);
        return newLineY(y);
    }

    private int newLineY(int oldY) {
        return oldY + NEW_LINE;
    }

}
