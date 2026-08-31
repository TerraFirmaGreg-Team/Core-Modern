package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;

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

    @SaveField
    @SyncToClient
    private double gasModifier = 1.0;

    @SaveField
    @SyncToClient
    private int gasLevelPercent = 0;

    private final ConditionalSubscriptionHandler gasUpdateSubscription;

    public OreProcessingBeneathMachine(BlockEntityCreationInfo info) {
        super(info);
        this.gasUpdateSubscription = new ConditionalSubscriptionHandler(this, this::tickGasInfo, this::isFormed);
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        gasModifier = 1.0;
        gasLevelPercent = 0;
        syncDataHolder.markClientSyncFieldDirty("gasModifier");
        syncDataHolder.markClientSyncFieldDirty("gasLevelPercent");
        gasUpdateSubscription.updateSubscription();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        gasModifier = 1.0;
        gasLevelPercent = 0;
        syncDataHolder.markClientSyncFieldDirty("gasModifier");
        syncDataHolder.markClientSyncFieldDirty("gasLevelPercent");
        gasUpdateSubscription.updateSubscription();
    }

    // Gas Logic - Check the amount of gas

    private void tickGasInfo() {
        if (getOffsetTimer() % 20 != 0)
            return;

        long[] tankInfo = getGasTankInfo();
        if (tankInfo == null || tankInfo[1] == 0) {
            gasLevelPercent = 0;
            gasModifier = 0.0;
            syncDataHolder.markClientSyncFieldDirty("gasModifier");
            syncDataHolder.markClientSyncFieldDirty("gasLevelPercent");
            return;
        }
        double ratio = (double) tankInfo[0] / tankInfo[1];
        gasLevelPercent = (int) (ratio * 100);
        gasModifier = calculateGaussianModifier(ratio);

        syncDataHolder.markClientSyncFieldDirty("gasModifier");
        syncDataHolder.markClientSyncFieldDirty("gasLevelPercent");
    }

    @Nullable
    private long[] getGasTankInfo() {
        var handlers = Objects.requireNonNullElseGet(
                getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP),
                Collections::<IRecipeHandler<?>>emptyList);

        for (var handler : handlers) {
            if (handler instanceof NotifiableFluidTank tank) {
                for (int i = 0; i < tank.getTanks(); i++) {
                    FluidStack stack = tank.getFluidInTank(i);
                    if (!stack.isEmpty() && stack.getFluid().is(TFGTags.Fluids.OreProcGas)) { // Use a tag tfg:ore_proc_gas
                        long stored = stack.getAmount();
                        long capacity = tank.getTankCapacity(i);
                        return new long[] { stored, capacity };
                    }
                }
            }
        }
        return null;
    }

    private double calculateGaussianModifier(double ratio) {
        double exponent = -Math.pow(ratio - OPTIMAL_RATIO, 2) / (2 * SIGMA * SIGMA);
        return Math.exp(exponent);
    }

    // Recipe Logic

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!super.beforeWorking(recipe))
            return false;

        if (gasLevelPercent == 0 && getGasTankInfo() == null) {
            RecipeLogic.putFailureReason(this, recipe,
                    Component.translatable("tfg.machine.ore_processing_beneath.no_gas")
                            .withStyle(ChatFormatting.RED));
            return false;
        }

        double ratio = gasLevelPercent / 100.0;
        if (ratio < MIN_RATIO || ratio > MAX_RATIO) {
            RecipeLogic.putFailureReason(this, recipe,
                    Component.translatable("tfg.machine.ore_processing_beneath.gas_critical",
                            gasLevelPercent)
                            .withStyle(ChatFormatting.RED));
            return false;
        }

        return true;
    }

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

        double modifier = processor.gasModifier;

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

            var copied = new GTRecipe(r.recipeType,
                    r.inputs, newOutputs,
                    r.tickInputs, r.tickOutputs,
                    r.inputChanceLogics, r.outputChanceLogics,
                    r.tickInputChanceLogics, r.tickOutputChanceLogics,
                    r.conditions, r.ingredientActions,
                    r.data, r.duration, r.parallels, r.subtickParallels, r.batchParallels, r.recipeCategory, r.groupColor, r.keepSpoilingProgress);
            copied.ocLevel = r.ocLevel;
            return copied;
        };
    }

    // GUI

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        if (!isFormed())
            return widgets;

        if (gasLevelPercent == 0 && gasModifier == 0.0) {
            widgets.add(Text.lang("tfg.machine.ore_processing_beneath.no_gas")
                    .withStyle(ChatFormatting.RED).asWidget());
            return widgets;
        }

        double ratio = gasLevelPercent / 100.0;
        int modifierPercent = (int) (gasModifier * 100);

        ChatFormatting levelColor;
        if (ratio < MIN_RATIO || ratio > MAX_RATIO) {
            levelColor = ChatFormatting.RED;
        } else if (Math.abs(ratio - OPTIMAL_RATIO) < 0.10) {
            levelColor = ChatFormatting.GREEN;
        } else {
            levelColor = ChatFormatting.YELLOW;
        }

        widgets.add(Text.lang("tfg.machine.ore_processing_beneath.gas_level",
                gasLevelPercent).withStyle(levelColor).asWidget());
        widgets.add(Text.lang("tfg.machine.ore_processing_beneath.output_modifier",
                modifierPercent).withStyle(modifierPercent >= 90 ? ChatFormatting.GREEN : ChatFormatting.YELLOW).asWidget()
                .tooltip(t -> t.addLine(Component.translatable("tfg.machine.ore_processing_beneath.output_modifier.tooltip"))));

        var lastRecipe = getRecipeLogic().getLastRecipe();
        if (getRecipeLogic().isWorking() && lastRecipe != null && lastRecipe.parallels > 1) {
            customLines.add(Component.translatable("tfg.machine.ore_processing_beneath.parallel_info",
                    Component.literal(lastRecipe.parallels + "/" + MAX_PARALLELS).withStyle(ChatFormatting.AQUA))
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("tfg.machine.ore_processing_beneath.parallel_info.tooltip",
                                    MAX_PARALLELS)))));

            var tickFluids = lastRecipe.tickInputs.get(FluidRecipeCapability.CAP);
            if (tickFluids != null) {
                for (Content content : tickFluids) {
                    var ingredient = FluidRecipeCapability.CAP.of(content.content());
                    if (ingredient.getStacks().length > 0) {
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

                        customLines.add(Component.translatable("tfg.machine.ore_processing_beneath.fluid_consumption",
                                stack.getDisplayName(), amountText)
                                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("tfg.machine.ore_processing_beneath.fluid_consumption.tooltip")))));
                    }
                }
            }
        }

        return widgets;
    }
}
