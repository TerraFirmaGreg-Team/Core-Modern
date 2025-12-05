package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import lombok.Getter;

import su.terrafirmagreg.core.common.TFGHelpers;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMRGenerator extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SMRGenerator.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final int tier;

    static List<GTRecipe> lubricantRecipes = List.of(new GTRecipe[0]);
    static List<GTRecipe> boostRecipes = List.of(new GTRecipe[0]);

    public static final String LUBRICATION_KEY = "lubrication";
    public static final String BOOST_KEY = "boost";
    public static final String DURATION_KEY = "duration";

    static {
        // ordered from best to worst so we can use findFirst
        lubricantRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(1))
                .addData(LUBRICATION_KEY, 4)
                .addData(DURATION_KEY, 288)
                .buildRawRecipe());
        lubricantRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.Lubricant.getFluid(1))
                .addData(LUBRICATION_KEY, 2)
                .addData(DURATION_KEY, 72)
                .buildRawRecipe());

        boostRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.Oxygen.getFluid(1))
                .addData(BOOST_KEY, 2)
                .addData(DURATION_KEY, 1)
                .buildRawRecipe());
        boostRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1))
                .addData(BOOST_KEY, 4)
                .addData(DURATION_KEY, 4)
                .buildRawRecipe());
        boostRecipes.add(GTRecipeBuilder.ofRaw()
                .inputFluids(TFGHelpers.getMaterial("booster_t3").getFluid(1))
                .addData(BOOST_KEY, 8)
                .addData(DURATION_KEY, 8)
                .buildRawRecipe());
    }

    private Optional<GTRecipe> activeBoost;
    private int runningTimer = 0;
    private int boostDuration = 0, lubeDuration = 0;

    public SMRGenerator(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    private boolean isIntakesObstructed() {
        var dir = this.getFrontFacing();
        boolean mutableXZ = dir.getAxis() == Direction.Axis.Z;
        var centerPos = this.getPos().relative(dir);
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

    @Override
    public long getOverclockVoltage() {
        return GTValues.V[tier];
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof SMRGenerator engineMachine)) {
            return RecipeModifier.nullWrongType(SMRGenerator.class, machine);
        }
        long EUt = recipe.getOutputEUt().voltage();
        if (EUt * recipe.duration < 1)
            return ModifierFunction.NULL;

        Optional<GTRecipe> lubeRecipe = lubricantRecipes.stream().filter(
                (lr) -> RecipeHelper.matchRecipe(engineMachine, lr).isSuccess()).findFirst();

        if (EUt > 0 && !engineMachine.isIntakesObstructed() && lubeRecipe.isPresent()) {
            int maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt);
            int actualParallel = ParallelLogic.getParallelAmount(engineMachine, recipe, maxParallel);
            int tier = lubeRecipe.get().data.getInt(LUBRICATION_KEY);
            float durationModifier = (tier / 2.0F);
            double eutMultiplier;
            int consumptionMult = 1;
            if (engineMachine.activeBoost.isPresent()) {
                consumptionMult = engineMachine.activeBoost.get().data.getInt(BOOST_KEY);
                eutMultiplier = actualParallel * (consumptionMult * 3);
            } else {
                eutMultiplier = actualParallel;
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

    // Méthode utilitaire pour consommer un fluide et mettre à jour la FluidStack locale

    public int maxBoostsAllowed() {
        int machineLimitedBoosts = GTUtil.getTierByVoltage(getMaxVoltage()) - getTier() - 1;
        return Math.min(machineLimitedBoosts, boostRecipes.size());
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        var recipe = recipeLogic.getLastRecipe();

        if (recipe != null) {
            long EUt = recipe.getOutputEUt().voltage();
            int duration = recipe.duration;
            if ((EUt / recipe.parallels) * duration < 1) {
                this.getRecipeLogic().setWaiting(Component.translatable("cosmiccore.errors.bad_fuel"));
            }
        }

        //
        // Consommation Lubricant
        if (lubeDuration <= 0) {
            for (GTRecipe lubeRecipe : lubricantRecipes) {
                if (RecipeHelper.matchRecipe(this, lubeRecipe).isSuccess() &&
                        RecipeHelper.handleRecipeIO(this, lubeRecipe, IO.IN, getRecipeLogic().getChanceCaches()).isSuccess()) {
                    lubeDuration = lubeRecipe.data.getInt(DURATION_KEY);
                    break;
                }
            }
            // handle no lube
            if (lubeDuration == 0) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }

        if (boostDuration <= 0) {
            activeBoost = Optional.empty();
            boostDuration = 1;
            for (int i = maxBoostsAllowed(); i >= 0; i--) {
                if (RecipeHelper.matchRecipe(this, boostRecipes.get(i)).isSuccess() &&
                        RecipeHelper.handleRecipeIO(this, boostRecipes.get(i), IO.IN, getRecipeLogic().getChanceCaches()).isSuccess()) {
                    activeBoost = Optional.of(boostRecipes.get(i));
                    boostDuration = activeBoost.get().data.getInt(DURATION_KEY);
                    break;
                }
            }
        }

        // Met à jour l'affichage côté GUI
        // lubricantAmountForDisplay = (currentLubricant != null && !currentLubricant.isEmpty()) ? currentLubricant.getAmount() : 0;

        runningTimer++;
        boostDuration--;
        lubeDuration--;
        if (runningTimer > 72000)
            runningTimer %= 72000;

        return value;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    // GUI for the Combustion Generator

    @Override
    public void addDisplayText(List<Component> textList) {
        /*
        MultiblockDisplayText.Builder builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive());

        if (recipeLogic.isSuspend() && !recipeLogic.getFancyTooltip().isEmpty()) {
            builder.addCustom(t -> t.add(recipeLogic.getFancyTooltip().get(0)));
            return;
        }

        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe == null) {
            Iterator<GTRecipe> iterator = recipeLogic.searchRecipe();
            recipe = iterator != null && iterator.hasNext() ? iterator.next() : null;
            if (recipe == null)
                return;
        }

        int duration = recipe.duration;
        long EUt = recipe.getOutputEUt().voltage();
        long absEUt = Math.abs(EUt);

        FluidStack requiredFluid = RecipeHelper.getInputFluids(recipe).isEmpty() ? FluidStack.EMPTY : RecipeHelper.getInputFluids(recipe).get(0);
        long baseFluidPerCycle = requiredFluid.getAmount();

        long maxVoltage = getMaxVoltage();
        long ocAmount = absEUt == 0 ? 1 : maxVoltage / absEUt;
        ocAmount = Math.max(1, ocAmount);
        long finalEUPt = absEUt * ocAmount;

        // Consumes working

        builder.addCustom(t -> t.add(Component.literal("Consumes ")
                .append(Component.literal(FormattingUtil.formatNumbers(baseFluidPerCycle) + " mB ").withStyle(ChatFormatting.RED))
                .append(Component.literal("per cycle").withStyle(ChatFormatting.GRAY))));

        // Generates

        int voltageTier = GTUtil.getTierByVoltage(finalEUPt); //.getFloorTierByVoltage

        builder.addCustom(t -> t.add(Component.literal("Generates ")
                .append(Component.literal(FormattingUtil.formatNumbers(finalEUPt) + " EU/t").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(GTValues.VNF[voltageTier]).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY))));

        // Cycle Duration

        builder.addCustom(t -> {
            double seconds = duration / 20.0;
            t.add(Component.literal("Cycle duration: ")
                    .append(Component.literal(duration + " ticks").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (≈" + String.format("%.2f", seconds) + " s)").withStyle(ChatFormatting.GREEN)));
        });

        // Booster

        if (isFormed && activeBoost.isPresent()) {
            int boosterTier = activeBoost.get().data.getInt(BOOST_KEY);
            FluidStack booster = (FluidStack) activeBoost.get().inputs.get(FluidRecipeCapability.CAP).get(0).getContent();
            builder.addCustom(tl -> tl.add(
                    Component.translatable("tfg.gui.smr_generator.booster_used",
                            Component.translatable(booster.getTranslationKey()),
                            Component.literal("x" + boosterTier).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.AQUA)));
        }

        // Lubricant

        if (isFormed && currentLubricant != null && !currentLubricant.isEmpty()) {
            int tier = lubricantTiers.getInt(currentLubricant);

            int ticksPerUnit = currentLubricant.containsFluid(GTMaterials.Lubricant.getFluid(1)) ? 72
                    : currentLubricant.containsFluid(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(FluidStorageKeys.LIQUID, 1)) ? 288
                            : 1;

            long totalTicksRemaining = lubricantAmountForDisplay * ticksPerUnit;

            long totalSeconds = totalTicksRemaining / 20;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            String timeFormatted = String.format("%dh %02dm", hours, minutes);

            builder.addCustom(tl -> tl.add(
                    Component.translatable("tfg.gui.smr_generator.lubricant_used",
                            Component.translatable(currentLubricant.getTranslationKey()))
                            .append(Component.literal(" [Boost: x" + (tier / 2) + ", Lasts: " +
                                    timeFormatted + "]").withStyle(ChatFormatting.YELLOW))
                            .withStyle(ChatFormatting.YELLOW)));
        }

        // Idle or Running Perfectly

        builder.addWorkingStatusLine();

        // Credit to Frontiers Team for the code

        builder.addCustom(tl -> tl.add(
                Component.translatable("tfg.gui.smr_generator.credit")
                        .withStyle(ChatFormatting.GRAY)));

         */
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_STEAM.get(false),
                () -> List.of(Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                this::isIntakesObstructed,
                () -> null));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
