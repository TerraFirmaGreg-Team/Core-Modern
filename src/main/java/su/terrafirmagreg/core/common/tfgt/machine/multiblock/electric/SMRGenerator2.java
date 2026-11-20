package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.ParentWidget;

import su.terrafirmagreg.core.utils.TFGHelpers;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SMRGenerator2 extends TieredWorkableElectricMultiblockMachine {

    // TODO: CosmicCore Lubricants for efficiency bonus

    public record FluidBonus(int tier, int tickCycle) {
    }

    @Nullable
    private static Map<Fluid, FluidBonus> boosters;
    @Nullable
    private static Map<Fluid, FluidBonus> lubricants;

    @Nullable
    private FluidStack currentLubricant;
    @Nullable
    private FluidStack currentBooster;
    @Nullable
    private FluidStack activeBooster;
    @Nullable
    private GTRecipe lastUsedRecipe;
    private int runningTimer = 0;

    public SMRGenerator2(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        recipeLogic.setRegressWhenWaiting(false);
    }

    public static Map<Fluid, FluidBonus> getBoosters() {
        if (boosters == null) {
            Map<Fluid, FluidBonus> map = new HashMap<>();
            putBonus(map, "ozone", 1, 144);
            putBonus(map, "cyclohex_diperoxide", 4, 36);
            putBonus(map, "booster_t3", 8, 36);
            boosters = map;
        }
        return boosters;
    }

    public static Map<Fluid, FluidBonus> getLubricants() {
        if (lubricants == null) {
            Map<Fluid, FluidBonus> map = new HashMap<>();
            map.put(GTMaterials.Lubricant.getFluid(), new FluidBonus(2, 72));
            putBonus(map, "polyalkylene_lubricant", 4, 144);
            lubricants = map;
        }
        return lubricants;
    }

    private static void putBonus(Map<Fluid, FluidBonus> map, String materialName, int tier, int tickCycle) {
        Material material = TFGHelpers.getMaterial(materialName);
        if (material != null && material.hasFluid())
            map.put(material.getFluid(), new FluidBonus(tier, tickCycle));
    }

    @Nullable
    private static FluidBonus getBonus(@Nullable FluidStack stack, Map<Fluid, FluidBonus> bonuses) {
        if (stack == null || stack.isEmpty())
            return null;
        return bonuses.get(stack.getFluid());
    }

    @Nullable
    private FluidStack findBest(Map<Fluid, FluidBonus> bonuses) {
        FluidStack best = null;
        int bestTier = 0;
        for (IRecipeHandler<?> handler : getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)) {
            for (Object content : handler.getContents()) {
                if (!(content instanceof FluidStack stack) || stack.isEmpty())
                    continue;
                FluidBonus bonus = bonuses.get(stack.getFluid());
                if (bonus != null && bonus.tier() > bestTier) {
                    best = stack;
                    bestTier = bonus.tier();
                }
            }
        }
        return best;
    }

    private long getStoredAmount(@Nullable FluidStack reference) {
        if (reference == null || reference.isEmpty())
            return 0;
        long total = 0;
        for (IRecipeHandler<?> handler : getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)) {
            for (Object content : handler.getContents()) {
                if (content instanceof FluidStack stack && stack.isFluidEqual(reference))
                    total += stack.getAmount();
            }
        }
        return total;
    }

    private boolean hasLubricant() {
        return findBest(getLubricants()) != null;
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        if (super.isRecipeLogicAvailable() && hasLubricant())
            return true;

        if (recipeLogic.isActive())
            recipeLogic.interruptRecipe();
        return false;
    }

    private void updateFluids() {
        currentBooster = findBest(getBoosters());
        currentLubricant = findBest(getLubricants());
    }

    private boolean consume(FluidStack fluid, int amount) {
        GTRecipe drain = GTRecipeBuilder.ofRaw().inputFluids(new FluidStack(fluid, amount)).buildRawRecipe();
        return RecipeHelper.handleRecipeIO(this, drain, IO.IN, recipeLogic.getChanceCaches()).isSuccess();
    }

    @Override
    public long getOverclockVoltage() {
        return GTValues.V[getTier()];
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof SMRGenerator2 smr)) {
            return RecipeModifier.nullWrongType(SMRGenerator2.class, machine);
        }
        EnergyStack EUt = recipe.getOutputEUt();
        if (EUt.isEmpty()) {
            return ModifierFunction.NULL;
        }

        smr.updateFluids();
        FluidBonus lubricant = getBonus(smr.currentLubricant, getLubricants());
        if (lubricant == null) {
            return ModifierFunction
                    .cancel(Component.translatable("gtceu.multiblock.large_combustion_engine.no_lubricant"));
        }

        int maxParallel = (int) (smr.getOverclockVoltage() / EUt.getTotalEU());
        int actualParallel = ParallelLogic.getParallelAmount(smr, recipe, maxParallel);
        FluidBonus booster = getBonus(smr.currentBooster, getBoosters());
        smr.activeBooster = booster == null ? null : new FluidStack(smr.currentBooster, 1);
        double eutMultiplier = booster == null ? actualParallel : actualParallel * booster.tier() * 2.0;

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallel))
                .outputModifier(ContentModifier.multiplier(actualParallel))
                .durationMultiplier(lubricant.tier() / 2.0)
                .eutMultiplier(eutMultiplier)
                .parallels(actualParallel)
                .build();
    }

    @Override
    public boolean onWorking() {
        updateFluids();
        boolean value = super.onWorking();

        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe != null) {
            lastUsedRecipe = recipe;
        }

        FluidBonus lubricant = getBonus(currentLubricant, getLubricants());
        if (lubricant == null || (runningTimer % lubricant.tickCycle() == 0 && !consume(currentLubricant, 1))) {
            recipeLogic.interruptRecipe();
            return false;
        }

        FluidBonus booster = getBonus(activeBooster, getBoosters());
        if (booster != null && runningTimer % booster.tickCycle() == 0 && !consume(activeBooster, 1)) {
            activeBooster = null;
            recipeLogic.interruptRecipe();
            return false;
        }

        runningTimer++;
        if (runningTimer > 72000)
            runningTimer %= 72000;

        return value;
    }

    private int getBoosterTier() {
        FluidBonus booster = getBonus(currentBooster, getBoosters());
        return booster == null ? 0 : booster.tier();
    }

    private int getLubricantTier() {
        FluidBonus lubricant = getBonus(currentLubricant, getLubricants());
        return lubricant == null ? 0 : lubricant.tier();
    }

    private long getRemainingTicks(@Nullable FluidStack stack, Map<Fluid, FluidBonus> bonuses) {
        FluidBonus bonus = getBonus(stack, bonuses);
        return bonus == null ? 0 : getStoredAmount(stack) * bonus.tickCycle();
    }

    private long getCurrentProduction() {
        GTRecipe recipe = recipeLogic.getLastRecipe();
        return isActive() && isWorkingEnabled() && recipe != null ? recipe.getOutputEUt().getTotalEU() : 0;
    }

    private long getFluidPerCycle() {
        if (lastUsedRecipe == null)
            return 0;
        List<FluidStack> inputs = RecipeHelper.getInputFluids(lastUsedRecipe);
        return inputs.isEmpty() ? 0 : inputs.get(0).getAmount();
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager, 260, 190).margin(4, 2));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);

        BooleanSyncValue isFormedSync = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        LongSyncValue overclockVoltageSync = new LongSyncValue(this::getOverclockVoltage);
        IntSyncValue boosterTierSync = new IntSyncValue(this::getBoosterTier);
        IntSyncValue lubricantTierSync = new IntSyncValue(this::getLubricantTier);
        LongSyncValue boosterTicksSync = new LongSyncValue(() -> getRemainingTicks(currentBooster, getBoosters()));
        LongSyncValue lubricantTicksSync = new LongSyncValue(() -> getRemainingTicks(currentLubricant, getLubricants()));
        LongSyncValue euOutputSync = new LongSyncValue(this::getCurrentProduction);
        LongSyncValue fluidPerCycleSync = new LongSyncValue(this::getFluidPerCycle);
        IntSyncValue cycleDurationSync = new IntSyncValue(() -> lastUsedRecipe == null ? 0 : lastUsedRecipe.duration);
        GenericSyncValue<Component> boosterNameSync = componentSync(
                () -> currentBooster == null ? Component.empty() : currentBooster.getDisplayName());
        GenericSyncValue<Component> lubricantNameSync = componentSync(
                () -> currentLubricant == null ? Component.empty() : currentLubricant.getDisplayName());

        syncManager.syncValue("smrOverclockVoltage", overclockVoltageSync);
        syncManager.syncValue("smrBoosterTier", boosterTierSync);
        syncManager.syncValue("smrLubricantTier", lubricantTierSync);
        syncManager.syncValue("smrBoosterTicks", boosterTicksSync);
        syncManager.syncValue("smrLubricantTicks", lubricantTicksSync);
        syncManager.syncValue("smrEuOutput", euOutputSync);
        syncManager.syncValue("smrFluidPerCycle", fluidPerCycleSync);
        syncManager.syncValue("smrCycleDuration", cycleDurationSync);
        syncManager.syncValue("smrBoosterName", boosterNameSync);
        syncManager.syncValue("smrLubricantName", lubricantNameSync);

        widgets.add(Text.dynamic(() -> computeMaxEnergyLine(overclockVoltageSync.getLongValue(),
                boosterTierSync.getIntValue())).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue()));

        widgets.add(Text.dynamic(() -> Component.literal("Energy Output: ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(FormattingUtil.formatNumbers(euOutputSync.getLongValue()))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" EU/t").withStyle(ChatFormatting.WHITE))).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && euOutputSync.getLongValue() > 0));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.gui.consumes")
                .append(Component.literal(FormattingUtil.formatNumbers(fluidPerCycleSync.getLongValue()) + " mB ")
                        .withStyle(ChatFormatting.RED))
                .append(Component.translatable("tfg.gui.per_cycle").withStyle(ChatFormatting.GRAY))).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && fluidPerCycleSync.getLongValue() > 0));

        widgets.add(Text.dynamic(() -> {
            int duration = cycleDurationSync.getIntValue();
            return Component.translatable("tfg.gui.cycle_duration")
                    .append(Component.literal(duration + " ticks").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (≈" + String.format("%.2f", duration / 20.0) + " s)")
                            .withStyle(ChatFormatting.GREEN));
        }).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && cycleDurationSync.getIntValue() > 0));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.gui.smr_generator.booster_used",
                boosterNameSync.getValue()).withStyle(ChatFormatting.AQUA)).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && boosterTierSync.getIntValue() > 0));
        widgets.add(Text.dynamic(() -> Component.literal("[Boost: x" + (boosterTierSync.getIntValue() * 2)
                + ", Lasts: " + formatTicks(boosterTicksSync.getLongValue()) + "]")
                .withStyle(ChatFormatting.AQUA)).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && boosterTierSync.getIntValue() > 0));

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.gui.smr_generator.lubricant_used",
                lubricantNameSync.getValue()).withStyle(ChatFormatting.YELLOW)).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && lubricantTierSync.getIntValue() > 0));
        widgets.add(Text.dynamic(() -> Component.literal("[Boost: x" + (lubricantTierSync.getIntValue() / 2)
                + ", Lasts: " + formatTicks(lubricantTicksSync.getLongValue()) + "]")
                .withStyle(ChatFormatting.YELLOW)).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && lubricantTierSync.getIntValue() > 0));

        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_combustion_engine.no_lubricant")
                .withStyle(ChatFormatting.RED)).asWidget()
                .setEnabledIf($ -> isFormedSync.getBoolValue() && lubricantTierSync.getIntValue() == 0));

        return widgets;
    }

    private static Component computeMaxEnergyLine(long rawVoltage, int boosterTier) {
        int amperage = boosterTier > 0 ? boosterTier * 2 : 1;
        long totalEUt = rawVoltage * amperage;
        int displayTier = GTUtil.getFloorTierByVoltage(totalEUt);
        long displayVoltage = GTValues.VEX[displayTier];
        int displayAmperage = Math.max(1, (int) (totalEUt / displayVoltage));

        return Component.empty()
                .append(Component.translatable("tfg.gui.max_energy_per_tick_amps.prefix").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" "))
                .append(Component.literal(FormattingUtil.formatNumbers(displayVoltage * displayAmperage)
                        + " (" + displayAmperage + "A ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(GTValues.VNF[displayTier]))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY));
    }

    private static String formatTicks(long ticks) {
        long totalSeconds = ticks / 20;
        return String.format("%dh %02dm", totalSeconds / 3600, (totalSeconds % 3600) / 60);
    }

    private static GenericSyncValue<Component> componentSync(Supplier<Component> getter) {
        return GenericSyncValue.builder(Component.class)
                .adapter(ByteBufAdapters.COMPONENT)
                .getter(getter)
                .build();
    }
}
