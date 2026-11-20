package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import fi.dea.mc.deafission.common.data.machine.hb.HeatHatchMachine;
import fi.dea.mc.deafission.common.data.recipe.HeatIngredient;
import fi.dea.mc.deafission.common.data.recipe.HeatRecipeCapability;

/**
 * Heat-consuming multiblock (Ostrum Linear Accelerator).
 * <p>
 * GTm8: Dea's Fission's AuxExchangerMachine is an empty subclass of WorkableMultiblockMachine, so we extend
 * WorkableElectricMultiblockMachine directly to get energy handling, tier and the standard MUI display for free,
 * and only add the heat lines through the definition's additionalDisplay.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomAuxExchangerMachine extends WorkableElectricMultiblockMachine {

    @Nullable
    protected HeatHatchMachine cachedHeatHatch;

    public CustomAuxExchangerMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        this.cachedHeatHatch = findHeatHatch();
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        this.cachedHeatHatch = null;
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        this.cachedHeatHatch = null;
    }

    @Nullable
    private HeatHatchMachine findHeatHatch() {
        return getParts().stream()
                .filter(HeatHatchMachine.class::isInstance)
                .map(HeatHatchMachine.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    protected HeatHatchMachine getHeatHatch() {
        if (cachedHeatHatch == null && isFormed())
            cachedHeatHatch = findHeatHatch();
        return cachedHeatHatch;
    }

    @Nullable
    private HeatIngredient currentHeatIngredient() {
        GTRecipe recipe = getRecipeLogic().getLastRecipe();
        if (recipe == null)
            return null;
        var heatInputs = recipe.inputs.get(HeatRecipeCapability.CAP);
        if (heatInputs == null || heatInputs.isEmpty())
            return null;
        return heatInputs.get(0).content() instanceof HeatIngredient heat ? heat : null;
    }

    private long totalHeat() {
        HeatHatchMachine hatch = getHeatHatch();
        return hatch == null ? 0 : (long) hatch.heatHandler.getTotalContentAmount();
    }

    private int recipeTemperature() {
        HeatIngredient heat = currentHeatIngredient();
        return heat == null ? 0 : (int) heat.temperature;
    }

    private long heatRequired() {
        HeatIngredient heat = currentHeatIngredient();
        return heat == null ? 0 : (long) heat.heat;
    }

    private int batteryTemperature() {
        GTRecipe recipe = getRecipeLogic().getLastRecipe();
        HeatHatchMachine hatch = getHeatHatch();
        if (recipe == null || hatch == null)
            return 0;
        return (int) hatch.heatHandler.getMinimumParticipantTemperature(recipe);
    }

    public List<IWidget> createHeatDisplayWidgets(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        BooleanSyncValue hasHatch = syncManager.getOrCreateSyncHandler("tfgAuxHasHeatHatch", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> isFormed() && getHeatHatch() != null));
        BooleanSyncValue hasHeatRecipe = syncManager.getOrCreateSyncHandler("tfgAuxHasHeatRecipe",
                BooleanSyncValue.class, () -> new BooleanSyncValue(() -> currentHeatIngredient() != null));
        LongSyncValue totalHeat = syncManager.getOrCreateSyncHandler("tfgAuxTotalHeat", LongSyncValue.class,
                () -> new LongSyncValue(this::totalHeat));
        IntSyncValue recipeTemp = syncManager.getOrCreateSyncHandler("tfgAuxRecipeTemp", IntSyncValue.class,
                () -> new IntSyncValue(this::recipeTemperature));
        IntSyncValue batteryTemp = syncManager.getOrCreateSyncHandler("tfgAuxBatteryTemp", IntSyncValue.class,
                () -> new IntSyncValue(this::batteryTemperature));
        LongSyncValue heatRequired = syncManager.getOrCreateSyncHandler("tfgAuxHeatRequired", LongSyncValue.class,
                () -> new LongSyncValue(this::heatRequired));

        BooleanSupplier showHeat = hasHatch::getBoolValue;
        BooleanSupplier showRecipe = () -> hasHatch.getBoolValue() && hasHeatRecipe.getBoolValue();

        widgets.add(Text.lang("tfg.machine.aux_exchanger.heat.title")
                .withStyle(ChatFormatting.GOLD)
                .asWidget()
                .setEnabledIf(w -> showHeat.getAsBoolean()));

        widgets.add(labeledValue("tfg.machine.aux_exchanger.recipe_temperature",
                () -> Component.literal(FormattingUtil.formatNumbers(recipeTemp.getIntValue()) + "°")
                        .withStyle(ChatFormatting.GOLD),
                "tfg.machine.aux_exchanger.recipe_temperature.desc", showRecipe));

        widgets.add(labeledValue("tfg.machine.aux_exchanger.battery_temperature",
                () -> Component.literal(FormattingUtil.formatNumbers(batteryTemp.getIntValue()) + "°")
                        .withStyle(ChatFormatting.YELLOW),
                "tfg.machine.aux_exchanger.battery_temperature.desc", showRecipe));

        widgets.add(labeledValue("tfg.machine.aux_exchanger.hu_consumed",
                () -> Component.literal(FormattingUtil.formatNumbers(heatRequired.getLongValue()) + " HU/recipe")
                        .withStyle(ChatFormatting.RED),
                "tfg.machine.aux_exchanger.hu_consumed.desc", showRecipe));

        widgets.add(labeledValue("tfg.machine.aux_exchanger.total_hu",
                () -> Component.literal(FormattingUtil.formatNumbers(totalHeat.getLongValue()) + " HU")
                        .withStyle(ChatFormatting.AQUA),
                "tfg.machine.aux_exchanger.total_hu.desc", showHeat));

        widgets.add(labeledValue("tfg.machine.aux_exchanger.speed_modifier",
                () -> {
                    int tempRecipe = recipeTemp.getIntValue();
                    double ratio = tempRecipe > 0 ? (double) batteryTemp.getIntValue() / tempRecipe : 0;
                    // duration multiplier = 1 / 2^ratio  ->  speed = 2^ratio
                    int speedPercent = (int) Math.round(Math.pow(2, ratio) * 100);
                    return Component.literal(speedPercent + "%")
                            .withStyle(speedPercent >= 100 ? ChatFormatting.GREEN : ChatFormatting.RED);
                },
                "tfg.machine.aux_exchanger.speed_modifier.desc",
                () -> showRecipe.getAsBoolean() && recipeTemp.getIntValue() > 0));

        return widgets;
    }

    private static IWidget labeledValue(String labelKey, Supplier<Component> value, String hoverKey,
            BooleanSupplier enabled) {
        return Text.dynamic(() -> Component.translatable(labelKey)
                .withStyle(ChatFormatting.WHITE)
                .append(value.get()))
                .asWidget()
                .tooltip(new RichTooltip().add(Component.translatable(hoverKey).withStyle(ChatFormatting.GRAY)))
                .setEnabledIf(w -> enabled.getAsBoolean());
    }
}
