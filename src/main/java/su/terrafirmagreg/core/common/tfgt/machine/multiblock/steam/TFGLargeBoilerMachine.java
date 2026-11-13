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
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.serialization.network.IByteBufAdapter;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.ParentWidget;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.api.pattern.TFGPredicates;
import su.terrafirmagreg.core.common.data.TFGTags;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TFGLargeBoilerMachine extends WorkableMultiblockMachine implements IMuiMachine {

    public record BoosterFluid(
            Supplier<Fluid> fluid,
            int fluidAmountMb,
            int temperatureBonus,
            int minBoosterTier, // tier of the booster module substructure required
            String translationKey) {
    }

    // Every Boosters available - fluidAmountMb * 4 per second in order of !temperatureBonus!
    private static final List<BoosterFluid> BOOSTERS = List.of(
            new BoosterFluid(
                    () -> GTMaterials.Creosote.getFluid(1).getFluid(),
                    32, 300,
                    1,
                    "block.gtceu.creosote"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("tfg", "conifer_pitch")),
                    5, 300,
                    1,
                    "material.tfg.conifer_pitch"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("afc", "maple_sap")),
                    5, 300,
                    1,
                    "fluid.afc.maple_sap"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("afc", "birch_sap")),
                    5, 300,
                    1,
                    "fluid.afc.birch_sap"),
            new BoosterFluid(
                    () -> GTMaterials.WoodGas.getFluid(1).getFluid(),
                    52, 600,
                    1,
                    "material.gtceu.wood_gas"),
            new BoosterFluid(
                    () -> TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource(),
                    1, 600,
                    1,
                    "fluid.tfc.olive_oil"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("tfg", "raw_aromatic_mix")),
                    300, 1200,
                    2,
                    "material.tfg.raw_aromatic_mix"),
            new BoosterFluid(
                    () -> GTMaterials.RocketFuel.getFluid(1).getFluid(),
                    200, 5000,
                    2,
                    "material.gtceu.rocket_fuel"),
            new BoosterFluid(
                    () -> BuiltInRegistries.FLUID.get(
                            ResourceLocation.fromNamespaceAndPath("tfg", "radioactive_effluent")),
                    2, 16000,
                    3,
                    "material.tfg.radioactive_effluent"));

    //#region Substructures

    // Booster Module is used to input the booster and boost the initial max PU
    public static final String BOOSTER = "booster";
    // Catalyst module is used to increase efficiency (lengthen recipe duration)
    public static final String CATALYST = "catalyst";

    // This is temporary the catalyst will become a list too
    private static final double[] CATALYST_DURATION_MULTIPLIERS = { 1.0, 1.25, 1.5, 2.0 };

    // Booster Module also gives a bonus to max PU to imitate the old Boilers
    private static final int[] BOOSTER_TEMPERATURE_BONUS = { 0, 320, 800, 1320 };

    //#endregion

    // Boosters list for EMI
    public static List<BoosterFluid> getBoosters() {
        return BOOSTERS;
    }

    // Steam output boost depending on the water
    private static final Map<TagKey<Fluid>, Float> WATER_STEAM_MULTIPLIERS = new LinkedHashMap<>();
    static {
        WATER_STEAM_MULTIPLIERS.put(
                TFGTags.Fluids.WATER_BOILER_T2,
                1.5f);
        // WATER_STEAM_MULTIPLIERS.put(TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfg", "water_boiler_t3")), 2.0f);
    }

    public static final int TICKS_PER_STEAM_GENERATION = 5;

    @Getter
    public final int baseMaxTemperature;
    @Getter
    public final int heatSpeed;
    @SaveField
    @Getter
    @Setter
    private int currentTemperature, throttle;
    @Nullable
    protected TickableSubscription temperatureSubs;
    @Getter
    @Setter
    private int steamGenerated;

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

    @Getter
    private final int maxModuleTier;

    @SyncToClient
    @Getter
    private int boosterTier = 0;
    @SyncToClient
    @Getter
    private int catalystTier = 0;

    // addHandlerList routes their handlers here instead of the recipe logic because they are only used for boosters
    private final List<IRecipeHandler<?>> boosterTanks = new ArrayList<>();

    // Fluid Hatch in the Booster Module needs to be routed to boosterTanks instead of
    // the controller capabilities so booster hatches stay dedicated to boosters
    private final Set<RecipeHandlerList> boosterHandlerLists = Collections.newSetFromMap(new IdentityHashMap<>());

    private List<BoosterFluid> compatibleBoosters = List.of();
    private int bestBonusPossible = 0;

    // Cache for getBestAvailableBooster()
    @Nullable
    private BoosterFluid cachedBooster = null;
    private long boosterCacheTimer = -1L;

    public TFGLargeBoilerMachine(BlockEntityCreationInfo info, int baseMaxTemperature, int heatSpeed,
            int maxModuleTier) {
        super(info, new TFGLargeBoilerRecipeLogic());
        this.baseMaxTemperature = baseMaxTemperature;
        this.heatSpeed = heatSpeed;
        this.maxModuleTier = maxModuleTier;
        this.throttle = 100;
    }

    public int getEffectiveBoosterTier() {
        return isFormed() ? Math.min(boosterTier, maxModuleTier) : 0;
    }

    public int getEffectiveCatalystTier() {
        return isFormed() ? Math.min(catalystTier, maxModuleTier) : 0;
    }

    // Max temperature (noted as PU in-game) without booster. The Booster Module also gives a bonus to temperature
    // May have an issue with efficiency but the catalyst does counter it
    public int getMaxTemperature() {
        return baseMaxTemperature + BOOSTER_TEMPERATURE_BONUS[getEffectiveBoosterTier()];
    }

    public double getCatalystDurationMultiplier() {
        return CATALYST_DURATION_MULTIPLIERS[getEffectiveCatalystTier()];
    }

    private void refreshCompatibleBoosters() {
        int tier = getEffectiveBoosterTier();
        compatibleBoosters = BOOSTERS.stream()
                .filter(b -> tier >= b.minBoosterTier())
                .sorted(Comparator.comparingInt(BoosterFluid::temperatureBonus).reversed())
                .toList();
        bestBonusPossible = compatibleBoosters.isEmpty() ? 0 : compatibleBoosters.get(0).temperatureBonus();
        boosterCacheTimer = -1L;
        cachedBooster = null;
    }

    @Override
    public TFGLargeBoilerRecipeLogic getRecipeLogic() {
        return (TFGLargeBoilerRecipeLogic) super.getRecipeLogic();
    }

    private static boolean isModule(String substructureName) {
        return BOOSTER.equals(substructureName) || CATALYST.equals(substructureName);
    }

    // addHandlerList only routes their handlers to boosterTanks so you can't input the boosters anywhere,
    // will do the same for catalyst
    @Override
    public void formStructure(@NotNull String substructureName) {
        refreshBoosterHandlerLists();
        boosterTanks.clear();
        super.formStructure(substructureName);
        if (isModule(substructureName)) {
            readModuleTier(substructureName);
        }
        refreshCompatibleBoosters();
        scheduleSteamSubscriptionUpdate();
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        boosterTanks.clear();
        if (isModule(substructureName)) {
            setModuleTier(substructureName, 0);
        }
        refreshCompatibleBoosters();
        scheduleSteamSubscriptionUpdate();
    }

    @Override
    public void addHandlerList(RecipeHandlerList handlerList) {
        if (boosterHandlerLists.contains(handlerList)) {
            if (handlerList.getHandlerIO().support(IO.IN)) {
                boosterTanks.addAll(handlerList.getCapability(FluidRecipeCapability.CAP));
            }
            return;
        }
        if (handlerList == RecipeHandlerList.NO_DATA)
            return;
        IO io = handlerList.getHandlerIO();
        getCapabilitiesProxy().computeIfAbsent(io, i -> new ArrayList<>()).add(handlerList);
        var entrySet = handlerList.getHandlerMap().entrySet();
        var inner = getCapabilitiesFlat().computeIfAbsent(io,
                i -> new Reference2ObjectOpenHashMap<>(entrySet.size()));
        for (var entry : entrySet) {
            var entryList = entry.getValue();
            inner.computeIfAbsent(entry.getKey(), c -> new ArrayList<>(entryList.size())).addAll(entryList);
        }
    }

    private void refreshBoosterHandlerLists() {
        boosterHandlerLists.clear();
        PatternState state = getPatternState(BOOSTER);
        if (!state.getState().isValid())
            return;
        for (BlockInfo info : state.getCache().values()) {
            if (info.getBlockEntity() instanceof MultiblockPartMachine part) {
                boosterHandlerLists.addAll(part.getRecipeHandlers());
            }
        }
    }

    // Doing the same thing as CoilWorkableElectricMultiblockMachine
    // You have to update all the blocks of the substructure like you would do for Coils in an EBF
    private void readModuleTier(String substructureName) {
        PatternState state = getPatternState(substructureName);
        if (!state.isFormed()) {
            setModuleTier(substructureName, 0);
            return;
        }

        int tier = -1;
        for (var entry : state.getCache().long2ObjectEntrySet()) {
            int blockTier = TFGPredicates.getBoilerModuleTier(entry.getValue().getBlockState().getBlock());
            if (blockTier < 0)
                continue; // hatches
            if (tier < 0) {
                tier = blockTier;
            } else if (tier != blockTier) {
                state.setError(new PatternStringError(
                        Component.translatable("tfg.multiblock.large_boiler.module_tier_mismatch", tier, blockTier),
                        BlockPos.of(entry.getLongKey())));
                invalidateStructure(substructureName);
                return;
            }
        }
        setModuleTier(substructureName, Math.max(tier, 0));
    }

    private void setModuleTier(String substructureName, int tier) {
        if (BOOSTER.equals(substructureName)) {
            boosterTier = tier;
            getSyncDataHolder().markClientSyncFieldDirty("boosterTier");
        } else if (CATALYST.equals(substructureName)) {
            catalystTier = tier;
            getSyncDataHolder().markClientSyncFieldDirty("catalystTier");
        }
    }

    private void scheduleSteamSubscriptionUpdate() {
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
        List<FluidIngredient> drainWater = new ArrayList<>(List.of(FluidIngredient.of(TFGTags.Fluids.WATER_BOILER, maxDrain)));
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
        if (compatibleBoosters.isEmpty() || boosterTanks.isEmpty())
            return null;

        BoosterFluid bestBooster = null;

        // Check every fluid hatch of the Booster module
        for (IRecipeHandler<?> tank : boosterTanks) {
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

        var drainBoost = List.of(FluidIngredient.of(bestBooster.fluid().get(), bestBooster.fluidAmountMb()));
        for (IRecipeHandler<?> tank : boosterTanks) {
            drainBoost = (List<FluidIngredient>) tank.handleRecipe(IO.IN, null, drainBoost, false);
            if (drainBoost == null || drainBoost.isEmpty())
                return bestBooster.temperatureBonus();
        }
        boosterCacheTimer = -1L;
        cachedBooster = null;
        return 0;
    }

    public int getEffectiveMaxTemperature(@Nullable BoosterFluid best) {
        return getMaxTemperature() + (best != null ? best.temperatureBonus() : 0);
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

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager, 240, 180)
                .margin(4, 2));
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
                    public @Nullable BoosterFluid deserialize(FriendlyByteBuf buffer) {
                        int index = buffer.readInt();
                        return index < 0 ? null : BOOSTERS.get(index);
                    }

                    @Override
                    public void serialize(FriendlyByteBuf buffer, @Nullable BoosterFluid u) {
                        buffer.writeInt(u == null ? -1 : BOOSTERS.indexOf(u));
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

        IntSyncValue boosterTierSync = new IntSyncValue(this::getEffectiveBoosterTier);
        IntSyncValue catalystTierSync = new IntSyncValue(this::getEffectiveCatalystTier);
        IntSyncValue maxTemperatureSync = new IntSyncValue(this::getMaxTemperature);
        IntSyncValue waterDrainedSync = new IntSyncValue(() -> lastWaterDrained);
        syncManager.syncValue("boosterTier", boosterTierSync);
        syncManager.syncValue("catalystTier", catalystTierSync);
        syncManager.syncValue("maxTemperature", maxTemperatureSync);
        syncManager.syncValue("waterDrained", waterDrainedSync);

        if (!isFormed())
            return widgets;

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.multiblock.large_boiler.temperature",
                currentTempSync.getIntValue(),
                maxTemperatureSync.getIntValue() +
                        (boosterFluidSync.getValue() != null ? boosterFluidSync.getValue().temperatureBonus() : 0)))
                .asWidget()
                .tooltip(t -> t.add(Text.lang("tfg.multiblock.large_boiler.temperature.tooltip"))));
        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_boiler.steam_output",
                steamGeneratedSync.getValue() / TICKS_PER_STEAM_GENERATION)).asWidget());

        widgets.add(Text.dynamic(() -> {
            if (boosterFluidSync.getValue() == null) {
                return Text.lang("tfg.multiblock.large_boiler.booster_none")
                        .withStyle(ChatFormatting.GRAY);
            } else {
                return Text.lang("tfg.multiblock.large_boiler.booster_active",
                        Component.translatable(boosterFluidSync.getValue().translationKey()).withStyle(ChatFormatting.GREEN),
                        Component.literal("+" + boosterFluidSync.getValue().temperatureBonus()).withStyle(ChatFormatting.GREEN));
            }
        }).asWidget());

        widgets.add(Text.dynamic(() -> {
            int tier = boosterTierSync.getIntValue();
            if (tier <= 0) {
                return Text.lang("tfg.multiblock.large_boiler.booster_module_missing")
                        .withStyle(ChatFormatting.GRAY);
            }
            return Text.lang("tfg.multiblock.large_boiler.booster_module",
                    Component.literal("T" + tier).withStyle(ChatFormatting.GOLD),
                    Component.literal(maxTemperatureSync.getIntValue() + " PU").withStyle(ChatFormatting.RED));
        }).asWidget());

        widgets.add(Text.dynamic(() -> {
            int tier = catalystTierSync.getIntValue();
            if (tier <= 0) {
                return Text.lang("tfg.multiblock.large_boiler.catalyst_module_missing")
                        .withStyle(ChatFormatting.GRAY);
            }
            return Text.lang("tfg.multiblock.large_boiler.catalyst_module",
                    Component.literal("T" + tier).withStyle(ChatFormatting.GOLD),
                    Component.literal("x" + CATALYST_DURATION_MULTIPLIERS[tier]).withStyle(ChatFormatting.GREEN));
        }).asWidget());

        widgets.add(Text.dynamic(() -> {
            if (waterInUseSync.getStringValue() != null) {
                String tagPath = ResourceLocation.parse(waterInUseSync.getStringValue()).getPath();
                return Text.lang("tfg.multiblock.large_boiler.water_boosted",
                        Component.translatable("fluid.tag.tfg." + tagPath).withStyle(ChatFormatting.AQUA),
                        Component.literal("x" + lastWaterMultiplierSync.getStringValue()).withStyle(ChatFormatting.AQUA));
            } else {
                return Text.lang("tfg.multiblock.large_boiler.water_normal",
                        Component.translatable("tfg.multiblock.large_boiler.standard").withStyle(ChatFormatting.GRAY));
            }
        }).asWidget());

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.multiblock.large_boiler.water_consumption",
                Component.literal(String.valueOf(waterDrainedSync.getIntValue() / TICKS_PER_STEAM_GENERATION))
                        .withStyle(ChatFormatting.AQUA)))
                .asWidget()
                .tooltip(t -> t.add(Text.lang("tfg.multiblock.large_boiler.water_consumption.tooltip"))));

        widgets.add(Text.dynamic(() -> {
            int efficiencyPercent = (int) Math.round(getTemperatureMultiplier(currentTempSync.getIntValue())
                    * CATALYST_DURATION_MULTIPLIERS[catalystTierSync.getIntValue()] * 100);
            return Text.lang("tfg.multiblock.large_boiler.fuel_efficiency",
                    ChatFormatting.YELLOW.toString() + efficiencyPercent + "%");
        }).asWidget());

        widgets.add(Text.dynamic(() -> Text.lang("gtceu.multiblock.large_boiler.throttle",
                ChatFormatting.AQUA.toString() + throttleSync.getIntValue() + "%")).asWidget()
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

        private int computeDuration(GTRecipe recipe, int throttle) {
            double tempMultiplier = getTemperatureMultiplier(getMachine().getCurrentTemperature());
            return (int) Math.round(recipe.duration / (throttle / 100.0) * tempMultiplier
                    * getMachine().getCatalystDurationMultiplier());
        }

        @Override
        public void setupRecipe(GTRecipe recipe) {
            super.setupRecipe(recipe);
            if (lastRecipe != null) {
                setCurrentThrottle(getMachine().getThrottle());
                duration = computeDuration(lastRecipe, currentThrottle);
            }
        }

        public void modifyFuelBurnTime(int newThrottle) {
            if (lastRecipe != null) {
                double newThrottleMultiplier = (double) currentThrottle / newThrottle;
                duration = computeDuration(lastRecipe, newThrottle);
                progress = (int) Math.round(newThrottleMultiplier * progress);
            }
            setCurrentThrottle(newThrottle);
        }

        // Modify the recipe duration depending of the temperature, the throttle and the catalyst

        public void refreshDurationForTemperature() {
            if (lastRecipe != null) {
                int targetDuration = computeDuration(lastRecipe, currentThrottle);
                if (Math.abs(duration - targetDuration) > 2) {
                    double progressRatio = (double) progress / Math.max(1, duration);
                    duration = targetDuration;
                    progress = (int) Math.round(progressRatio * duration);
                }
            }
        }
    }
}
