package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.*;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import fi.dea.mc.deafission.common.data.machine.AuxExchangerMachine;
import fi.dea.mc.deafission.common.data.machine.hb.HeatHatchMachine;
import fi.dea.mc.deafission.common.data.recipe.HeatIngredient;
import fi.dea.mc.deafission.common.data.recipe.HeatRecipeCapability;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;

@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomAuxExchangerMachine extends AuxExchangerMachine
        implements IDisplayUIMachine, ITieredMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CustomAuxExchangerMachine.class, AuxExchangerMachine.MANAGED_FIELD_HOLDER);

    protected EnergyContainerList energyContainer;

    @Nullable
    protected HeatHatchMachine cachedHeatHatch;

    @Getter
    protected int tier;

    public CustomAuxExchangerMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // *** Multiblock Lifecycle ***//
    //////////////////////////////////////

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.energyContainer = getEnergyContainer();
        this.tier = GTUtil.getFloorTierByVoltage(getMaxVoltage());

        this.cachedHeatHatch = getParts().stream()
                .filter(p -> p instanceof HeatHatchMachine)
                .map(p -> (HeatHatchMachine) p)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
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

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

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

        textList.add(Component.translatable("tfg.machine.aux_echanger.output.title"));

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
                    FormattingUtil.formatNumbers(heatRequired) + " HU/cycle",
                    ChatFormatting.RED,
                    "tfg.machine.aux_exchanger.hu_consumed.desc"));

            textList.add(labeledValue(
                    "tfg.machine.aux_exchanger.total_hu",
                    FormattingUtil.formatNumbers(totalHeat) + " HU",
                    ChatFormatting.AQUA,
                    "tfg.machine.aux_exchanger.total_hu.desc"));

            if (tempRecipe > 0) {
                double ratio = Math.min((double) tempBattery / tempRecipe, 3.0);
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

    //////////////////////////////////////
    // ********** UI ***********//

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return super.createUI(entityPlayer);
    }

    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 170);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 162)
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

    //////////////////////////////////////
    // ****** ENERGY ********//
    //////////////////////////////////////

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
