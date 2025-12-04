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
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

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

    // Nouveau champ synchrone pour afficher la quantité actuelle côté GUI
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
        if (EUt * recipe.duration < 1)
            return ModifierFunction.NULL;

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

        if (EUt > 0 && !engineMachine.isIntakesObstructed() && engineMachine.currentLubricant != null &&
                !engineMachine.currentLubricant.isEmpty()) {
            int maxParallel = (int) (engineMachine.getOverclockVoltage() / EUt);
            int actualParallel = ParallelLogic.getParallelAmount(engineMachine, recipe, maxParallel);
            int tier = lubricantTiers.getInt(engineMachine.currentLubricant);
            float durationModifier = (tier / 2.0F);
            double eutMultiplier = 1;
            int consumptionMult = 1;
            if (engineMachine.currentBooster != null && !engineMachine.currentBooster.isEmpty()) {
                consumptionMult = boostingTiers.getInt(engineMachine.currentBooster);
                eutMultiplier = actualParallel * (boostingTiers.getInt(engineMachine.currentBooster) * 3);
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

    private void consumeFluidAndShrink(FluidStack stack, int tickCycle, int consumptionRate) {
        if (stack == null || stack.isEmpty() || tickCycle <= 0 || consumptionRate <= 0)
            return;

        if (runningTimer % tickCycle == 0 && stack.getAmount() >= consumptionRate) {
            boolean ok = consumeExactFluid(stack.getFluid(), consumptionRate);
            if (ok) {
                // Met à jour la FluidStack locale (affichage). cast sécurisé car amount fit dans int.
                stack.shrink((int) consumptionRate); // update visuelle
            } else if (stack == currentLubricant) {
                recipeLogic.interruptRecipe();
            }
        }
    }

    /**
     * Consomme exactement "amount" mB du fluide "wantedFluid" dans les capacités IO.IN.
     * Tentative robuste : on recherche NotifiableFluidTank soit directement soit dans une collection renvoyée par handler.getCapability().
     * Si on ne trouve que des copies via getContents(), shrink() en fallback.
     *
     * @return true si la consommation a réussi (la totalité a été drainée), false sinon.
     */
    private boolean consumeExactFluid(Fluid wanted, int amount) {
        if (wanted == null || amount <= 0)
            return false;

        int remaining = amount;

        var caps = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        if (caps == null || caps.isEmpty())
            return false;

        for (Object cap : caps) {

            if (!(cap instanceof NotifiableFluidTank tank))
                continue;

            var contents = tank.getContents();
            if (contents == null || contents.isEmpty())
                continue;

            Object obj = contents.get(0);
            if (!(obj instanceof FluidStack fs))
                continue;

            if (fs.isEmpty())
                continue;

            if (!fs.getFluid().isSame(wanted))
                continue;

            int available = fs.getAmount();
            int toDrain = Math.min(available, remaining);

            if (toDrain > 0)
                tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);

            remaining -= toDrain;

            if (remaining <= 0)
                return true;
        }

        return false;
    }

    /**
     * Tente de drainer depuis un NotifiableFluidTank donné.
     * Retourne la nouvelle valeur restante à drainer.
     */

    private int tryDrainFromTankDirect(NotifiableFluidTank tank, Fluid wantedFluid, int remaining) {
        if (tank == null || remaining <= 0)
            return remaining;
        try {
            var contents = tank.getContents();
            if (contents == null || contents.isEmpty())
                return remaining;
            Object maybe = contents.get(0);
            if (!(maybe instanceof FluidStack fs))
                return remaining;
            if (fs.isEmpty())
                return remaining;
            if (!fs.getFluid().isSame(wantedFluid))
                return remaining;

            int available = fs.getAmount();
            if (available <= 0)
                return remaining;

            int toDrain = Math.min(available, remaining);
            if (toDrain <= 0)
                return remaining;

            tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            remaining -= toDrain;
        } catch (Throwable t) {

        }
        return remaining;
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

        // Consommation Booster
        if (currentBooster != null && !currentBooster.isEmpty()) {
            int tickCycle = 1;
            if (currentBooster.isFluidEqual(GTMaterials.Oxygen.getFluid(1)))
                tickCycle = 1;
            else if (currentBooster.isFluidEqual(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 1)))
                tickCycle = 4;
            else if (currentBooster.isFluidEqual(TFGHelpers.getMaterial("booster_t3").getFluid(1)))
                tickCycle = 8;

            consumeFluidAndShrink(currentBooster, tickCycle, 1);
        }

        // Consommation Lubricant GUI ONLY
        if (currentLubricant != null && !currentLubricant.isEmpty()) {
            int tickCycle = currentLubricant.containsFluid(GTMaterials.Lubricant.getFluid(1)) ? 72
                    : currentLubricant.containsFluid(TFGHelpers.getMaterial("polyalkylene_lubricant").getFluid(FluidStorageKeys.LIQUID, 1)) ? 288 : 1;
            consumeFluidAndShrink(currentLubricant, tickCycle, 1);
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
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
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

        builder.addCustom(t -> t.add(Component.literal("Consumes ")
                .append(Component.literal(FormattingUtil.formatNumbers(baseFluidPerCycle) + " mB ").withStyle(ChatFormatting.RED))
                .append(Component.literal("per cycle").withStyle(ChatFormatting.GRAY))));

        int voltageTier = GTUtil.getFloorTierByVoltage(finalEUPt);

        builder.addCustom(t -> t.add(Component.literal("Generates ")
                .append(Component.literal(FormattingUtil.formatNumbers(finalEUPt) + " EU/t").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(GTValues.VNF[voltageTier]).setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY))));

        builder.addCustom(t -> {
            double seconds = duration / 20.0;
            t.add(Component.literal("Cycle duration: ")
                    .append(Component.literal(duration + " ticks").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (≈" + String.format("%.2f", seconds) + " s)").withStyle(ChatFormatting.GREEN)));
        });

        if (isFormed && currentBooster != null && !currentBooster.isEmpty()) {
            int boosterTier = boostingTiers.getInt(currentBooster);
            builder.addCustom(tl -> tl.add(
                    Component.translatable("tfg.gui.smr_generator.booster_used",
                            Component.translatable(currentBooster.getTranslationKey()),
                            Component.literal("x" + boosterTier).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.AQUA)));
        }

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
