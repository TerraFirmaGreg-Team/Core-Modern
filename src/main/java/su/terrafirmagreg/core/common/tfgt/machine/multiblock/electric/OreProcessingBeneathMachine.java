package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;

import su.terrafirmagreg.core.common.data.TFGTags;

// Credit to Monicore by NegaNote
// https://github.com/NegaNote/MoniLabs/blob/main/src/main/java/net/neganote/monilabs/common/machine/multiblock/SculkVatMachine.java

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreProcessingBeneathMachine extends WorkableElectricMultiblockMachine {

    private static final double MIN_RATIO = 0.15; // Under this amount the machine won't start
    private static final double MAX_RATIO = 0.85; // Over this amount the machine won't start
    private static final double OPTIMAL_RATIO = 0.50; // The percentage of fluid in the hatch so it's optimal
    private static final double SIGMA = 0.15; // Lower will make the curve harder

    private static final int MAX_PARALLELS = 8; // Amount of parallel
    private static final double PARALLEL_EU_DISCOUNT = 0.75;

    public OreProcessingBeneathMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    // Gas Logic - Check the amount of gas

    /**
     * @return the fill ratio of the first tank holding a tfg:ore_proc_gas fluid, or -1 if there is none
     */
    private double getGasRatio() {
        for (var handler : getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)) {
            if (handler instanceof NotifiableFluidTank tank) {
                for (int i = 0; i < tank.getTanks(); i++) {
                    FluidStack stack = tank.getFluidInTank(i);
                    if (!stack.isEmpty() && stack.getFluid().is(TFGTags.Fluids.OreProcGas)) {
                        int capacity = tank.getTankCapacity(i);
                        return capacity <= 0 ? -1 : (double) stack.getAmount() / capacity;
                    }
                }
            }
        }
        return -1;
    }

    private int getGasLevelPercent() {
        double ratio = getGasRatio();
        return ratio < 0 ? -1 : (int) (ratio * 100);
    }

    private static double calculateGaussianModifier(double ratio) {
        double exponent = -Math.pow(ratio - OPTIMAL_RATIO, 2) / (2 * SIGMA * SIGMA);
        return Math.exp(exponent);
    }

    private int getOutputModifierPercent() {
        double ratio = getGasRatio();
        return ratio < 0 ? 0 : (int) (calculateGaussianModifier(ratio) * 100);
    }

    // Recipe Logic

    public static @NotNull ModifierFunction parallelModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof OreProcessingBeneathMachine processor)) {
            return RecipeModifier.nullWrongType(OreProcessingBeneathMachine.class, machine);
        }

        int maxParallel = MAX_PARALLELS;
        long recipeEUt = recipe.getInputEUt().getTotalEU();
        if (recipeEUt > 0) {
            long maxByEnergy = (long) (processor.getOverclockVoltage() / (recipeEUt * PARALLEL_EU_DISCOUNT));
            maxParallel = (int) Math.min(MAX_PARALLELS, Math.max(1, maxByEnergy));
        }

        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallel);
        if (parallels <= 0)
            return ModifierFunction.NULL;
        if (parallels == 1)
            return ModifierFunction.IDENTITY;

        ModifierFunction base = ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels * PARALLEL_EU_DISCOUNT)
                .parallels(parallels)
                .build();

        // Just change the fluid to not scale only with parallel
        double fluidCorrection = Math.sqrt(parallels) / parallels;
        ContentModifier fluidFix = ContentModifier.multiplier(fluidCorrection);

        return r -> {
            GTRecipe modified = base.apply(r);
            if (modified != null && modified.tickInputs.containsKey(FluidRecipeCapability.CAP)) {
                var fixedFluids = fluidFix.applyContents(
                        Map.of(FluidRecipeCapability.CAP, modified.tickInputs.get(FluidRecipeCapability.CAP)));
                modified.tickInputs.put(FluidRecipeCapability.CAP, fixedFluids.get(FluidRecipeCapability.CAP));
            }
            return modified;
        };
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof OreProcessingBeneathMachine processor)) {
            return RecipeModifier.nullWrongType(OreProcessingBeneathMachine.class, machine);
        }

        double ratio = processor.getGasRatio();
        if (ratio < 0) {
            return ModifierFunction.cancel(Component.translatable("tfg.machine.ore_processing_beneath.no_gas")
                    .withStyle(ChatFormatting.RED));
        }
        if (ratio < MIN_RATIO || ratio > MAX_RATIO) {
            return ModifierFunction.cancel(Component.translatable("tfg.machine.ore_processing_beneath.gas_critical",
                    (int) (ratio * 100)).withStyle(ChatFormatting.RED));
        }

        double modifier = calculateGaussianModifier(ratio);

        return r -> {
            Map<RecipeCapability<?>, List<Content>> newOutputs = new HashMap<>();
            for (var entry : r.outputs.entrySet()) {
                var cap = entry.getKey();
                List<Content> newContents = new ArrayList<>();
                for (Content content : entry.getValue()) {
                    if (content.isChanced()) {
                        // Byproduct only if chance isn't guarantee
                        int newChance = (int) (content.chance() * modifier);
                        newContents.add(new Content(content.content(), newChance, content.maxChance()));
                    } else {
                        // If Guarantee keep the number
                        newContents.add(content);
                    }
                }
                newOutputs.put(cap, newContents);
            }

            var copied = new GTRecipe(r.recipeType, r.id,
                    r.inputs, newOutputs,
                    r.tickInputs, r.tickOutputs,
                    r.inputChanceLogics, r.outputChanceLogics,
                    r.tickInputChanceLogics, r.tickOutputChanceLogics,
                    r.conditions, r.ingredientActions,
                    r.data, r.duration, r.parallels, r.subtickParallels, r.batchParallels, r.recipeCategory, r.groupColor, r.keepSpoilingProgress);
            copied.ocLevel = r.ocLevel;
            copied.outputModifier = r.outputModifier;
            return copied;
        };
    }

    // GUI

    private int getCurrentParallels() {
        var lastRecipe = getRecipeLogic().getLastUnrolledRecipe();
        return getRecipeLogic().isWorking() && lastRecipe != null ? lastRecipe.parallels : 0;
    }

    private List<Component> getFluidConsumptionLines() {
        var lastRecipe = getRecipeLogic().getLastUnrolledRecipe();
        if (!getRecipeLogic().isWorking() || lastRecipe == null || lastRecipe.parallels <= 1)
            return List.of();

        var tickFluids = lastRecipe.tickInputs.get(FluidRecipeCapability.CAP);
        if (tickFluids == null)
            return List.of();

        List<Component> lines = new ArrayList<>();
        for (Content content : tickFluids) {
            var ingredient = FluidRecipeCapability.CAP.of(content.content());
            if (ingredient.getStacks().length == 0)
                continue;
            FluidStack stack = ingredient.getStacks()[0];

            Component amountText;
            if (content.isChanced()) {
                double avg = stack.getAmount()
                        * ((double) content.chance() / content.maxChance())
                        * lastRecipe.parallels;
                amountText = Component.literal("≈ " + String.format("%.1f", avg) + " mB/t")
                        .withStyle(ChatFormatting.AQUA);
            } else {
                amountText = Component.literal(stack.getAmount() + " mB/t")
                        .withStyle(ChatFormatting.AQUA);
            }
            lines.add(Component.translatable("tfg.machine.ore_processing_beneath.fluid_consumption",
                    stack.getDisplayName(), amountText));
        }
        return lines;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager, 260, 200).margin(4, 2));
    }

    public List<IWidget> createGasDisplayWidgets(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        IntSyncValue gasLevel = new IntSyncValue(this::getGasLevelPercent);
        IntSyncValue outputModifier = new IntSyncValue(this::getOutputModifierPercent);
        IntSyncValue parallels = new IntSyncValue(this::getCurrentParallels);
        GenericListSyncHandler<Component> fluidLines = new GenericListSyncHandler.Builder<Component>()
                .adapter(ByteBufAdapters.COMPONENT)
                .getter(this::getFluidConsumptionLines)
                .build();

        syncManager.syncValue("oreProcGasLevel", gasLevel);
        syncManager.syncValue("oreProcOutputModifier", outputModifier);
        syncManager.syncValue("oreProcParallels", parallels);
        syncManager.syncValue("oreProcFluidLines", fluidLines);

        widgets.add(Text.lang("tfg.machine.ore_processing_beneath.no_gas")
                .withStyle(ChatFormatting.RED).asWidget()
                .setEnabledIf($ -> isFormed.getBoolValue() && gasLevel.getIntValue() < 0));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.ore_processing_beneath.gas_level",
                gasLevel.getIntValue()).withStyle(gasLevelColor(gasLevel.getIntValue()))).asWidget()
                .setEnabledIf($ -> isFormed.getBoolValue() && gasLevel.getIntValue() >= 0));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.ore_processing_beneath.output_modifier",
                outputModifier.getIntValue())
                .withStyle(outputModifier.getIntValue() >= 90 ? ChatFormatting.GREEN : ChatFormatting.YELLOW)).asWidget()
                .tooltip(t -> t.addLine(Component.translatable("tfg.machine.ore_processing_beneath.output_modifier.tooltip")))
                .setEnabledIf($ -> isFormed.getBoolValue() && gasLevel.getIntValue() >= 0));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.ore_processing_beneath.parallel_info",
                Component.literal(parallels.getIntValue() + "/" + MAX_PARALLELS).withStyle(ChatFormatting.AQUA))).asWidget()
                .tooltip(t -> t.addLine(Component.translatable("tfg.machine.ore_processing_beneath.parallel_info.tooltip",
                        MAX_PARALLELS)))
                .setEnabledIf($ -> isFormed.getBoolValue() && parallels.getIntValue() > 1));

        DynamicLinkedSyncHandler<GenericListSyncHandler<Component>> fluidLinesHandler = new DynamicLinkedSyncHandler<>(
                fluidLines)
                .widgetProvider((psm, list) -> {
                    Flow column = Flow.column()
                            .widthRel(1)
                            .coverChildrenHeight()
                            .crossAxisAlignment(Alignment.CrossAxis.START);
                    for (Component line : list.getValue()) {
                        column.child(Text.comp(line).asWidget()
                                .tooltip(t -> t.addLine(Component.translatable(
                                        "tfg.machine.ore_processing_beneath.fluid_consumption.tooltip"))));
                    }
                    return column;
                });

        widgets.add(new DynamicWidget<>()
                .widthRel(1)
                .coverChildrenHeight()
                .syncHandler(fluidLinesHandler)
                .setEnabledIf($ -> isFormed.getBoolValue() && parallels.getIntValue() > 1));

        return widgets;
    }

    private static ChatFormatting gasLevelColor(int percent) {
        double ratio = percent / 100.0;
        if (ratio < MIN_RATIO || ratio > MAX_RATIO)
            return ChatFormatting.RED;
        if (Math.abs(ratio - OPTIMAL_RATIO) < 0.10)
            return ChatFormatting.GREEN;
        return ChatFormatting.YELLOW;
    }
}
