package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import lombok.Getter;
import su.terrafirmagreg.core.common.atmosphere.AtmosphereSystem;
import su.terrafirmagreg.core.common.atmosphere.DimensionAtmosphereManager;
import su.terrafirmagreg.core.common.atmosphere.IBubbleProvider;

/**
 * Temperature Regulator Machine - provides normal temperature in a spherical bubble.
 * <p>
 * Useful for extreme environments like Venus (hot) or Glacio (cold).
 * <p>
 * Tiered:
 * - LV: 8 block radius
 * - MV: 16 block radius
 * - HV: 32 block radius
 * - EV+: 64 block radius
 */
public class TemperatureRegulatorMachine extends TieredEnergyMachine
        implements IControllable, IFancyUIMachine, IMachineLife,
        IBubbleProvider, DimensionAtmosphereManager.ITemperatureProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TemperatureRegulatorMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /**
     * Bubble radius based on tier.
     */
    public static double bubbleRadius(int tier) {
        return switch (tier) {
            case GTValues.LV -> 8.0;
            case GTValues.MV -> 16.0;
            case GTValues.HV -> 32.0;
            default -> 64.0; // EV and above
        };
    }

    /**
     * Energy consumption per tick based on radius.
     */
    public static long energyPerTick(int tier) {
        double radius = bubbleRadius(tier);
        // Base: proportional to volume, slightly higher than gravity (temperature is harder)
        long base = (long) (4.0 / 3.0 * Math.PI * radius * radius * radius / 800);
        return Math.max(1, Math.min(base, GTValues.V[tier]));
    }

    @Persisted
    @Getter
    private boolean workingEnabled = true;

    @Persisted
    private boolean isCurrentlyActive = false;

    protected ISubscription energySubscription;
    protected TickableSubscription tickSubscription;

    public TemperatureRegulatorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
    }

    @Override
    protected @NotNull NotifiableEnergyContainer createEnergyContainer(Object @NotNull... args) {
        long capacity = GTValues.V[tier] * 64;
        return new NotifiableEnergyContainer(this, capacity, GTValues.V[tier], 2L, 0L, 0L);
    }

    // ==================== IBubbleProvider ====================

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    @Override
    public Level getLevel() {
        return super.getLevel();
    }

    @Override
    public boolean isActive() {
        return isCurrentlyActive && workingEnabled && !isRemote();
    }

    @Override
    public double getRadius() {
        return bubbleRadius(tier);
    }

    // ==================== Machine Lifecycle ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote())
            return;

        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSubscription));
        }

        energySubscription = energyContainer.addChangedListener(this::updateSubscription);

        // Register with atmosphere system
        AtmosphereSystem.registerBubbleProvider(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        // Unregister from atmosphere system
        AtmosphereSystem.unregisterBubbleProvider(this);

        if (energySubscription != null) {
            energySubscription.unsubscribe();
            energySubscription = null;
        }
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        AtmosphereSystem.unregisterBubbleProvider(this);
    }

    // ==================== Working Logic ====================

    public void updateSubscription() {
        if (isRemote())
            return;

        boolean canWork = workingEnabled && hasEnoughEnergy();
        boolean wasActive = isCurrentlyActive;
        isCurrentlyActive = canWork;

        if (wasActive != isCurrentlyActive) {
            markDirty();
        }

        if (canWork) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (tickSubscription != null) {
                tickSubscription.unsubscribe();
                tickSubscription = null;
            }
        }
    }

    public void tick() {
        if (isRemote())
            return;

        if (workingEnabled && isCurrentlyActive) {
            long energy = energyPerTick(tier);
            if (energyContainer.getEnergyStored() >= energy) {
                energyContainer.removeEnergy(energy);
            }
        }

        updateSubscription();
    }

    private boolean hasEnoughEnergy() {
        return energyContainer.getEnergyStored() >= energyPerTick(tier);
    }

    @Override
    public void setWorkingEnabled(boolean enabled) {
        if (this.workingEnabled == enabled)
            return;
        this.workingEnabled = enabled;
        markDirty();
        if (!isRemote()) {
            updateSubscription();
        }
    }

    // ==================== Tooltips ====================

    public static List<Component> getTooltips(int tier) {
        return List.of(
                Component.translatable("tfg.machine.temperature_regulator.tooltip.1"),
                Component.translatable("tfg.machine.temperature_regulator.tooltip.radius",
                        String.format("%.0f", bubbleRadius(tier))),
                Component.translatable("tfg.machine.temperature_regulator.tooltip.energy",
                        GTValues.VNF[tier]));
    }
}
