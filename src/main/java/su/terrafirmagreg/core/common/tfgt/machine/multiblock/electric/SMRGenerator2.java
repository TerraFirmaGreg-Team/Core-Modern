package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import su.terrafirmagreg.core.utils.TFGHelpers;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMRGenerator2 extends TieredWorkableElectricMultiblockMachine {

    // TODO: CosmicCore Lubricants for efficiency bonus

    private long lubricantAmountForDisplay = 0;
    private GTRecipe lastUsedRecipe = null;

    private FluidStack currentLubricant;
    private FluidStack currentBooster;
    // Probably a bad idea, most likely a better way to do this
    private static final Object2IntMap<FluidStack> lubricantTiers = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<FluidStack> boostingTiers = new Object2IntOpenHashMap<>();
    private int runningTimer = 0;

    static {
        var ozone = TFGHelpers.getMaterial("ozone");
        var cyclohex_diperoxide = TFGHelpers.getMaterial("cyclohex_diperoxide");
        var booster_t3 = TFGHelpers.getMaterial("booster_t3");
        var polyalkylene_lubricant = TFGHelpers.getMaterial("polyalkylene_lubricant");

        if (ozone != null && !ozone.isNull())
            boostingTiers.put(ozone.getFluid(1), 1);
        if (cyclohex_diperoxide != null && !cyclohex_diperoxide.isNull())
            boostingTiers.put(cyclohex_diperoxide.getFluid(1), 4);
        if (booster_t3 != null && !booster_t3.isNull())
            boostingTiers.put(booster_t3.getFluid(1), 8);

        lubricantTiers.put(GTMaterials.Lubricant.getFluid(1), 2);
        if (polyalkylene_lubricant != null && !polyalkylene_lubricant.isNull())
            lubricantTiers.put(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(1), 4);
    }

    public SMRGenerator2(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        recipeLogic.setRegressWhenWaiting(false);
    }

    private boolean isIntakesObstructed() {
        var dir = this.getFrontFacing();
        boolean mutableXZ = dir.getAxis() == Direction.Axis.Z;
        var centerPos = this.getBlockPos().relative(dir);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0)
                    continue;
                var blockPos = centerPos.offset(mutableXZ ? x : 0, y, mutableXZ ? 0 : x);
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    private void updateFluids() {

        if (currentBooster != null && currentBooster.isEmpty()) {
            currentBooster = null;
        }

        if (currentLubricant != null && currentLubricant.isEmpty()) {
            currentLubricant = null;
        }

        var fluidHolders = Objects
                .requireNonNullElseGet(getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP),
                        Collections::<IRecipeHandler<?>>emptyList)
                .stream()
                .map(container -> container.getContents().stream().filter(FluidStack.class::isInstance)
                        .map(FluidStack.class::cast).toList())
                .filter(container -> !container.isEmpty())
                .toList();

        currentBooster = null;
        currentLubricant = null;

        for (var fluidHolder : fluidHolders) {
            for (var fluidStack : fluidHolder) {
                if (boostingTiers.containsKey(fluidStack)) {
                    if (currentBooster == null || boostingTiers.getInt(fluidStack) > boostingTiers.getInt(currentBooster)) {
                        currentBooster = fluidStack;
                    }
                } else if (lubricantTiers.containsKey(fluidStack)) {
                    if (currentLubricant == null || lubricantTiers.getInt(fluidStack) > lubricantTiers.getInt(currentLubricant)) {
                        currentLubricant = fluidStack;
                    }
                }
            }
        }
    }

    @Override
    public long getOverclockVoltage() {
        return GTValues.V[getTier()];
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof SMRGenerator2 engineMachine)) {
            return RecipeModifier.nullWrongType(SMRGenerator2.class, machine);
        }
        long EUt = recipe.getOutputEUt().voltage();
        if (EUt * recipe.duration < 1) {
            return ModifierFunction.NULL;
        }
        var fluidHolders = Objects
                .requireNonNullElseGet(engineMachine.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP),
                        Collections::<IRecipeHandler<?>>emptyList)
                .stream()
                .map(container -> container.getContents().stream().filter(FluidStack.class::isInstance)
                        .map(FluidStack.class::cast).toList())
                .filter(container -> !container.isEmpty())
                .toList();

        for (var fluidHolder : fluidHolders) {
            for (var fluidStack : fluidHolder) {
                if (boostingTiers.containsKey(fluidStack)) {
                    if (engineMachine.currentBooster == null || engineMachine.currentBooster.isEmpty() ||
                            boostingTiers.getInt(fluidStack) > boostingTiers.getInt(engineMachine.currentBooster)) {
                        engineMachine.currentBooster = fluidStack;
                    }
                } else if (lubricantTiers.containsKey(fluidStack)) {
                    if (engineMachine.currentLubricant == null || engineMachine.currentLubricant.isEmpty() ||
                            lubricantTiers.getInt(fluidStack) > lubricantTiers.getInt(engineMachine.currentLubricant)) {
                        engineMachine.currentLubricant = fluidStack;
                    }
                }
            }
        }

        // Has a variant of lubricant
        if (EUt > 0 &&
                engineMachine.currentLubricant != null &&
                !engineMachine.currentLubricant.isEmpty()) {

            int maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt);
            int actualParallel = ParallelLogic.getParallelAmount(engineMachine, recipe, maxParallel);
            int tier = lubricantTiers.getInt(engineMachine.currentLubricant);
            float durationModifier = (lubricantTiers.getInt(engineMachine.currentLubricant) / 2.0F);
            double eutMultiplier = 1;
            int consumptionMult = 1;
            if (engineMachine.currentBooster == null || engineMachine.currentBooster.isEmpty()) {
                eutMultiplier = actualParallel;
            } else {
                consumptionMult = 1;
                //boostingTiers.getInt(engineMachine.currentBooster); - AJOUTER SI BESOIN QUE LE GENERATEUR CONSOMME PLUS
                eutMultiplier = actualParallel * (boostingTiers.getInt(engineMachine.currentBooster) * 2);
            }

            return ModifierFunction.builder()
                    .inputModifier(ContentModifier.multiplier(consumptionMult * actualParallel))
                    .outputModifier(ContentModifier.multiplier(consumptionMult * actualParallel))
                    .durationMultiplier(durationModifier)
                    .eutMultiplier(eutMultiplier)
                    .parallels(actualParallel)
                    .build();

        }
        return ModifierFunction.NULL;
    }

    @Override
    public boolean onWorking() {
        updateFluids();
        boolean value = super.onWorking();

        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe != null) {
            lastUsedRecipe = recipe;
        }
        if (currentBooster != null && !currentBooster.isEmpty()) {
            int consumptionRate = -1;
            int tickCycle = -1;
            if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("ozone").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 144;
            } else if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("cyclohex_diperoxide").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 36;
            } else if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("booster_t3").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 36;
            }
            if (tickCycle != -1 && runningTimer % tickCycle == 0) {
                if (consumptionRate != -1 && currentBooster.getAmount() >= consumptionRate) {
                    currentBooster.shrink(consumptionRate);
                }
            }
        }
        // Currently all lubricants are the same, however this may change, so assume this is left this way intentionally
        // (Anyone else who reads this)
        if (currentLubricant != null && !currentLubricant.isEmpty()) {
            int consumptionRate = -1;
            int tickCycle = -1;
            if (currentLubricant.containsFluid(GTMaterials.Lubricant.getFluid(1))) {
                tickCycle = 72; // 72000 ticks per hour divide by tickCycle to know how much is getting consummed
                consumptionRate = 1; // 1000/hr
            } else if (currentLubricant.containsFluid(
                    (TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(1)))) {
                tickCycle = 144;
                consumptionRate = 1; // 500/hr
            } else if (currentLubricant.containsFluid(
                    (TFGHelpers.getMaterial("uranium_waste").getFluid(1)))) {
                tickCycle = 288;
                consumptionRate = 1; // 250/hr
            }
            if (tickCycle != -1 && runningTimer % tickCycle == 0) {
                if (consumptionRate != -1 && currentLubricant.getAmount() >= consumptionRate) {
                    currentLubricant.shrink(consumptionRate);
                } else {
                    recipeLogic.interruptRecipe();
                }
            }
        } else if (currentLubricant != null) {
            recipeLogic.interruptRecipe();
        }

        // Met à jour l'affichage côté GUI
        lubricantAmountForDisplay = (currentLubricant != null && !currentLubricant.isEmpty()) ? currentLubricant.getAmount() : 0;

        runningTimer++;
        if (runningTimer > 72000)
            runningTimer %= 72000;

        return value;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);

        long rawVoltage = getOverclockVoltage();
        int baseTier = GTUtil.getFloorTierByVoltage(rawVoltage);
        long baseVoltage = GTValues.VEX[baseTier];

        int amperage = 1;

        if (currentBooster != null && !currentBooster.isEmpty()) {
            int tier = boostingTiers.getInt(currentBooster);
            if (tier > 0)
                amperage = tier * 2;
        }

        long totalEUt = rawVoltage * amperage;

        int displayTier = GTUtil.getFloorTierByVoltage(totalEUt);
        long displayVoltage = GTValues.VEX[displayTier];

        amperage = (int) (totalEUt / displayVoltage);
        if (amperage < 1) {
            amperage = 1;
        }

        final int amperageFinal = amperage;
        final long displayVoltageFinal = displayVoltage;
        final Component voltageNameFinal = Component.literal(GTValues.VNF[displayTier]);

        if (recipeLogic.isSuspend()) {
            return widgets;
        }

        var combined = Component.empty();

        Component prefix = Component.translatable("tfg.gui.max_energy_per_tick_amps.prefix")
                .withStyle(ChatFormatting.WHITE);

        Component middle = Component.literal(
                FormattingUtil.formatNumbers(displayVoltageFinal * amperageFinal)
                        + " (" + amperageFinal + "A ")
                .withStyle(ChatFormatting.GRAY);

        Component suffix = Component.literal(")").withStyle(ChatFormatting.GRAY);

        combined.append(prefix)
                .append(Component.literal(" "))
                .append(middle)
                .append(voltageNameFinal)
                .append(suffix);

        widgets.add(Text.of(combined).asWidget());

        // EU Generation if active with EU in green

        if (isActive() && isWorkingEnabled()) {
            long euOutput = recipeLogic.getLastRecipe() != null ? recipeLogic.getLastRecipe().getOutputEUt().voltage() : 0;

            MutableComponent text = Component.literal("Energy Output: ").withStyle(ChatFormatting.WHITE);
            Component euValue = Component.literal(FormattingUtil.formatNumbers(euOutput)).withStyle(ChatFormatting.GREEN);
            Component unit = Component.literal(" EU/t").withStyle(ChatFormatting.WHITE);
            text.append(euValue).append(unit);

            widgets.add(Text.of(text).asWidget());
        }

        // Consumes working

        GTRecipe recipe = lastUsedRecipe; // <-- utilise la dernière recette mémorisée
        if (recipe == null)
            return widgets;

        FluidStack requiredFluid = RecipeHelper.getInputFluids(recipe).isEmpty()
                ? FluidStack.EMPTY
                : RecipeHelper.getInputFluids(recipe).get(0);
        if (requiredFluid.isEmpty())
            return widgets;

        long EUt = recipe.getOutputEUt().voltage();
        long maxVoltage = getMaxVoltage();
        int maxParallel = (int) Math.max(1, maxVoltage / EUt);
        int actualParallel = ParallelLogic.getParallelAmount(this, recipe, maxParallel);

        int tier = 1;
        if (currentLubricant != null && !currentLubricant.isEmpty())
            tier = lubricantTiers.getInt(currentLubricant);

        float durationMultiplier = tier / 2.0f;
        long totalFluid = Math.round(requiredFluid.getAmount() * actualParallel * 1F);

        widgets.add(Text.of(Component.translatable("tfg.gui.consumes")
                .append(Component.literal(FormattingUtil.formatNumbers(totalFluid) + " mB ").withStyle(ChatFormatting.RED))
                .append(Component.translatable("tfg.gui.per_cycle").withStyle(ChatFormatting.GRAY))).asWidget());

        // How many ticks in a cycle

        int duration = recipe.duration;

        widgets.add(Text.of(Component.translatable("tfg.gui.cycle_duration")
                .append(Component.literal(duration + " ticks").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" (≈" + String.format("%.2f", duration / 20.0) + " s)").withStyle(ChatFormatting.GREEN))).asWidget());

        // Booster

        long boosterAmountForDisplay = (currentBooster != null && !currentBooster.isEmpty())
                ? currentBooster.getAmount()
                : 0;

        if (isFormed && currentBooster != null && !currentBooster.isEmpty()) {
            int tierBooster = boostingTiers.getInt(currentBooster);

            // Récupération de la consommation réelle (exactement la même logique que onWorking)
            int consumptionRate = -1;
            int tickCycle = -1;

            if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("ozone").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 144;
            } else if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("cyclohex_diperoxide").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 36;
            } else if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("booster_t3").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 36;
            }

            // Durée restante
            long totalTicksRemaining = 0;
            if (consumptionRate > 0 && tickCycle > 0) {
                double mB_per_tick = (double) consumptionRate / (double) tickCycle;
                totalTicksRemaining = (long) (boosterAmountForDisplay / mB_per_tick);
            }

            long totalSeconds = totalTicksRemaining / 20;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            String timeFormatted = String.format("%dh %02dm", hours, minutes);

            // Ligne 1 : nom du booster
            widgets.add(Text.lang("tfg.gui.smr_generator.booster_used",
                    Component.translatable(currentBooster.getTranslationKey()))
                    .withStyle(ChatFormatting.AQUA).asWidget());

            // Ligne 2 : détails du booster
            widgets.add(Text.str("[Boost: x" + tierBooster
                    + ", Lasts: " + timeFormatted + "]")
                    .withStyle(ChatFormatting.AQUA).asWidget());

        }

        // Lubricant

        lubricantAmountForDisplay = (currentLubricant != null && !currentLubricant.isEmpty()) ? currentLubricant.getAmount() : 0;

        if (isFormed && currentLubricant != null && !currentLubricant.isEmpty()) {
            int tierLubricant = lubricantTiers.getInt(currentLubricant);

            int ticksPerUnit = currentLubricant.containsFluid(GTMaterials.Lubricant.getFluid(1)) ? 72
                    : currentLubricant.containsFluid(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(FluidStorageKeys.LIQUID, 1)) ? 144
                            : 1;

            long totalTicksRemaining = lubricantAmountForDisplay * ticksPerUnit;

            long totalSeconds = totalTicksRemaining / 20;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            String timeFormatted = String.format("%dh %02dm", hours, minutes);

            // Ligne 1 : nom du lubricant
            widgets.add(Text.lang("tfg.gui.smr_generator.lubricant_used",
                    Component.translatable(currentLubricant.getTranslationKey()))
                    .withStyle(ChatFormatting.YELLOW).asWidget());

            // Ligne 2 : détails du lubricant
            widgets.add(
                    Text.str("[Boost: x" + (tierLubricant / 2)
                            + ", Lasts: " + timeFormatted + "]")
                            .withStyle(ChatFormatting.YELLOW).asWidget());

        }

        return widgets;
    }

    @Nullable
    public String getRecipeFluidInputInfo() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe == null) {
            Iterator<GTRecipe> iterator = recipeLogic.searchRecipe();
            recipe = iterator != null && iterator.hasNext() ? iterator.next() : null;
            if (recipe == null)
                return null;
        }
        FluidStack requiredFluidInput = RecipeHelper.getInputFluids(recipe).get(0);

        long ocAmount = getMaxVoltage() / recipe.getOutputEUt().voltage();
        int neededAmount = GTMath.saturatedCast(ocAmount * requiredFluidInput.getAmount());
        return ChatFormatting.RED + FormattingUtil.formatNumbers(neededAmount) + "mB";
    }

}
