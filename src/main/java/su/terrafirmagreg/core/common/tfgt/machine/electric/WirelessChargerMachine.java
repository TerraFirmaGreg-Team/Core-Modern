package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import dev.ftb.mods.ftbteams.data.PlayerTeam;
import top.theillusivec4.curios.api.CuriosApi;

// Credit to CosmicCore by Ghostipedia
// https://github.com/Frontiers-PackForge/CosmicCore/blob/main-1.20.1-forge/src/main/java/com/ghostipedia/cosmiccore/common/machine/WirelessChargerMachine.java

public class WirelessChargerMachine extends TieredEnergyMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessChargerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private ChargeMode mode = ChargeMode.SUPER_CHARGED;

    private final int longRange;
    private final int shortRange;
    private final long chargeAmount;

    private TickableSubscription chargeSubscription;
    private Set<UUID> previousPlayersInRange = new HashSet<>();

    private static final int ACTIVE_LINGER_TICKS = 40; // Just so it doesn't flicker
    private int activeLingerTicks = 0;
    private boolean visuallyActive = false;

    public WirelessChargerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.longRange = longRangeFor(tier);
        this.shortRange = shortRangeFor(tier);
        this.chargeAmount = GTValues.V[tier];
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long voltage = GTValues.V[getTier()];
        return new NotifiableEnergyContainer(this, voltage * 64L, voltage, 4L, 0L, 0L) {

            @Override
            public long getInputAmperage() {
                return mode == ChargeMode.SUPER_CHARGED ? 4L : 1L;
            }
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote())
            return;
        setActive(false);
        chargeSubscription = subscribeServerTick(this::chargeLoop);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (chargeSubscription != null) {
            chargeSubscription.unsubscribe();
            chargeSubscription = null;
        }
    }

    private int currentRange() {
        return mode == ChargeMode.SUPER_CHARGED ? shortRange : longRange;
    }

    private void chargeLoop() {
        int tickRate = mode == ChargeMode.SUPER_CHARGED ? 4 : 20;
        if (getOffsetTimer() % tickRate != 0)
            return;

        List<Player> players = resolveEligiblePlayers();
        notifyRangeChanges(players);

        long maxChargeValue = chargeAmount * energyContainer.getInputAmperage();
        if (energyContainer.getEnergyStored() >= maxChargeValue) {
            for (Player player : players) {
                chargePlayer(player, maxChargeValue);
            }
        }

        updateActiveState(tickRate);
    }

    // Use to enable the active state and disable it when timer at 0

    private void updateActiveState(int tickRate) {
        boolean shouldBeActive = activeLingerTicks > 0;
        activeLingerTicks = Math.max(0, activeLingerTicks - tickRate);

        if (shouldBeActive == visuallyActive)
            return;
        visuallyActive = shouldBeActive;
        setActive(shouldBeActive);
    }

    private void setActive(boolean active) {
        MachineRenderState renderState = getRenderState();
        if (renderState == null)
            return;
        if (renderState.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            setRenderState(renderState.setValue(
                    GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                    active ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
        }
    }

    // Check for players in range - less heavy that scanning every entities

    private List<Player> resolveEligiblePlayers() {
        List<Player> players = new ArrayList<>();
        MinecraftServer server = getLevel().getServer();
        if (server == null)
            return players;

        var owner = getOwner();
        if (owner instanceof PlayerOwner) {
            addIfInRange(players, server, owner.getUUID());
        } else if (owner instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getTeam();
            if (team == null)
                return players;
            var members = team.isPlayerTeam()
                    ? ((PlayerTeam) team).getEffectiveTeam().getMembers()
                    : team.getMembers();
            for (UUID uuid : members) {
                addIfInRange(players, server, uuid);
            }
        }
        return players;
    }

    private void addIfInRange(List<Player> players, MinecraftServer server, UUID uuid) {
        Player player = server.getPlayerList().getPlayer(uuid);
        if (player != null && isPlayerInRange(player)) {
            players.add(player);
        }
    }

    private boolean isPlayerInRange(Player player) {
        if (player.level() != getLevel())
            return false;
        double radius = currentRange();
        return player.distanceToSqr(Vec3.atCenterOf(getPos())) <= radius * radius;
    }

    private void chargePlayer(Player player, long maxChargeValue) {
        if (GTCEu.Mods.isCuriosLoaded()) {
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInv -> {
                var curios = curiosInv.getEquippedCurios();
                for (int i = 0; i < curios.getSlots(); i++) {
                    tryChargeStack(curios.getStackInSlot(i), maxChargeValue);
                }
            });
        }

        var playerInv = player.getInventory();
        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            tryChargeStack(playerInv.getItem(i), maxChargeValue);
        }
    }

    private static final long FE_PER_EU = 4L;

    private void tryChargeStack(ItemStack stack, long maxChargeValue) {
        if (stack.isEmpty())
            return;
        if (energyContainer.getEnergyStored() < maxChargeValue)
            return;

        // First check if there is a GT Item to charge
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            if (!electricItem.chargeable())
                return;
            long charged = electricItem.charge(maxChargeValue, getTier(), true, false);
            if (charged > 0) {
                energyContainer.changeEnergy(-charged);
                activeLingerTicks = ACTIVE_LINGER_TICKS;
            }
            return;
        }

        // Fallback to Forge Energy with GT converter rule at 4 FE = 1 EU
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(feStorage -> {
            if (!feStorage.canReceive())
                return;
            int maxFe = (int) Math.min(Integer.MAX_VALUE, maxChargeValue * FE_PER_EU);
            int insertedFe = feStorage.receiveEnergy(maxFe, false);
            if (insertedFe > 0) {
                long euCost = (insertedFe + FE_PER_EU - 1) / FE_PER_EU;
                energyContainer.changeEnergy(-euCost);
                activeLingerTicks = ACTIVE_LINGER_TICKS;
            }
        });
    }

    private void notifyRangeChanges(List<Player> players) {
        Set<UUID> current = new HashSet<>();
        for (Player player : players) {
            current.add(player.getUUID());
        }

        String radius = FormattingUtil.formatNumbers(currentRange());
        for (Player player : players) {
            if (!previousPlayersInRange.contains(player.getUUID())) {
                player.displayClientMessage(
                        Component.translatable("tfg.wireless_charger.enter_range", radius), true);
            }
        }
        // Let players know they left the range
        MinecraftServer server = getLevel().getServer();
        if (server != null) {
            for (UUID uuid : previousPlayersInRange) {
                if (!current.contains(uuid)) {
                    Player player = server.getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        player.displayClientMessage(
                                Component.translatable("tfg.wireless_charger.left_range", radius), true);
                    }
                }
            }
        }

        previousPlayersInRange = current;
    }

    // Formula for range of charging

    public static int shortRangeFor(int tier) {
        return 16 * Math.max(1, tier - GTValues.MV);
    }

    public static int longRangeFor(int tier) {
        return 80 * Math.max(1, tier - GTValues.MV);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
            BlockHitResult hitResult) {
        if (!getLevel().isClientSide) {
            mode = mode == ChargeMode.SUPER_CHARGED ? ChargeMode.MIXED : ChargeMode.SUPER_CHARGED;
            playerIn.displayClientMessage(Component.translatable(
                    "tfg.wireless_charger.mode." + mode.ordinal(),
                    FormattingUtil.formatNumbers(currentRange())), false);
            // Clear so players within range know that the mode changed
            previousPlayersInRange.clear();
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    public enum ChargeMode {
        SUPER_CHARGED,
        MIXED
    }
}
