package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.gregtechceu.gtceu.utils.GTMath;
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

// Code from CosmicCore credits to them and Ghostipedia thanks for the authorization to reuse the code
// https://github.com/Frontiers-PackForge/CosmicCore/blob/main-1.20.1-forge/src/main/java/com/ghostipedia/cosmiccore/common/machine/multiblock/multi/logic/ExoticCombustionEngineMachine.java

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMRGenerator extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SMRGenerator.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    // TODO: CosmicCore Lubricants for efficiency bonus

    private FluidStack currentLubricant;
    private FluidStack currentBooster;
    @Getter
    private final int tier;
    // Probably a bad idea, most likely a better way to do this
    @DescSynced
    private static final Object2IntMap<FluidStack> lubricantTiers = new Object2IntOpenHashMap<>();
    @DescSynced
    private static final Object2IntMap<FluidStack> boostingTiers = new Object2IntOpenHashMap<>();
    private int runningTimer = 0;
    static {
        // Boosting Tiers
        boostingTiers.put(GTMaterials.Oxygen.getFluid(1), 1);
        boostingTiers.put(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1), 4);
        boostingTiers.put(TFGHelpers.getMaterial("booster_t3").getFluid(1), 16);
        // Lubricant Tiers
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
        if (EUt * recipe.duration < 720) {
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
                consumptionMult = boostingTiers.getInt(engineMachine.currentBooster) * 2;
                eutMultiplier = actualParallel * (boostingTiers.getInt(engineMachine.currentBooster));
            }

            return ModifierFunction.builder()
                    .inputModifier(ContentModifier.multiplier(consumptionMult * actualParallel))
                    .outputModifier(ContentModifier.multiplier(actualParallel))
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
            if ((EUt / recipe.parallels) * duration < 720) {
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
                consumptionRate = 4;
                tickCycle = 1;
            } else if (currentBooster.isFluidEqual(GTMaterials.Acetone.getFluid(1))) {
                tickCycle = 2;
                consumptionRate = 1;
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
                consumptionRate = 1; // 1000/hr
            } else if (currentLubricant
                    .containsFluid(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(FluidStorageKeys.LIQUID, 1))) {
                tickCycle = 288;
                consumptionRate = 1; // 500/hr
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
        var voltageName = Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(getOverclockVoltage())]);
        var amperageName = currentBooster != null ? boostingTiers.getInt(currentBooster) * 3 : 1;
        if (recipeLogic.isSuspend() && !recipeLogic.getFancyTooltip().isEmpty()) {
            builder.addCustom(t -> t.add(recipeLogic.getFancyTooltip().get(0)));
            return;
        }
        builder.addCustom(t -> t.add(Component.translatable("gtceu.multiblock.max_energy_per_tick_amps",
                FormattingUtil.formatNumbers(getOverclockVoltage() * amperageName),
                amperageName, voltageName).withStyle(ChatFormatting.GRAY)));
        if (isActive() && isWorkingEnabled()) {
            builder.addCurrentEnergyProductionLine(
                    recipeLogic.getLastRecipe() != null ? recipeLogic.getLastRecipe().getOutputEUt().voltage() : 0);
        }

        builder.addFuelNeededLine(getRecipeFluidInputInfo(), recipeLogic.getDuration());

        if (isFormed && currentBooster != null) {
            builder.addCustom(tl -> tl.add(Component
                    .translatable("tfg.guid.smr_generator.booster_used",
                            Component.translatable(currentBooster.getTranslationKey()))
                    .withStyle(ChatFormatting.AQUA)));
        }

        if (isFormed && currentLubricant != null) {
            builder.addCustom(tl -> tl.add(Component
                    .translatable("tfg.gui.smr_generator.lubricant_used",
                            Component.translatable(currentLubricant.getTranslationKey()))
                    .withStyle(ChatFormatting.YELLOW)));
        }

        builder.addWorkingStatusLine();

        builder.addCustom(tl -> tl.add(
                Component.translatable("tfg.gui.smr_generator.credit")
                        .withStyle(ChatFormatting.GRAY)));
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
