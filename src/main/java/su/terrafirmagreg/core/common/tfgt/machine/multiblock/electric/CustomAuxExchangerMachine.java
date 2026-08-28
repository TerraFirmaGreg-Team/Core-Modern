package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

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
