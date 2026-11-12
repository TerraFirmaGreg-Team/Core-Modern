package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

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

import su.terrafirmagreg.core.TFGCore;

/**
 * Heat-consuming multiblock (Ostrum Linear Accelerator).
 * <p>
 * GTm8: Dea's Fission's AuxExchangerMachine is an empty subclass of WorkableMultiblockMachine, so we extend
 * WorkableElectricMultiblockMachine directly to get energy handling, tier and the standard MUI display for free,
 * and only add the heat lines on top.
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
        this.cachedHeatHatch = getParts().stream()
                .filter(HeatHatchMachine.class::isInstance)
                .map(HeatHatchMachine.class::cast)
                .findFirst()
                .orElse(null);
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
    private HeatIngredient currentHeatIngredient() {
        GTRecipe recipe = getRecipeLogic().getLastRecipe();
        if (recipe == null)
            return null;
        try {
            var heatInputs = recipe.inputs.get(HeatRecipeCapability.CAP);
            if (heatInputs == null || heatInputs.isEmpty())
                return null;
            return (HeatIngredient) heatInputs.get(0).content();
        } catch (ClassCastException e) {
            TFGCore.LOGGER.warn("Unexpected heat input content in recipe {}", recipe.id, e);
            return null;
        }
    }

    private long totalHeat() {
        return cachedHeatHatch == null ? 0 : (long) cachedHeatHatch.heatHandler.getTotalContentAmount();
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
        if (recipe == null || cachedHeatHatch == null)
            return 0;
        return (int) cachedHeatHatch.heatHandler.getMinimumParticipantTemperature(recipe);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        BooleanSyncValue hasHatch = syncManager.getOrCreateSyncHandler("tfgAuxHasHeatHatch", BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> isFormed() && cachedHeatHatch != null));
        BooleanSyncValue hasHeatRecipe = syncManager.getOrCreateSyncHandler("tfgAuxHasHeatRecipe",
                BooleanSyncValue.class, () -> new BooleanSyncValue(() -> currentHeatIngredient() != null));
        LongSyncValue totalHeat = syncManager.getOrCreateSyncHandler("tfgAuxTotalHeat", LongSyncValue.class,
                () -> new LongSyncValue(this::totalHeat, ignored -> {
                }));
        IntSyncValue recipeTemp = syncManager.getOrCreateSyncHandler("tfgAuxRecipeTemp", IntSyncValue.class,
                () -> new IntSyncValue(this::recipeTemperature));
        IntSyncValue batteryTemp = syncManager.getOrCreateSyncHandler("tfgAuxBatteryTemp", IntSyncValue.class,
                () -> new IntSyncValue(this::batteryTemperature));
        LongSyncValue heatRequired = syncManager.getOrCreateSyncHandler("tfgAuxHeatRequired", LongSyncValue.class,
                () -> new LongSyncValue(this::heatRequired, ignored -> {
                }));

        BooleanSupplier showHeat = hasHatch::getBoolValue;
        BooleanSupplier showRecipe = () -> hasHatch.getBoolValue() && hasHeatRecipe.getBoolValue();

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.aux_exchanger.heat.title")
                .withStyle(ChatFormatting.GOLD))
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

/*
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomAuxExchangerMachine extends AuxExchangerMachine
        implements ITieredMachine {

    protected EnergyContainerList energyContainer;

    @Nullable
    protected HeatHatchMachine cachedHeatHatch;

    @Getter
    protected int tier;

    public CustomAuxExchangerMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        this.energyContainer = getEnergyContainer();
        this.tier = GTUtil.getFloorTierByVoltage(getMaxVoltage());

        this.cachedHeatHatch = getParts().stream()
                .filter(p -> p instanceof HeatHatchMachine)
                .map(p -> (HeatHatchMachine) p)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        this.energyContainer = null;
        this.cachedHeatHatch = null;
        this.tier = 0;
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        this.energyContainer = null;
        this.cachedHeatHatch = null;
        this.tier = 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        int numParallels;
        int subtickParallels;
        int totalRuns;
        boolean exact;

        if (recipeLogic.isActive() && recipeLogic.getLastRecipe() != null) {
            numParallels = recipeLogic.getLastRecipe().parallels;
            subtickParallels = recipeLogic.getLastRecipe().subtickParallels;
            totalRuns = recipeLogic.getLastRecipe().getTotalRuns();
            exact = true;
        } else {
            numParallels = getParallelHatch()
                    .map(IParallelHatch::getCurrentParallel)
                    .orElse(0);
            subtickParallels = 0;
            totalRuns = 0;
            exact = false;
        }

        var builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1)
                .addTotalRunsLine(totalRuns)
                .addParallelsLine(numParallels, exact)
                .addSubtickParallelsLine(subtickParallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic);

        addHeatDisplayText(textList);

        textList.add(Component.empty());

        if (recipeLogic.isActive()) {
            textList.add(Component.translatable("tfg.machine.aux_echanger.output.title"));
            builder.addOutputLines(recipeLogic.getLastRecipe());
        }

        builder.addOutputLines(recipeLogic.getLastRecipe());

        getDefinition().getAdditionalDisplay().accept(this, textList);
        IDisplayUIMachine.super.addDisplayText(textList);
    }

    private void addHeatDisplayText(List<Component> textList) {
        if (!isFormed() || cachedHeatHatch == null)
            return;

        try {
            textList.add(Component.empty());
            textList.add(Component.translatable("tfg.machine.aux_exchanger.heat.title")
                    .withStyle(ChatFormatting.GOLD));

            var recipe = recipeLogic.getLastRecipe();
            long totalHeat = (long) cachedHeatHatch.heatHandler.getTotalContentAmount();

            if (recipe == null) {
                textList.add(labeledValue(
                        "tfg.machine.aux_exchanger.total_hu",
                        FormattingUtil.formatNumbers(totalHeat) + " HU",
                        ChatFormatting.AQUA,
                        "tfg.machine.aux_exchanger.total_hu.desc"));
                return;
            }

            var heatInputs = recipe.inputs.get(HeatRecipeCapability.CAP);
            if (heatInputs == null || heatInputs.isEmpty())
                return;

            HeatIngredient heatIngredient = (HeatIngredient) heatInputs.get(0).content;

            int tempRecipe = (int) heatIngredient.temperature;
            int tempBattery = (int) cachedHeatHatch.heatHandler.getMinimumParticipantTemperature(recipe);
            long heatRequired = (long) heatIngredient.heat;

            textList.add(labeledValue(
                    "tfg.machine.aux_exchanger.recipe_temperature",
                    FormattingUtil.formatNumbers(tempRecipe) + "°",
                    ChatFormatting.GOLD,
                    "tfg.machine.aux_exchanger.recipe_temperature.desc"));

            textList.add(labeledValue(
                    "tfg.machine.aux_exchanger.battery_temperature",
                    FormattingUtil.formatNumbers(tempBattery) + "°",
                    ChatFormatting.YELLOW,
                    "tfg.machine.aux_exchanger.battery_temperature.desc"));

            textList.add(labeledValue(
                    "tfg.machine.aux_exchanger.hu_consumed",
                    FormattingUtil.formatNumbers(heatRequired) + " HU/recipe",
                    ChatFormatting.RED,
                    "tfg.machine.aux_exchanger.hu_consumed.desc"));

            textList.add(labeledValue(
                    "tfg.machine.aux_exchanger.total_hu",
                    FormattingUtil.formatNumbers(totalHeat) + " HU",
                    ChatFormatting.AQUA,
                    "tfg.machine.aux_exchanger.total_hu.desc"));

            if (tempRecipe > 0) {
                double ratio = (double) tempBattery / tempRecipe;
                double durationMultiplier = 1 / Math.pow(2, ratio);
                int speedPercent = (int) Math.round((1 / durationMultiplier) * 100);

                textList.add(labeledValue(
                        "tfg.machine.aux_exchanger.speed_modifier",
                        speedPercent + "%",
                        speedPercent >= 100 ? ChatFormatting.GREEN : ChatFormatting.RED,
                        "tfg.machine.aux_exchanger.speed_modifier.desc"));
            }

        } catch (ClassCastException | NullPointerException e) {
            TFGCore.LOGGER.warn("Failed to display heat info", e);
            textList.add(Component.translatable(
                    "tfg.machine.aux_exchanger.heat_unavailable").withStyle(ChatFormatting.RED));
        }
    }

    private static Component labeledValue(
            String labelKey,
            String value,
            ChatFormatting valueColor,
            String hoverKey) {
        MutableComponent comp = Component.translatable(labelKey)
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(value).withStyle(valueColor));

        return comp.withStyle(style -> style.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.translatable(hoverKey).withStyle(ChatFormatting.GRAY))));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return super.createUI(entityPlayer);
    }

    @Override
    public boolean hasPlayerInventory() {
        return true;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 210, 180);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 202, 172)
                .setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5,
                        self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                        .setMaxWidthLimit(200)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream()
                .filter(Objects::nonNull)
                .map(IFancyUIProvider.class::cast)
                .toList();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel panel) {
        IVoidable.attachConfigurators(panel, this);
        super.attachConfigurators(panel);
    }

    @Override
    public void attachTooltips(TooltipsPanel panel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, panel);
        }
    }

    // Energy

    public EnergyContainerList getEnergyContainer() {
        List<IEnergyContainer> containers = new ArrayList<>();
        var handlers = getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (handlers.isEmpty()) {
            handlers = getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP);
        }
        for (IRecipeHandler<?> handler : handlers) {
            if (handler instanceof IEnergyContainer container) {
                containers.add(container);
            }
        }
        return new EnergyContainerList(containers);
    }

    @Override
    public long getMaxVoltage() {
        if (energyContainer == null) {
            energyContainer = getEnergyContainer();
        }

        long highestVoltage = energyContainer.getHighestInputVoltage();
        if (energyContainer.getNumHighestInputContainers() > 1) {
            int tier = GTUtil.getTierByVoltage(highestVoltage);
            return GTValues.V[Math.min(tier + 1, GTValues.MAX)];
        }
        return highestVoltage;
    }
}
*/
