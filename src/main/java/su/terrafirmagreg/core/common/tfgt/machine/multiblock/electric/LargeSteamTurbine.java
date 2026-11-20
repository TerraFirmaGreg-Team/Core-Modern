package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import static com.gregtechceu.gtceu.api.GTValues.EV;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

public class LargeSteamTurbine extends TieredWorkableElectricMultiblockMachine {

    public static final int MIN_DURABILITY_TO_WARN = 10;

    private final long BASE_EU_OUTPUT;
    @Getter
    private final int tier;

    public LargeSteamTurbine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.tier = tier;
        this.BASE_EU_OUTPUT = GTValues.V[EV];
        recipeLogic.setRegressWhenWaiting(false);
    }

    @Nullable
    private RotorHolderPartMachine getRotorHolder() {
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof RotorHolderPartMachine rotorHolder) {
                return rotorHolder;
            }
        }
        return null;
    }

    @Override
    public long getOverclockVoltage() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return BASE_EU_OUTPUT * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    /**
     * @return EUt multiplier that should be applied to the turbine's output
     */
    protected double productionBoost() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return 1;
            return Math.pow(1.0 * currentSpeed / maxSpeed, 2);
        }
        return 0;
    }

    public boolean hasRotor() {
        var rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor();
    }

    public int getRotorSpeed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorSpeed();
        }
        return 0;
    }

    public int getMaxRotorHolderSpeed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getMaxRotorHolderSpeed();
        }
        return 0;
    }

    public int getTotalEfficiency() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getTotalEfficiency();
        }
        return -1;
    }

    public long getCurrentProduction() {
        return isActive() && recipeLogic.getLastUnrolledRecipe() != null ? recipeLogic.getLastUnrolledRecipe().getOutputEUt().getTotalEU() : 0;
    }

    public int getRotorDurabilityPercent() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorDurabilityPercent();
        }
        return -1;
    }

    private boolean isIntakesObstructed() {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                // Skip the controller block itself
                if (i == 0 && j == 0)
                    continue;
                var blockPos = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(),
                        isFlipped(),
                        i, j, 1);
                var blockState = this.getLevel().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////

    /**
     * Recipe Modifier for <b>Large Turbine Multiblocks</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is fast parallelized up to {@code (baseEUt * power) / recipeEUt} times.
     * Duration is then multiplied by the holder efficiency.
     * </p>
     *
     * @param machine a {@link LargeSteamTurbine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Turbine Multiblock and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof LargeSteamTurbine turbineMachine)) {
            return RecipeModifier.nullWrongType(LargeSteamTurbine.class, machine);
        }
        if (turbineMachine.isIntakesObstructed()) {
            return ModifierFunction.cancel(Component.translatable("gtceu.multiblock.turbine.obstructed"));
        }

        var rotorHolder = turbineMachine.getRotorHolder();
        if (rotorHolder == null)
            return ModifierFunction.cancel(Component.translatable("gtceu.multiblock.turbine.no_rotor"));

        EnergyStack EUt = recipe.getOutputEUt();
        long turbineMaxVoltage = turbineMachine.getOverclockVoltage();
        double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;

        if (EUt.isEmpty() || turbineMaxVoltage <= EUt.voltage() || holderEfficiency <= 0)
            return ModifierFunction.cancel(Component.translatable("gtceu.multiblock.turbine.no_rotor"));

        int maxParallel = (int) (turbineMaxVoltage / EUt.getTotalEU());
        if (turbineMaxVoltage % EUt.getTotalEU() != 0)
            maxParallel++;

        int actualParallel = ParallelLogic.getParallelAmountFast(turbineMachine, recipe, maxParallel);
        double eutMultiplier = (maxParallel == actualParallel) ? turbineMachine.productionBoost() * turbineMaxVoltage / EUt.voltage() : turbineMachine.productionBoost() * actualParallel;

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallel))
                .outputModifier(ContentModifier.multiplier(actualParallel))
                .eutMultiplier(eutMultiplier)
                .parallels(actualParallel)
                .durationMultiplier(holderEfficiency)
                .build();
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return true;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        BooleanSyncValue isActive = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isActive));

        BooleanSyncValue hasRotor = new BooleanSyncValue(this::hasRotor);
        IntSyncValue rotorSpeed = new IntSyncValue(this::getRotorSpeed);
        IntSyncValue maxRotorSpeed = new IntSyncValue(this::getMaxRotorHolderSpeed);
        IntSyncValue efficiency = new IntSyncValue(this::getTotalEfficiency);
        IntSyncValue rotorDurability = new IntSyncValue(this::getRotorDurabilityPercent);

        LongSyncValue euOutput = new LongSyncValue(this::getCurrentProduction);
        LongSyncValue maxOutput = new LongSyncValue(this::getOverclockVoltage);
        LongSyncValue turbineVoltage = new LongSyncValue(this::getDisplayRecipeVoltage);

        syncManager.syncValue("hasRotor", hasRotor);
        syncManager.syncValue("rotorSpeed", rotorSpeed);
        syncManager.syncValue("maxRotorSpeed", maxRotorSpeed);
        syncManager.syncValue("efficiency", efficiency);
        syncManager.syncValue("rotorDurability", rotorDurability);

        syncManager.syncValue("euOutput", euOutput);
        syncManager.syncValue("maxOutput", maxOutput);
        syncManager.syncValue("turbineVoltage", turbineVoltage);

        var hasNoRotorWarning = Text.lang("gtceu.multiblock.turbine.no_rotor")
                .withStyle(ChatFormatting.RED)
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && !hasRotor.getBoolValue());
        var rotorSpeedDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.rotor_speed",
                FormattingUtil.formatNumbers(rotorSpeed.getIntValue()),
                FormattingUtil.formatNumbers(maxRotorSpeed.getIntValue()))
                .withStyle(ChatFormatting.WHITE))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && hasRotor.getBoolValue());
        var turbineEfficiencyDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.efficiency",
                efficiency.getIntValue())
                .withStyle(ChatFormatting.WHITE))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && hasRotor.getBoolValue());
        var turbinePowerDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.energy_per_tick",
                FormattingUtil.formatNumbers(euOutput.getLongValue()),
                FormattingUtil.formatNumbers(maxOutput.getLongValue()))
                .withStyle(ChatFormatting.WHITE))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && hasRotor.getBoolValue() && isActive.getBoolValue());
        var rotorDurabilityDisplay = Text.dynamic(() -> Component.translatable("gtceu.multiblock.turbine.rotor_durability",
                rotorDurability.getIntValue())
                .withStyle(rotorDurability.getIntValue() > MIN_DURABILITY_TO_WARN ? ChatFormatting.WHITE : ChatFormatting.RED))
                .asWidget()
                .setEnabledIf(w -> isFormed.getBoolValue() && hasRotor.getBoolValue());

        List<IWidget> widgets = new ArrayList<>();

        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));

        widgets.add(GTMultiblockTextUtil.addEnergyUsageExactLine(this, syncManager, euOutput, turbineVoltage, true));
        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addRecipeTypeField(this, syncManager));

        widgets.add(hasNoRotorWarning);
        widgets.add(rotorSpeedDisplay);
        widgets.add(turbineEfficiencyDisplay);
        widgets.add(turbinePowerDisplay);
        widgets.add(rotorDurabilityDisplay);

        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addTotalRunsLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        widgets.addAll(GTMultiblockTextUtil.addRecipeFailReasonLines(this, syncManager));

        return widgets;
    }
}
