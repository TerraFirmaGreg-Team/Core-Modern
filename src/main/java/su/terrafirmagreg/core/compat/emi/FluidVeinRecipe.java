package su.terrafirmagreg.core.compat.emi;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
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
        return List.of(EmiStack.of(GTItems.PROSPECTOR_HV));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(fluid));
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 180;
    }

    private final int fluidSize = 24;

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int yPointer = 2;

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins." + veinID.getPath()), 2, yPointer);

        yPointer = addTank(widgets, this.getDisplayWidth() / 2 - fluidSize / 2, yPointer);

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.yield_range", minYield, maxYield), 2, yPointer);

        //Add tooltip that shows depletion amount
        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.depletion", depChance + "%"), 2, yPointer);

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.depleted_yield", depYield), 2, yPointer);

        yPointer = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.weight", weight), 2, yPointer);

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
            //slot is 18px tall, text is 8px tall
            var slotWidget = new SlotWidget(EmiStack.of(Objects.requireNonNull(GTRegistries.DIMENSION_MARKERS.get(dimension.location())).getIcon()),
                    pointerX, y + 4 - 8);
            slotWidget.drawBack(false);

            widgets.add(slotWidget);
            pointerX += 20;
        }
        return newLineY(y);
    }

    private int addBiomes(WidgetHolder widgets, int x, int y) {

        widgets.addText(Component.translatable("tfg.emi.fluid_veins.biomes"), x, y, 0, false);
        int indent = 6;

        if (biomeHolder.size() == 0 && climateWeight == null) {
            y += 8;
            return addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.biome_any"), x + indent, y);
        }

        for (Holder<Biome> entry : biomeHolder) {
            y += 8;
            addTextLine(widgets, Component.translatable("biome." + entry.unwrapKey().get().location().toLanguageKey()), x + indent, y);
        }

        return newLineY(y);
    }

    private int addClimate(WidgetHolder widgets, int x, int y) {
        if (climateWeight == null) {
            return y;
        }

        for (var climateDef : climateWeight) {
            widgets.addText(Component.translatable("tfg.emi.fluid_veins.biomes"), x, y, 0, false);
            int indent = 6;

            if (climateDef.getBiomes() != null) {
                for (ResourceKey<Biome> entry : climateDef.getBiomes()) {
                    y += 8;
                    addTextLine(widgets, Component.translatable("biome." + entry.location().toLanguageKey()), x + indent, y);
                }
                y = newLineY(y);
            } else {
                y += 8;
                y = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins.biome_any"), x + indent, y);
            }

            for (var climateVar : climateDef.getClimates().entrySet()) {
                String type = climateVar.getKey();
                float min = climateVar.getValue().get(0);
                float max = climateVar.getValue().get(1);

                String numberSuffix = "temperature".equals(type) ? "°C" : "mm";

                y = addTextLine(widgets, Component.translatable("tfg.emi.fluid_veins." + type, min + numberSuffix, max + numberSuffix), x, y);
            }
        }
        return y;
    }

    private int addTextLine(WidgetHolder widgets, Component text, int x, int y) {
        widgets.addText(text, x, y, 0, false);
        return newLineY(y);
    }

    private int newLineY(int oldY) {
        return oldY + 10;
    }

}
