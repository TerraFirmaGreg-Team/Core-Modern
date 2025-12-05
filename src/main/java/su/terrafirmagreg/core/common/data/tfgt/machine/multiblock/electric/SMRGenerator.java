package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
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
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import lombok.Getter;

import su.terrafirmagreg.core.common.TFGHelpers;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMRGenerator extends WorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SMRGenerator.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final int tier;

    static List<GTRecipe> lubricantRecipes = new ArrayList<>();
    static List<GTRecipe> boostRecipes = new ArrayList<>();

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

    private Optional<GTRecipe> activeBoost = Optional.empty();
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

        Optional<GTRecipe> lubeRecipe = lubricantRecipes.stream()
                .filter(lr -> RecipeHelper.matchRecipe(engineMachine, lr).isSuccess())
                .findFirst();

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
            if (lubeDuration == 0) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }

        // Booster — un seul booster
        if (boostDuration <= 0) {
            activeBoost = Optional.empty();
            boostDuration = 1;
            GTRecipe candidate = boostRecipes.get(boostRecipes.size() - 1);
            if (RecipeHelper.matchRecipe(this, candidate).isSuccess() &&
                    RecipeHelper.handleRecipeIO(this, candidate, IO.IN, getRecipeLogic().getChanceCaches()).isSuccess()) {
                activeBoost = Optional.of(candidate);
                boostDuration = candidate.data.getInt(DURATION_KEY);
            }
        }

        runningTimer++;
        boostDuration = Math.max(0, boostDuration - 1);
        lubeDuration = Math.max(0, lubeDuration - 1);
        if (runningTimer > 72000)
            runningTimer %= 72000;

        return value;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
