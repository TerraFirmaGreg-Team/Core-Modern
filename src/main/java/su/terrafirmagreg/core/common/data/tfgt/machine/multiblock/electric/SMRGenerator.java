package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;

import su.terrafirmagreg.core.common.TFGHelpers;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMRGenerator extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SMRGenerator.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private FluidStack currentLubricant;
    private FluidStack currentBooster;
    @Getter
    private final int tier;
    @DescSynced
    private static final Object2IntMap<FluidStack> lubricantTiers = new Object2IntOpenHashMap<>();
    @DescSynced
    private static final Object2IntMap<FluidStack> boostingTiers = new Object2IntOpenHashMap<>();
    private int runningTimer = 0;

    // --- Nouveau champ pour stocker la quantité initiale de lubricant ---
    private long initialLubricantAmount = 0;

    // --- Nouveau champ synchrone pour afficher la quantité actuelle côté GUI ---
    @DescSynced
    private long lubricantAmountForDisplay = 0;

    static {
        boostingTiers.put(GTMaterials.Oxygen.getFluid(1), 2);
        boostingTiers.put(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1), 4);
        boostingTiers.put(TFGHelpers.getMaterial("booster_t3").getFluid(1), 8);
        lubricantTiers.put(GTMaterials.Lubricant.getFluid(1), 2);
        lubricantTiers.put(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(1), 4);
    }

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

                        // --- Réinitialise la quantité initiale au début de la recette ---
                        engineMachine.initialLubricantAmount = fluidStack.getAmount();
                    }
                }
            }
        }

        // Has a variant of lubricant
        if (EUt > 0 && !engineMachine.isIntakesObstructed() && engineMachine.currentLubricant != null &&
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
                consumptionMult = boostingTiers.getInt(engineMachine.currentBooster);
                eutMultiplier = actualParallel * (boostingTiers.getInt(engineMachine.currentBooster));
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
        boolean value = super.onWorking();
        var recipe = recipeLogic.getLastRecipe();
        if (recipe != null) {
            long EUt = recipe.getOutputEUt().voltage();
            int duration = recipe.duration;
            if ((EUt / recipe.parallels) * duration < 1) {
                this.getRecipeLogic().setWaiting(Component.translatable("cosmiccore.errors.bad_fuel"));
            }
        }
        if (currentBooster != null && !currentBooster.isEmpty()) {
            int consumptionRate = -1;
            int tickCycle = -1;
            if (currentBooster.isFluidEqual(GTMaterials.Oxygen.getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 1;
            } else if (currentBooster.isFluidEqual(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1))) {
                consumptionRate = 1;
                tickCycle = 72; // 1mb consumed every 72 ticks
            } else if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("booster_t3").getFluid(1))) {
                consumptionRate = 1;
                tickCycle = 144; // 1mb consumed every 144 ticks
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
                tickCycle = 72;
                consumptionRate = 1; // 1000mb/hr = 1mb every 72 ticks so
            } else if (currentLubricant
                    .containsFluid(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(FluidStorageKeys.LIQUID, 1))) {
                tickCycle = 288;
                consumptionRate = 1; // 500mb/hr
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

        // --- Met à jour la quantité synchrone pour l'affichage ---
        if (currentLubricant != null && !currentLubricant.isEmpty()) {
            lubricantAmountForDisplay = currentLubricant.getAmount();
        } else {
            lubricantAmountForDisplay = 0;
        }

        runningTimer++;
        if (runningTimer > 72000)
            runningTimer %= 72000;

        return value;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.Builder builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive());

        // Handle suspended GUI tooltip
        if (recipeLogic.isSuspend() && !recipeLogic.getFancyTooltip().isEmpty()) {
            builder.addCustom(t -> t.add(recipeLogic.getFancyTooltip().get(0)));
            return;
        }

        // --- Retrieve recipe ---
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

        FluidStack requiredFluid = RecipeHelper.getInputFluids(recipe).isEmpty()
                ? FluidStack.EMPTY
                : RecipeHelper.getInputFluids(recipe).get(0);

        long baseFluidPerCycle = requiredFluid.getAmount();

        long maxVoltage = getMaxVoltage();
        long ocAmount = absEUt == 0 ? 1 : maxVoltage / absEUt;
        ocAmount = Math.max(1, ocAmount);

        long finalEUPt = absEUt * ocAmount;

        // --- GUI Display Lines ---

        // Fluid consumption line
        builder.addCustom(t -> t.add(Component.literal("Consumes ")
                .append(Component.literal(FormattingUtil.formatNumbers(baseFluidPerCycle) + " mB ").withStyle(ChatFormatting.RED))
                .append(Component.literal("per cycle").withStyle(ChatFormatting.GRAY))));

        // EUt line with voltage tier
        int voltageTier = GTUtil.getFloorTierByVoltage(finalEUPt);
        Component voltageName = Component.literal(GTValues.VNF[voltageTier]);

        builder.addCustom(t -> t.add(Component.literal("Generates ")
                .append(Component.literal(FormattingUtil.formatNumbers(finalEUPt) + " EU/t").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(GTValues.VNF[voltageTier]).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY))));

        // Cycle duration
        builder.addCustom(t -> {
            double seconds = duration / 20.0;
            t.add(Component.literal("Cycle duration: ")
                    .append(Component.literal(duration + " ticks").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (≈" + String.format("%.2f", seconds) + " s)").withStyle(ChatFormatting.GREEN)));
        });

        // Booster line
        if (isFormed && currentBooster != null && !currentBooster.isEmpty()) {
            int boosterTier = boostingTiers.getInt(currentBooster);
            builder.addCustom(tl -> tl.add(
                    Component.translatable("tfg.gui.smr_generator.booster_used",
                            Component.translatable(currentBooster.getTranslationKey()),
                            Component.literal("x" + boosterTier).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.AQUA)));
        }

        // Lubricant
        if (isFormed && currentLubricant != null && !currentLubricant.isEmpty()) {
            int tier = lubricantTiers.getInt(currentLubricant);

            int ticksPerUnit = currentLubricant.containsFluid(GTMaterials.Lubricant.getFluid(1)) ? 72
                    : currentLubricant.containsFluid(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(FluidStorageKeys.LIQUID, 1)) ? 288
                            : 1;

            // Temps restant basé sur la quantité actuelle
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

        builder.addWorkingStatusLine();

        builder.addCustom(tl -> tl.add(
                Component.translatable("tfg.gui.smr_generator.credit")
                        .withStyle(ChatFormatting.GRAY)));
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
