package su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.InterplanetaryLogisticsNetwork.*;
import su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.NetworkSenderConfigEntry;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunAmmoLoaderMachine;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;
import su.terrafirmagreg.core.network.TFGNetworkHandler;

public class InterplanetaryItemLauncherMachine extends WorkableElectricMultiblockMachine
        implements ILogisticsNetworkSender, IMachineLife, IFancyUIMachine, IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            InterplanetaryItemLauncherMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    protected TickableSubscription tickSubscription;

    private @Nullable EnergyContainerList energyInputs;

    private final List<RailgunItemBusMachine> itemInputs = new ArrayList<>();
    private final long[] lastActiveTime = new long[33];

    private RailgunAmmoLoaderMachine ammoLoaderPart;

    public InterplanetaryItemLauncherMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public InterplanetaryItemLauncherMachine getMachine() {
        return this;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        updateSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel sLvl) {
            if (!InterplanetaryLogisticsNetwork.DIMENSION_DISTANCES.containsKey(getDimensionalPos().dimension()))
                return;
            sLvl.getServer().tell(new TickTask(0, () -> getLogisticsNetwork().loadOrCreatePart(this)));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote())
            getLogisticsNetwork().unloadPart(this);
    }

    @Override
    public void onMachineRemoved() {
        if (!isRemote())
            getLogisticsNetwork().destroyPart(this);
    }

    @Override
    public boolean isMachineInvalid() {
        return !isFormed() || isInValid();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // Collect multiblock parts
        List<IEnergyContainer> energyHatches = new ArrayList<>();
        itemInputs.clear();
        for (var part : getParts()) {

            if (part instanceof EnergyHatchPartMachine energyHatch) {
                energyHatches.add(energyHatch.energyContainer);
            }

            if (part instanceof RailgunAmmoLoaderMachine ammo) {
                ammoLoaderPart = ammo;
            }

            if (part instanceof RailgunItemBusMachine bus) {
                itemInputs.add(bus);
                bus.getInventory()
                        .addChangedListener(() -> lastActiveTime[IntCircuitBehaviour
                                .getCircuitConfiguration(bus.getCircuitInventory().getStackInSlot(0))] = Objects
                                        .requireNonNull(getLevel()).getGameTime());
            }
        }

        energyInputs = new EnergyContainerList(energyHatches);
        updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        energyInputs = null;
        ammoLoaderPart = null;
        itemInputs.clear();
    }

    @Override
    public List<RailgunItemBusMachine> getInventories() {
        if (isMachineInvalid())
            return List.of();
        List<RailgunItemBusMachine> parts = new ArrayList<>();
        for (var part : getParts()) {
            if (part instanceof RailgunItemBusMachine r)
                parts.add(r);
        }
        return parts;
    }

    private void updateSubscription() {
        if (isRemote())
            return;
        if (isFormed() && isWorkingEnabled()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (tickSubscription != null)
                tickSubscription.unsubscribe();
        }
    }

    private void tick() {
        if (Objects.requireNonNull(getLevel()).getGameTime() % 20 != 0) {
            updateSubscription();
            return;
        }
        for (var config : getSendConfigurations()) {
            if (ammoLoaderPart.getInventory().isEmpty())
                break;
            var withCircuit = itemInputs.stream()
                    .filter((c) -> IntCircuitBehaviour.getCircuitConfiguration(
                            c.getCircuitInventory().getStackInSlot(0)) == config.getSenderDistinctInventory()
                            && c.isWorkingEnabled() && !c.getInventory().isEmpty())
                    .toList();
            if (withCircuit.isEmpty() || config.getReceiverPartID() == null)
                continue;
            var result = tryLaunchItemPayload(config);
            if (result)
                break;
        }
        updateSubscription();
    }

    private boolean tryLaunchItemPayload(NetworkSenderConfigEntry config) {
        if (energyInputs == null || !isFormed || !isWorkingEnabled()) return false;

        var destination = getLogisticsNetwork().getNetworkMachine(config.getReceiverPartID());
        if (!(destination instanceof ILogisticsNetworkReceiver receiver))
            return false;

        List<ItemStack> itemsToExtract = new ArrayList<>();
        var itemBuses = itemInputs.stream()
                .filter((c) -> IntCircuitBehaviour.getCircuitConfiguration(
                        c.getCircuitInventory().getStackInSlot(0)) == config.getSenderDistinctInventory()
                        && c.isWorkingEnabled() && !c.getInventory().isEmpty())
                .toList();
        if (itemBuses.isEmpty())
            return false;
        if (config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.ITEM) {
            for (int i = 0; i < config.getCurrentSendFilter().getSlots(); i++) {
                var stack = config.getCurrentSendFilter().getStackInSlot(i);
                if (!stack.isEmpty()) { // empty slots are "0 air"
                    itemsToExtract.add(config.getCurrentSendFilter().getStackInSlot(i));
                    if (!receiver.canAcceptItems(config.getReceiverDistinctInventory(), itemsToExtract)) {
                        itemsToExtract.remove(stack);
                    }
                }
            }
        } else if (config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.REDSTONE_SIGNAL) {
            boolean hasAnySignal = false;
            for (var bus : itemBuses) {
                if (Objects.requireNonNull(getLevel()).hasNeighborSignal(bus.getPos())) {
                    hasAnySignal = true;
                    break;
                }
            }
            if (!hasAnySignal)
                return false;
        } else {
            var inactivityTime = lastActiveTime[config.getSenderDistinctInventory()];
            var currentTick = Objects.requireNonNull(getLevel()).getGameTime();
            if (inactivityTime + (20L * config.getCurrentInactivityTimeout()) >= currentTick)
                return false;
        }

        if (itemsToExtract.isEmpty()) {
            int matched = 0;
            for (var bus : itemBuses) {
                if (bus.getInventory().isEmpty())
                    continue;
                for (int i = 0; i < bus.getInventory().getSlots(); i++) {
                    var stack = bus.getInventory().getStackInSlot(i);
                    if (stack.isEmpty())
                        continue;
                    itemsToExtract.add(stack);
                    if (tryExtractFromCircuitInventory(itemsToExtract, config.getSenderDistinctInventory(), true)
                            && receiver.canAcceptItems(config.getReceiverDistinctInventory(), itemsToExtract)) {
                        matched++;
                    } else {
                        itemsToExtract.remove(stack);
                    }
                    matched++;
                    if (matched == 3)
                        break;
                }
                if (matched == 3)
                    break;
            }
        }

        var sendPos = InterplanetaryLogisticsNetwork.DIMENSION_DISTANCES.get(getDimensionalPos().dimension());
        var receiverPos = InterplanetaryLogisticsNetwork.DIMENSION_DISTANCES
                .get(receiver.getDimensionalPos().dimension());
        var travelTime = Math.abs(sendPos - receiverPos);

        if (!tryExtractFromCircuitInventory(itemsToExtract, config.getSenderDistinctInventory(), true)
                || itemsToExtract.isEmpty() || itemsToExtract.stream().allMatch(ItemStack::isEmpty))
            return false;
        ammoLoaderPart.getInventory().extractItemInternal(0, 1, false);
        energyInputs.removeEnergy(16 * GTValues.V[GTValues.HV]);
        var extracted = tryExtractFromCircuitInventory(itemsToExtract, config.getSenderDistinctInventory(), false);
        if (extracted)
            receiver.onPackageSent(config.getReceiverDistinctInventory(), itemsToExtract, 20 * travelTime);
        launchEffects();
        return true;
    }

    @Override
    public Component getCurrentStatusText() {
        if (!isFormed())
            return Component.literal("§cMultiblock not formed");
        return null;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addWorkingStatusLine();

        if (energyInputs != null) {
            textList.add(Component.literal("Power stored: %s".formatted(FormattingUtil.formatNumbers(energyInputs.getEnergyStored()))));
            textList.add(Component.literal("Power capacity: %s".formatted(FormattingUtil.formatNumbers(energyInputs.getEnergyCapacity()))));
        }

        for (var part : this.getParts()) {
            part.addMultiText(textList);
        }
    }

    private boolean tryExtractFromCircuitInventory(List<ItemStack> toExtract, int circuit, boolean simulated) {
        List<ItemStack> remainingItems = new ArrayList<>();
        for (var v : toExtract) {
            remainingItems.add(v.copy());
        }
        var itemBuses = itemInputs.stream().filter(
                (c) -> IntCircuitBehaviour.getCircuitConfiguration(c.getCircuitInventory().getStackInSlot(0)) == circuit
                        && c.isWorkingEnabled())
                .toList();
        for (RailgunItemBusMachine bus : itemBuses) {
            tryExtractFromInventory(remainingItems, bus.getInventory().storage, simulated);
            if (remainingItems.isEmpty())
                return true;
        }
        return false;
    }

    private static void tryExtractFromInventory(List<ItemStack> remainingItems, CustomItemStackHandler inventory,
            boolean simulated) {
        for (var iter = remainingItems.listIterator(); iter.hasNext();) {
            var stack = iter.next();
            for (int slotIndex = 0; slotIndex < inventory.getSlots(); slotIndex++) {
                if (ItemStack.isSameItem(inventory.getStackInSlot(slotIndex), stack)) {
                    var extracted = inventory.extractItem(slotIndex, stack.getCount(), simulated);
                    stack.setCount(stack.getCount() - extracted.getCount());
                }
                if (stack.getCount() == 0) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    private void launchEffects() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            BlockPos basePos = this.getPos();
            BlockState state = this.getBlockState();
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);

            double x = basePos.getX() + 0.5;
            double y = basePos.getY() + 1.0;
            double z = basePos.getZ() + 0.5;

            switch (facing) {
                case NORTH -> z += 2;
                case SOUTH -> z -= 2;
                case WEST -> x += 2;
                case EAST -> x -= 2;
            }

            assert TFGParticles.RAILGUN_AMMO.getKey() != null;
            TFGNetworkHandler.sendParticle(
                    serverLevel,
                    x, y, z, // position
                    new Vec3(0, 1.0, 0), // vector velocity
                    TFGParticles.RAILGUN_AMMO.getKey().location(), // particle
                    1, // spawn count
                    0.0, 0.0, 0.0 // x,y,z spread
            );

            assert TFGParticles.RAILGUN_BOOM.getKey() != null;
            TFGNetworkHandler.sendParticle(
                    serverLevel,
                    x, y + 13, z,
                    Vec3.ZERO,
                    TFGParticles.RAILGUN_BOOM.getKey().location(),
                    1,
                    0.0, 0.0, 0.0);

            TFGNetworkHandler.sendParticle(
                    serverLevel,
                    x, y + 3, z,
                    new Vec3(1.0, 1.0, 1.0),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "explosion"),
                    10,
                    1.0, 1.0, 1.0);

            TFGNetworkHandler.sendParticle(
                    serverLevel,
                    x, y + 23, z,
                    new Vec3(0, 0.0, 0),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "wax_off"),
                    100,
                    0.0, 10.0, 0.0);

            ResourceLocation boomSound = ResourceLocation.fromNamespaceAndPath("minecraft", "entity.warden.sonic_boom");
            TFGNetworkHandler.sendSound(
                    serverLevel,
                    x, y + 13, z, // position
                    boomSound,
                    10.0f, // volume
                    0.1f // pitch
            );

            ResourceLocation rocketSound = ResourceLocation.fromNamespaceAndPath("minecraft",
                    "entity.firework_rocket.blast");
            TFGNetworkHandler.sendSound(
                    serverLevel,
                    x, y, z,
                    rocketSound,
                    10.0f,
                    0.1f);

            ResourceLocation explosionSound = ResourceLocation.fromNamespaceAndPath("minecraft",
                    "entity.generic.explode");
            TFGNetworkHandler.sendSound(
                    serverLevel,
                    x, y, z,
                    explosionSound,
                    10.0f,
                    2.0f);
        }
    }
}
