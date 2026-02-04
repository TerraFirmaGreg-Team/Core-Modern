package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import lombok.Getter;
import su.terrafirmagreg.core.common.atmosphere.AtmosphereRoom;
import su.terrafirmagreg.core.common.atmosphere.AtmosphereSystem;
import su.terrafirmagreg.core.common.atmosphere.IAtmosphereProvider;

/**
 * Oxygen Distributor Machine - provides oxygen via flood fill in sealed rooms.
 * <p>
 * Tiered:
 * - LV: 10,000 blocks max
 * - MV: 100,000 blocks max
 * - HV: 1,000,000 blocks max
 */
public class OxygenDistributorMachine extends TieredEnergyMachine
        implements IControllable, IFancyUIMachine, IMachineLife, IAtmosphereProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OxygenDistributorMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /**
     * Maximum room size based on tier.
     */
    public static int maxRoomSize(int tier) {
        return switch (tier) {
            case GTValues.LV -> 10_000;
            case GTValues.MV -> 100_000;
            default -> 1_000_000; // HV and above
        };
    }

    /**
     * Energy consumption per tick based on room size.
     * Base: 1 EU/t per 100 blocks
     */
    public static long energyPerTick(int roomSize, int tier) {
        long base = Math.max(1, roomSize / 100);
        return Math.min(base, GTValues.V[tier]); // Cap at tier voltage
    }

    @Persisted
    @Getter
    private boolean workingEnabled = true;

    @Persisted
    private boolean isCurrentlyProviding = false;

    @Getter
    private int currentRoomSize = 0;

    @Getter
    private AtmosphereRoom.Mode currentMode = AtmosphereRoom.Mode.INACTIVE;

    @Getter
    private final NotifiableFluidTank oxygenTank;
    protected ISubscription energySubscription;
    protected ISubscription fluidSubscription;
    protected TickableSubscription tickSubscription;

    public OxygenDistributorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);

        // Oxygen tank - consumes oxygen fluid
        this.oxygenTank = new NotifiableFluidTank(this, 1, 16 * FluidHelper.getBucket(), IO.IN);
    }

    @Override
    protected @NotNull NotifiableEnergyContainer createEnergyContainer(Object @NotNull... args) {
        long capacity = GTValues.V[tier] * 64;
        return new NotifiableEnergyContainer(this, capacity, GTValues.V[tier], 2L, 0L, 0L);
    }

    // ==================== IAtmosphereProvider ====================

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
        return workingEnabled && !isRemote() && hasEnoughEnergy() && hasEnoughOxygen();
    }

    @Override
    public int getMaxRoomSize() {
        return maxRoomSize(tier);
    }

    @Override
    public int getMaxHorizontalDimension() {
        return 128;
    }

    @Override
    public void onRoomStateChanged(AtmosphereRoom room) {
        if (room == null) {
            currentRoomSize = 0;
            currentMode = AtmosphereRoom.Mode.INACTIVE;
            isCurrentlyProviding = false;
        } else {
            currentRoomSize = room.getScan().interiorSize();
            currentMode = room.getMode();
            isCurrentlyProviding = room.getMode() == AtmosphereRoom.Mode.SEALED ||
                    room.getMode() == AtmosphereRoom.Mode.BUBBLE;
        }
        markDirty();
    }

    @Override
    public void onRoomBreached(AtmosphereRoom room) {
        // Could spawn vortex effect here
        // For now, just log
        if (getLevel() instanceof ServerLevel) {
            // Vortex spawning would go here
        }
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
        fluidSubscription = oxygenTank.addChangedListener(this::updateSubscription);

        // Register with atmosphere system
        AtmosphereSystem.registerProvider(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        // Unregister from atmosphere system
        AtmosphereSystem.unregisterProvider(this);

        if (energySubscription != null) {
            energySubscription.unsubscribe();
            energySubscription = null;
        }
        if (fluidSubscription != null) {
            fluidSubscription.unsubscribe();
            fluidSubscription = null;
        }
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        AtmosphereSystem.unregisterProvider(this);
    }

    // ==================== Working Logic ====================

    public void updateSubscription() {
        if (isRemote())
            return;

        boolean canWork = workingEnabled && hasEnoughEnergy();

        if (canWork && isCurrentlyProviding) {
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

        if (workingEnabled && isCurrentlyProviding) {
            // Consume energy based on room size
            long energy = energyPerTick(currentRoomSize, tier);
            if (energyContainer.getEnergyStored() >= energy) {
                energyContainer.removeEnergy(energy);

                // Consume oxygen fluid (1mB per 1000 blocks per tick)
                long oxygenAmount = Math.max(1, currentRoomSize / 1000);
                oxygenTank.drain(oxygenAmount, false);
            } else {
                // Not enough energy - will trigger room revalidation
                // TODO: Not room revalidation, instead it triggers shutdown of the machine. Room still exists it's just not oxygenated.
            }
        }

        updateSubscription();
    }

    private boolean hasEnoughEnergy() {
        return energyContainer.getEnergyStored() >= energyPerTick(Math.max(100, currentRoomSize), tier);
    }

    private boolean hasEnoughOxygen() {
        // Need some oxygen in the tank to operate
        return oxygenTank.getFluidAmount() > 0;
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
                Component.translatable("tfg.machine.oxygen_distributor.tooltip.1"),
                Component.translatable("tfg.machine.oxygen_distributor.tooltip.max_size",
                        String.format("%,d", maxRoomSize(tier))),
                Component.translatable("tfg.machine.oxygen_distributor.tooltip.energy",
                        GTValues.VNF[tier]));
    }
}
