package su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.dries007.tfc.common.fluids.SimpleFluid;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.serialization.network.IByteBufAdapter;
import brachy.modularui.value.sync.*;
import lombok.Getter;
import lombok.Setter;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TFGLargeBoilerMachine extends WorkableMultiblockMachine {

    public record BoosterFluid(
            Supplier<Fluid> fluid,
            int fluidAmountMb,
            int temperatureBonus,
            int minBoilerTemperature, // maxTemperature required define in the Multiblock
            String translationKey) {
    }

    // Every Boosters available - fluidAmountMb * 4 per second in order of !temperatureBonus!
    private static final List<BoosterFluid> BOOSTERS = List.of(
            new BoosterFluid(
                    () -> GTMaterials.Creosote.getFluid(1).getFluid(),
                    32, 300,
                    0,
                    "block.gtceu.creosote"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("tfg", "conifer_pitch")),
                    5, 300,
                    0,
                    "material.tfg.conifer_pitch"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("afc", "maple_sap")),
                    5, 300,
                    0,
                    "fluid.afc.maple_sap"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("afc", "birch_sap")),
                    5, 300,
                    0,
                    "fluid.afc.birch_sap"),
            new BoosterFluid(
                    () -> GTMaterials.WoodGas.getFluid(1).getFluid(),
                    52, 600,
                    0,
                    "material.gtceu.wood_gas"),
            new BoosterFluid(
                    () -> TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource(),
                    1, 600,
                    0,
                    "fluid.tfc.olive_oil"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("tfg", "raw_aromatic_mix")),
                    300, 1200,
                    1280,
                    "material.tfg.raw_aromatic_mix"),
            new BoosterFluid(
                    () -> GTMaterials.RocketFuel.getFluid(1).getFluid(),
                    200, 5000,
                    1280,
                    "material.gtceu.rocket_fuel"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("tfg", "radioactive_effluent")),
                    2, 16000,
                    1280,
                    "material.tfg.radioactive_effluent")

    /*
    // Example of a Booster that requires 1800 Temp (minBoilerTemperature = 1800) :
    new BoosterFluid(
    () -> GTMaterials.Creosote.getFluid(1).getFluid(),
    20, 1000,
    1800
    )
    */
    );

    // Boosters list for EMI
    public static List<BoosterFluid> getBoosters() {
        return BOOSTERS;
    }

    // Steam output boost depending of the water
    private static final Map<TagKey<Fluid>, Float> WATER_STEAM_MULTIPLIERS = new LinkedHashMap<>();
    static {
        WATER_STEAM_MULTIPLIERS.put(
                TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfg", "water_boiler_t2")),
                1.5f);
        // WATER_STEAM_MULTIPLIERS.put(TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfg", "water_boiler_t3")), 2.0f);
    }

    private static final TagKey<Fluid> WATER_BOILER = TagKey.create(
            Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath("tfg", "water_boiler"));
    public static final int TICKS_PER_STEAM_GENERATION = 5;

    @Getter
    public final int maxTemperature, heatSpeed;
    @SaveField
    @Getter
    @Setter
    private int currentTemperature, throttle;
    @Nullable
    protected TickableSubscription temperatureSubs;
    @Getter
    @Setter
    private int steamGenerated;

    // Track last water type used — for GUI display
    @SaveField
    @Nullable
    private String lastWaterTagKey = null; // null = standard water (water_boiler tag)
    @SaveField
    private float lastWaterMultiplier = 1.0f;
    @SaveField
    @SyncToClient
    private int lastWaterDrained = 0;

    @SaveField
    private boolean hasNoWater = false;

    // Precomputed once in constructor (depends only on final maxTemperature)
    private final List<BoosterFluid> compatibleBoosters;
    private final int bestBonusPossible;

    // Cache for getBestAvailableBooster()
    @Nullable
    private BoosterFluid cachedBooster = null;
    private long boosterCacheTimer = -1L;

    public TFGLargeBoilerMachine(BlockEntityCreationInfo info, int maxTemperature, int heatSpeed) {
        super(info, new TFGLargeBoilerRecipeLogic());
        this.maxTemperature = maxTemperature;
        this.heatSpeed = heatSpeed;
        this.throttle = 100;
        this.compatibleBoosters = BOOSTERS.stream()
                .filter(b -> maxTemperature >= b.minBoilerTemperature())
                .sorted(Comparator.comparingInt(BoosterFluid::temperatureBonus).reversed())
                .toList();
        this.bestBonusPossible = compatibleBoosters.isEmpty() ? 0 : compatibleBoosters.get(0).temperatureBonus();
    }

    @Override
    public TFGLargeBoilerRecipeLogic getRecipeLogic() {
        return (TFGLargeBoilerRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        boosterCacheTimer = -1L; // invalidate cache
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSteamSubscription));
        }
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        boosterCacheTimer = -1L; // invalidate cache
        cachedBooster = null;
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSteamSubscription));
        }
    }

    @Override
    public void onUnload() {
        if (temperatureSubs != null) {
            temperatureSubs.unsubscribe();
            temperatureSubs = null;
        }
        super.onUnload();
    }

    protected void updateSteamSubscription() {
        if (currentTemperature > 0) {
            temperatureSubs = subscribeServerTick(temperatureSubs, this::updateCurrentTemperature);
        } else if (temperatureSubs != null) {
            temperatureSubs.unsubscribe();
            temperatureSubs = null;
        }
    }

    private DrainResult tryDrainWater(int maxDrain) {
        List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
        inputTanks.addAll(getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP));
        inputTanks.addAll(getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));

        for (Map.Entry<TagKey<Fluid>, Float> entry : WATER_STEAM_MULTIPLIERS.entrySet()) {
            List<FluidIngredient> check = new ArrayList<>(List.of(FluidIngredient.of(entry.getKey(), maxDrain)));
            for (IRecipeHandler<?> tank : inputTanks) {
                check = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, check, true);
                if (check == null || check.isEmpty())
                    break;
            }
            if (check == null || check.isEmpty()) {
                // Drain only if possible
                List<FluidIngredient> drain = new ArrayList<>(List.of(FluidIngredient.of(entry.getKey(), maxDrain)));
                for (IRecipeHandler<?> tank : inputTanks) {
                    drain = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, drain, false);
                    if (drain == null || drain.isEmpty())
                        break;
                }
                return new DrainResult(maxDrain, entry.getValue(), entry.getKey().location().toString());
            }
        }

        // Fallback
        List<FluidIngredient> drainWater = new ArrayList<>(List.of(FluidIngredient.of(WATER_BOILER, maxDrain)));
        for (IRecipeHandler<?> tank : inputTanks) {
            drainWater = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, drainWater, false);
            if (drainWater == null || drainWater.isEmpty())
                return new DrainResult(maxDrain, 1.0f, null);
        }

        int drained = (drainWater == null || drainWater.isEmpty()) ? maxDrain : maxDrain - drainWater.get(0).getAmount();
        return new DrainResult(drained, 1.0f, null);
    }

    private record DrainResult(int drained, float multiplier, @Nullable String tagKey) {
    }

    protected void updateCurrentTemperature() {
        int effectiveMaxTemp = getEffectiveMaxTemperature(getBestAvailableBooster());

        if (recipeLogic.isWorking()) {
            if (getOffsetTimer() % 10 == 0) {
                if (currentTemperature < effectiveMaxTemp) {
                    currentTemperature = Mth.clamp(currentTemperature + heatSpeed * 10, 0, effectiveMaxTemp);
                } else if (currentTemperature > effectiveMaxTemp) {
                    currentTemperature = Math.max(effectiveMaxTemp, currentTemperature - getCoolDownRate() * 10);
                }
            }
        } else if (currentTemperature > 0) {
            currentTemperature -= getCoolDownRate();
        }

        if (isFormed() && getOffsetTimer() % TICKS_PER_STEAM_GENERATION == 0) {

            // Update the recipe duration + drain booster every 20 ticks
            if (getOffsetTimer() % 20 == 0) {
                tryDrainBoostFluid();
                getRecipeLogic().refreshDurationForTemperature();
            }

            // Amount of steam output
            double baseDrainExact = (double) currentTemperature * throttle * TICKS_PER_STEAM_GENERATION /
                    (ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater * 100.0);

            // Only for amount of water consummed
            final int WATER_THRESHOLD = 480; // At the point the cost in water stats increasing
            double tempFactor = 1.0;
            if (currentTemperature > WATER_THRESHOLD) {
                double t = (currentTemperature - WATER_THRESHOLD) / 100.0;
                tempFactor = 1.0 + 0.035 * Math.pow(t, 1.5); // The formula to show how much it increases
            }

            int maxDrain = (int) Math.round(baseDrainExact * tempFactor); // eau consommée augmentée

            if (currentTemperature < 100) {
                steamGenerated = 0;
                lastWaterTagKey = null;
                lastWaterMultiplier = 1.0f;
                lastWaterDrained = 0;
                hasNoWater = false;
            } else if (maxDrain > 0) {
                DrainResult drainResult = tryDrainWater(maxDrain);
                int drained = drainResult.drained();
                float waterMultiplier = drainResult.multiplier();

                // Update GUI water display state
                lastWaterTagKey = drainResult.tagKey();
                lastWaterMultiplier = waterMultiplier;
                lastWaterDrained = drained;

                // Steam on baseDrainExact and not maxDrain
                steamGenerated = (int) Math.round(
                        baseDrainExact * ((double) drained / Math.max(1, maxDrain))
                                * ConfigHolder.INSTANCE.machines.largeBoilers.steamPerWater
                                * waterMultiplier);

                if (drained > 0) {
                    var fillSteam = List.of(FluidIngredient.of(GTMaterials.Steam.getFluid(steamGenerated)));
                    List<IRecipeHandler<?>> outputTanks = new ArrayList<>();
                    outputTanks.addAll(getCapabilitiesFlat(IO.OUT, FluidRecipeCapability.CAP));
                    outputTanks.addAll(getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));
                    for (IRecipeHandler<?> tank : outputTanks) {
                        fillSteam = (List<FluidIngredient>) tank.handleRecipe(IO.OUT, null, fillSteam, false);
                        if (fillSteam == null)
                            break;
                    }
                }
                boolean hasDrainedWater = drained > 0;
                if (hasNoWater && hasDrainedWater) {
                    GTUtil.doExplosion(getLevel(), getBlockPos(), 2f);
                    var center = getBlockPos().below().relative(getFrontFacing().getOpposite());
                    if (GTValues.RNG.nextInt(100) > 80) {
                        GTUtil.doExplosion(getLevel(), center, 2f);
                    }
                    for (Direction x : Direction.Plane.HORIZONTAL) {
                        for (Direction y : Direction.Plane.HORIZONTAL) {
                            if (GTValues.RNG.nextInt(100) > 80) {
                                GTUtil.doExplosion(getLevel(), center.relative(x).relative(y), 2f);
                            }
                        }
                    }
                } else {
                    hasNoWater = !hasDrainedWater;
                }
            }
        }
        updateSteamSubscription();
    }

    @Nullable
    private BoosterFluid computeBestAvailableBooster() {
        List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
        inputTanks.addAll(getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP));
        inputTanks.addAll(getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));

        if (compatibleBoosters.isEmpty())
            return null;

        BoosterFluid bestBooster = null;

        // Iterate tanks once; for each tank check which booster it can satisfy
        for (IRecipeHandler<?> tank : inputTanks) {
            for (BoosterFluid booster : compatibleBoosters) {
                // Skip if this booster can't beat what we already found
                if (bestBooster != null && booster.temperatureBonus() <= bestBooster.temperatureBonus())
                    break;

                var check = List.of(FluidIngredient.of(booster.fluid().get(), booster.fluidAmountMb()));
                var result = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, check, true);
                if (result == null || result.isEmpty()) {
                    bestBooster = booster;
                    // already found the best possible booster
                    if (booster.temperatureBonus() == bestBonusPossible)
                        return bestBooster;
                    break;
                }
            }
        }
        return bestBooster;
    }

    // Returns the cached booster and recomputed every 20 ticks
    // Call by the others less problematic because less often
    @Nullable
    private BoosterFluid getBestAvailableBooster() {
        long now = getOffsetTimer();
        if (boosterCacheTimer < 0 || now - boosterCacheTimer >= 20) {
            cachedBooster = computeBestAvailableBooster();
            boosterCacheTimer = now;
        }
        return cachedBooster;
    }

    // Drain the best Booster available
    // If no Booster then reset the max temperature

    private int tryDrainBoostFluid() {
        BoosterFluid bestBooster = getBestAvailableBooster();
        if (bestBooster == null)
            return 0;

        List<IRecipeHandler<?>> inputTanks = new ArrayList<>();
        inputTanks.addAll(getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP));
        inputTanks.addAll(getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));

        var drainBoost = List.of(FluidIngredient.of(bestBooster.fluid().get(), bestBooster.fluidAmountMb()));
        for (IRecipeHandler<?> tank : inputTanks) {
            drainBoost = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, drainBoost, false);
            if (drainBoost == null || drainBoost.isEmpty())
                return bestBooster.temperatureBonus();
        }
        boosterCacheTimer = -1L;
        cachedBooster = null;
        return 0;
    }

    public int getEffectiveMaxTemperature(@Nullable BoosterFluid best) {
        return maxTemperature + (best != null ? best.temperatureBonus() : 0);
    }

    protected int getCoolDownRate() {
        return 1;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (currentTemperature < getEffectiveMaxTemperature(getBestAvailableBooster())) {
            currentTemperature = Math.max(1, currentTemperature);
            updateSteamSubscription();
        }
        return value;
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        return ModifierFunction.IDENTITY;
    }

    public void cycleRecipeType() {
        if (!this.getRecipeLogic().isWorking()) {
            int nextIndex = (this.getActiveRecipeType() + 1) % this.getRecipeTypes().length;
            this.setActiveRecipeType(nextIndex);
            this.getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);

        IntSyncValue throttleSync = new IntSyncValue(this::getThrottle, this::setThrottle).allowC2S();
        IntSyncValue currentTempSync = new IntSyncValue(this::getCurrentTemperature);
        IntSyncValue steamGeneratedSync = new IntSyncValue(this::getSteamGenerated);
        StringSyncValue waterInUseSync = new StringSyncValue(() -> lastWaterTagKey);
        FloatSyncValue lastWaterMultiplierSync = new FloatSyncValue(() -> lastWaterMultiplier);
        GenericSyncValue<BoosterFluid> boosterFluidSync = GenericSyncValue.builder(BoosterFluid.class)
                .getter(this::getBestAvailableBooster)
                .adapter(new IByteBufAdapter<>() {
                    @Override
                    public BoosterFluid deserialize(FriendlyByteBuf buffer) {
                        return BOOSTERS.get(buffer.readInt());
                    }

                    @Override
                    public void serialize(FriendlyByteBuf buffer, BoosterFluid u) {
                        buffer.writeInt(BOOSTERS.indexOf(u));
                    }

                    @Override
                    public boolean areEqual(TFGLargeBoilerMachine.BoosterFluid t1, TFGLargeBoilerMachine.BoosterFluid t2) {
                        return t1 == t2;
                    }
                })
                .build();

        syncManager.syncValue("throttle", throttleSync);
        syncManager.syncValue("currentTemp", currentTempSync);
        syncManager.syncValue("steamGenerated", steamGeneratedSync);
        syncManager.syncValue("waterInUse", waterInUseSync);
        syncManager.syncValue("lastWaterMultiplier", lastWaterMultiplierSync);
        syncManager.syncValue("boosterFluid", boosterFluidSync);

        if (!isFormed())
            return widgets;

        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_boiler.temperature",
                currentTemperature + 274, getEffectiveMaxTemperature(boosterFluidSync.getValue()) + 274)).asWidget()
                .tooltip(t -> t.add(Text.lang("gtceu.multiblock.large_boiler.throttle.tooltip"))));
        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_boiler.steam_output",
                steamGeneratedSync.getValue() / TICKS_PER_STEAM_GENERATION)).asWidget());

        if (boosterFluidSync.getValue() == null) {
            widgets.add(Text.lang("tfg.multiblock.large_boiler.booster_none")
                    .withStyle(ChatFormatting.GRAY).asWidget());
        } else {
            widgets.add(Text.lang("tfg.multiblock.large_boiler.booster_active",
                    Component.translatable(boosterFluidSync.getValue().translationKey()).withStyle(ChatFormatting.GREEN),
                    Component.literal("+" + boosterFluidSync.getValue().temperatureBonus()).withStyle(ChatFormatting.GREEN)).asWidget());
        }

        if (waterInUseSync.getStringValue() != null) {
            String tagPath = ResourceLocation.parse(waterInUseSync.getStringValue()).getPath();
            widgets.add(Text.lang("tfg.multiblock.large_boiler.water_boosted",
                    Component.translatable("fluid.tag.tfg." + tagPath).withStyle(ChatFormatting.AQUA),
                    Component.literal("x" + lastWaterMultiplierSync.getStringValue()).withStyle(ChatFormatting.AQUA)).asWidget());
        } else {
            widgets.add(Text.lang("tfg.multiblock.large_boiler.water_normal",
                    Component.translatable("tfg.multiblock.large_boiler.standard").withStyle(ChatFormatting.GRAY)).asWidget());
        }

        int efficiencyPercent = (int) Math.round(100 - ((1.0 - getTemperatureMultiplier(currentTempSync.getIntValue())) * 100));
        widgets.add(Text.lang("tfg.multiblock.large_boiler.fuel_efficiency",
                ChatFormatting.YELLOW.toString() + efficiencyPercent + "%").asWidget());

        widgets.add(Text.lang("gtceu.multiblock.large_boiler.throttle", ChatFormatting.AQUA.toString() + getThrottle() + "%").asWidget()
                .tooltip(t -> t.add(Text.lang("gtceu.multiblock.large_boiler.throttle.tooltip"))));

        widgets.add(LargeBoilerMachine.createIntInputWithButtons(throttleSync));

        return widgets;
    }

    private static double getTemperatureMultiplier(int currentTemperature) {
        final int THRESHOLD = 480;
        final double MAX_REDUCTION = 0.6; // x2.5 max (0.5 = ×2 max, 0.6 = ×2.5 max, 0.33 = ×1.5 max)

        if (currentTemperature <= THRESHOLD)
            return 1.0;

        double t = (currentTemperature - THRESHOLD) / 1000.0;
        // logarithm : start fast then slow down until it reaches its cap
        double reduction = MAX_REDUCTION * (1.0 - Math.exp(-0.8 * t)); // Math.exp(-x * t) with (x) 2 faster towards cap and 0.5 slower towards cap
        return 1.0 - reduction;
    }

    public static class TFGLargeBoilerRecipeLogic extends RecipeLogic {

        @SaveField
        @SyncToClient
        @Getter
        int currentThrottle;

        public TFGLargeBoilerRecipeLogic() {
            super();
            currentThrottle = 100;
        }

        @Override
        public TFGLargeBoilerMachine getMachine() {
            return (TFGLargeBoilerMachine) super.getMachine();
        }

        public void setCurrentThrottle(int currentThrottle) {
            this.currentThrottle = currentThrottle;
        }

        @Override
        public void setupRecipe(GTRecipe recipe) {
            super.setupRecipe(recipe);
            if (lastRecipe != null) {
                setCurrentThrottle(getMachine().getThrottle());
                double tempMultiplier = getTemperatureMultiplier(getMachine().getCurrentTemperature());
                duration = (int) Math.round(lastRecipe.duration / (currentThrottle / 100.0) * tempMultiplier);
            }
        }

        public void modifyFuelBurnTime(int newThrottle) {
            if (lastRecipe != null) {
                double newThrottleMultiplier = (double) currentThrottle / newThrottle;
                double tempMultiplier = getTemperatureMultiplier(getMachine().getCurrentTemperature());
                duration = (int) Math.round(lastRecipe.duration / (newThrottle / 100.0) * tempMultiplier);
                progress = (int) Math.round(newThrottleMultiplier * progress);
            }
            setCurrentThrottle(newThrottle);
        }

        // Modify the recipe duration depending of the booster and the throttle

        public void refreshDurationForTemperature() {
            if (lastRecipe != null) {
                double tempMultiplier = getTemperatureMultiplier(getMachine().getCurrentTemperature());
                int targetDuration = (int) Math.round(lastRecipe.duration / (currentThrottle / 100.0) * tempMultiplier);
                if (Math.abs(duration - targetDuration) > 2) {
                    double progressRatio = (double) progress / Math.max(1, duration);
                    duration = targetDuration;
                    progress = (int) Math.round(progressRatio * duration);
                }
            }
        }
    }
}
