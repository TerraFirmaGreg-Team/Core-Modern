package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import static com.gregtechceu.gtceu.api.GTValues.IV;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

public class NuclearLargeTurbineMachine extends TieredWorkableElectricMultiblockMachine {

    public static final int MIN_DURABILITY_TO_WARN = 10;

    private final long BASE_EU_OUTPUT;
    @Getter
    private final int tier;

    public NuclearLargeTurbineMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.tier = tier;
        this.BASE_EU_OUTPUT = GTValues.V[IV];
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

    private boolean isIntakesObstructed() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder == null)
            return true;
        BlockPos rotorPos = getRotorHolder().getBlockPos();

        Level level = getLevel();
        Direction front = getFrontFacing();
        Direction right = front.getClockWise();

        boolean obstructed = false;

        // Vérifie les deux couches sous le rotor (-1 et -2)
        for (int yOffset = -1; yOffset >= -2; yOffset--) {
            BlockPos planeOrigin = rotorPos.offset(0, yOffset, 0);

            for (int z = -2; z <= 2; z++) {
                for (int x = -2; x <= 2; x++) {

                    // Coins (X) ignorés
                    if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                        continue;
                    }

                    BlockPos pos = planeOrigin
                            .relative(right, x)
                            .relative(front, z);

                    if (!level.getBlockState(pos).isAir()) {
                        obstructed = true;
                    }
                }
            }
        }

        // Vérifie les blocs uniques au-dessus du rotor (+5 à +8)
        for (int y = 5; y <= 8; y++) {
            BlockPos pos = rotorPos.above(y);
            if (!level.getBlockState(pos).isAir()) {
                return true;
            }
        }

        return obstructed;
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
        return isActive() && recipeLogic.getLastRecipe() != null ? recipeLogic.getLastRecipe().getOutputEUt().voltage() : 0;
    }

    public int getRotorDurabilityPercent() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            return rotorHolder.getRotorDurabilityPercent();
        }
        return -1;
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
     * @param machine a {@link LargeTurbineMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Turbine Multiblock and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof NuclearLargeTurbineMachine turbineMachine)) {
            return RecipeModifier.nullWrongType(NuclearLargeTurbineMachine.class, machine);
        }
        if (turbineMachine.isIntakesObstructed()) {
            return ModifierFunction.NULL;
        }
        var rotorHolder = turbineMachine.getRotorHolder();
        if (rotorHolder == null)
            return ModifierFunction.NULL;

        EnergyStack EUt = recipe.getOutputEUt();
        long turbineMaxVoltage = turbineMachine.getOverclockVoltage();
        double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;

        if (EUt.isEmpty() || turbineMaxVoltage <= EUt.voltage() || holderEfficiency <= 0)
            return ModifierFunction.NULL;

        // get the amount of parallel required to match the desired output voltage
        int maxParallel = (int) (turbineMaxVoltage / EUt.getTotalEU());
        int actualParallel = ParallelLogic.getParallelAmountFast(turbineMachine, recipe, maxParallel);
        double eutMultiplier = turbineMachine.productionBoost() * actualParallel;

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

    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);

        if (!isFormed())
            return widgets;

        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.getRotorEfficiency() > 0) {
            widgets.add(Text.lang("gtceu.multiblock.turbine.rotor_speed",
                    FormattingUtil.formatNumbers(rotorHolder.getRotorSpeed()),
                    FormattingUtil.formatNumbers(rotorHolder.getMaxRotorHolderSpeed())).asWidget());
            widgets.add(Text.lang("gtceu.multiblock.turbine.efficiency",
                    rotorHolder.getTotalEfficiency()).asWidget());

            long maxProduction = getOverclockVoltage();
            long currentProduction = getCurrentProduction();

            if (isActive()) {
                widgets.add(3, Text.lang("gtceu.multiblock.turbine.energy_per_tick",
                        FormattingUtil.formatNumbers(currentProduction),
                        FormattingUtil.formatNumbers(maxProduction)).asWidget());
            }

            int rotorDurability = rotorHolder.getRotorDurabilityPercent();
            if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                widgets.add(Text.lang("gtceu.multiblock.turbine.rotor_durability", rotorDurability).asWidget());
            } else {
                widgets.add(Text.lang("gtceu.multiblock.turbine.rotor_durability", rotorDurability)
                        .withStyle(ChatFormatting.RED).asWidget());
            }
        }

        return widgets;
    }
}
