package su.terrafirmagreg.core.compat.emi;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

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

import su.terrafirmagreg.core.common.tfgt.worldgen.ClimateWeightModifier;
import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidDefinition;
import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidRegistry;

public class FluidVeinRecipe implements EmiRecipe {

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
        var test = veinID.withSuffix("_emi");
        System.out.println(test);
        return test;
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
        return 168;
    }

    @Override
    public int getDisplayHeight() {
        return 170;
    }

    private final int fluidSize = 24;

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int yPointer = 2;

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins." + veinID.getPath()), 2, yPointer);

        yPointer = addTank(widgets, this.getDisplayWidth() / 2 - fluidSize / 2, yPointer);

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.yield_range", minYield + "mB/s", maxYield + "mB/s"), 2, yPointer);

        yPointer = addTextLineTooltip(widgets, Component.translatable("tfg.emi.fluid_veins.depletion", depChance + "%"), 2, yPointer,
                Component.translatable("tfg.emi.fluid_veins.depletion.tooltip", depChance + "%", depAmount, BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS));

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.depleted_yield", depYield + "mB/s"), 2, yPointer);

        if (weight != 0) {
            yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.weight", weight), 2, yPointer);
        }

        yPointer = addDimensionIcons(widgets, 2, yPointer);

        yPointer = addBiomes(widgets, 2, yPointer);

        yPointer = addClimate(widgets, 2, yPointer);
    }

    private int addTank(WidgetHolder widgets, int x, int y) {
        var tankWidget = new TankWidget(EmiStack.of(fluid, 1000), x, y, fluidSize, fluidSize, 1000);
        tankWidget.drawBack(false);
        tankWidget.large(true);

        widgets.add(tankWidget);
        return y + fluidSize + 2;
    }

    private int addDimensionIcons(WidgetHolder widgets, int x, int y) {
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
                String type = climateVar.getKey();
                float min = climateVar.getValue().get(0).intValue();
                float max = climateVar.getValue().get(1).intValue();

                String numberSuffix = "temperature".equals(type) ? "°C" : "mm";

                y = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins." + type, min + numberSuffix, max + numberSuffix), x, y);
            }
        }
        return y;
    }

    private int addBiomeList(WidgetHolder widgets, Set<ResourceKey<Biome>> biomeKeys, int x, int y, boolean anyBiome) {
        int indent = 6;
        int cutoff = 6;
        widgets.addText(Component.translatable("tfg.emi.fluid_veins.biomes"), x, y, 0, false);

        if (!anyBiome) {
            int i = 0;

            MutableComponent tooltip = Component.empty();
            for (ResourceKey<Biome> entry : biomeKeys) {
                i++;
                var langComp = Component.translatable("biome." + entry.location().toLanguageKey());

                if (i < cutoff) {
                    y += 8;
                    addTextLine(widgets, langComp, x + indent, y);
                }

                String appendSuffix = i == biomeKeys.size() ? "" : ", ";
                tooltip.append(langComp).append(appendSuffix);
            }

            if (i >= cutoff) {
                y += 8;
                var overflowText = new TextWidget(Component.translatable("tfg.emi.fluid_veins.biomes_overflow", "+" + (i - cutoff + 1)).getVisualOrderText(), x + indent, y, 0, false) {
                    @Override
                    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                        return List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText()));
                    }
                };
                widgets.add(overflowText);
            }

            y = newLineY(y);
        } else {
            y += 8;
            y = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.biome_any"), x + indent, y);
        }

        return y;
    }

    private int addTextLine(WidgetHolder widgets, Component text, int x, int y) {
        widgets.addText(text, x, y, 0, false);
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
        return oldY + 10;
    }

}
