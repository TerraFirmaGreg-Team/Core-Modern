package su.terrafirmagreg.core.compat.emi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.common.data.GTItems;

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
        System.out.println(veinID);
        System.out.println(tfgFluidDef);

        if (tfgFluidDef != null) {
            var climateModifiers = tfgFluidDef.getClimateModifiers();
            System.out.println(climateModifiers);
            return climateModifiers;
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

    @Override
    public void addWidgets(WidgetHolder widgets) {

        widgets.addTank(EmiStack.of(fluid, 1000), 50, 100, 16, 16, 1000);
        widgets.addText(Component.literal(veinID.toString()), 50, 50, 0, false);
    }
}
