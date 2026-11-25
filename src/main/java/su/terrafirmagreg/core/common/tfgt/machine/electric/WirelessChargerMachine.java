package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import lombok.Getter;
import lombok.Setter;
import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.core.common.data.TFGTags;

// Credit to CosmicCore by Ghostipedia
// https://github.com/Frontiers-PackForge/CosmicCore/blob/main-1.20.1-forge/src/main/java/com/ghostipedia/cosmiccore/common/machine/WirelessChargerMachine.java
// Add the tag tfg:wireless_charger/booster so an item is prioritize the charge of the item

public class WirelessChargerMachine extends TieredEnergyMachine implements IMuiMachine {

    private static final int ACTIVE_LINGER_TICKS = 40;
    private static final long FE_PER_EU = 4L;
    private static final long BOOSTED_MULTIPLIER = 8L;

    @Getter
    @SaveField
    private ChargeMode mode = ChargeMode.SUPER_CHARGED;
    @Setter
    @Getter
    @SaveField
    private boolean rangeMessagesMuted = false;

    private final int longRange;
    private final int shortRange;
    private final long chargeAmount;

    private TickableSubscription chargeSubscription;
    private Set<UUID> previousPlayersInRange = new HashSet<>();
    private int activeLingerTicks = 0;
    private boolean visuallyActive = false;

    public WirelessChargerMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, new NotifiableEnergyContainer(GTValues.V[tier] * 256L, GTValues.V[tier], 8L, 0L, 0L) {

            @Override
            public long getInputAmperage() {
                return super.getMachine() instanceof WirelessChargerMachine charger && charger.mode != null
                        ? charger.mode.amperage
                        : 1L;
            }
        });
        this.longRange = longRangeFor(tier);
        this.shortRange = shortRangeFor(tier);
        this.chargeAmount = GTValues.V[tier];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote())
            return;
        setActive(false);
        chargeSubscription = subscribeServerTick(chargeSubscription, this::chargeLoop);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unsubscribe(chargeSubscription);
        chargeSubscription = null;
    }

    private int rangeFor(ChargeMode chargeMode) {
        return chargeMode == ChargeMode.SUPER_CHARGED ? shortRange : longRange;
    }

    private int currentRange() {
        return rangeFor(mode);
    }

    public void setMode(ChargeMode mode) {
        if (this.mode == mode)
            return;
        this.mode = mode;
        previousPlayersInRange.clear();
    }

    // Charge

    private void chargeLoop() {
        int tickRate = mode.tickRate;
        if (getOffsetTimer() % tickRate != 0)
            return;

        List<Player> players = resolveEligiblePlayers();
        notifyRangeChanges(players);

        long budget = Math.min(energyContainer.getEnergyStored(), chargeAmount * mode.amperage * tickRate);
        if (budget > 0 && !players.isEmpty()) {
            long perItemCap = chargeAmount * tickRate;
            List<ChargeTarget> boosted = new ArrayList<>();
            List<ChargeTarget> normal = new ArrayList<>();

            for (Player player : players) {
                for (ItemStack stack : collectStacks(player)) {
                    boolean isBoosted = isBoosted(stack);
                    ChargeTarget target = createTarget(stack,
                            isBoosted ? perItemCap * BOOSTED_MULTIPLIER : perItemCap);
                    if (target != null)
                        (isBoosted ? boosted : normal).add(target);
                }
            }

            for (ChargeTarget target : boosted) {
                if (budget <= 0)
                    break;
                budget -= charge(target, Math.min(budget, target.demand()));
            }

            // Split and smallest demands first so their leftover goes to the others
            normal.sort(Comparator.comparingLong(ChargeTarget::demand));
            for (int i = 0; i < normal.size() && budget > 0; i++) {
                ChargeTarget target = normal.get(i);
                long share = Math.max(1, budget / (normal.size() - i));
                budget -= charge(target, Math.min(share, target.demand()));
            }
        }

        updateActiveState(tickRate);
    }

    // Order of charge of items
    private static List<ItemStack> collectStacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        if (GTCEu.Mods.isCuriosLoaded()) {
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInv -> {
                var curios = curiosInv.getEquippedCurios();
                for (int i = 0; i < curios.getSlots(); i++) {
                    stacks.add(curios.getStackInSlot(i));
                }
            });
        }
        Inventory inv = player.getInventory();
        stacks.addAll(inv.armor);
        stacks.addAll(inv.offhand);
        stacks.addAll(inv.items);
        stacks.removeIf(ItemStack::isEmpty);
        return stacks;
    }

    private static boolean isBoosted(ItemStack stack) {
        return stack.is(TFGTags.Items.WirelessChargerBoosted);
    }

    @Nullable
    private ChargeTarget createTarget(ItemStack stack, long cap) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            if (!electricItem.chargeable())
                return null;
            long demand = electricItem.charge(cap, getTier(), true, true);
            return demand > 0 ? new ChargeTarget(electricItem, null, demand) : null;
        }

        // Forge Energy fallback 4 FE = 1 EU
        IEnergyStorage feStorage = stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (feStorage == null || !feStorage.canReceive())
            return null;
        int simulatedFe = feStorage.receiveEnergy((int) Math.min(Integer.MAX_VALUE, cap * FE_PER_EU), true);
        return simulatedFe > 0 ? new ChargeTarget(null, feStorage, (simulatedFe + FE_PER_EU - 1) / FE_PER_EU) : null;
    }

    private long charge(ChargeTarget target, long amount) {
        if (amount <= 0)
            return 0;

        long euCost;
        if (target.electricItem() != null) {
            euCost = target.electricItem().charge(amount, getTier(), true, false);
        } else {
            int insertedFe = target.feStorage().receiveEnergy((int) Math.min(Integer.MAX_VALUE, amount * FE_PER_EU),
                    false);
            euCost = (insertedFe + FE_PER_EU - 1) / FE_PER_EU;
        }

        if (euCost > 0) {
            energyContainer.changeEnergy(-euCost);
            activeLingerTicks = ACTIVE_LINGER_TICKS;
        }
        return euCost;
    }

    private record ChargeTarget(@Nullable IElectricItem electricItem, @Nullable IEnergyStorage feStorage,
            long demand) {
    }

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
        if (renderState == null || !renderState.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS))
            return;
        setRenderState(renderState.setValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                active ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
    }

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
        return player.distanceToSqr(Vec3.atCenterOf(getBlockPos())) <= radius * radius;
    }

    private void notifyRangeChanges(List<Player> players) {
        Set<UUID> current = new HashSet<>();
        String radius = FormattingUtil.formatNumbers(currentRange());

        for (Player player : players) {
            current.add(player.getUUID());
            if (!rangeMessagesMuted && !previousPlayersInRange.contains(player.getUUID())) {
                player.displayClientMessage(Component.translatable("tfg.wireless_charger.enter_range", radius), true);
            }
        }

        MinecraftServer server = getLevel().getServer();
        if (server != null && !rangeMessagesMuted) {
            for (UUID uuid : previousPlayersInRange) {
                if (current.contains(uuid))
                    continue;
                Player player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    player.displayClientMessage(Component.translatable("tfg.wireless_charger.left_range", radius),
                            true);
                }
            }
        }

        previousPlayersInRange = current;
    }

    public static int shortRangeFor(int tier) {
        return 16 * Math.max(1, tier - GTValues.MV);
    }

    public static int longRangeFor(int tier) {
        return 80 * Math.max(1, tier - GTValues.MV);
    }

    @Override
    protected @NotNull InteractionResult onScrewdriverClick(@NotNull ExtendedUseOnContext context) {
        if (isRemote())
            return InteractionResult.SUCCESS;

        setMode(mode == ChargeMode.SUPER_CHARGED ? ChargeMode.MIXED : ChargeMode.SUPER_CHARGED);

        var player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable("tfg.wireless_charger.mode." + mode.ordinal(),
                    FormattingUtil.formatNumbers(currentRange())), false);
        }
        return InteractionResult.SUCCESS;
    }

    // GUI

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var superMode = new BooleanSyncValue(() -> mode == ChargeMode.SUPER_CHARGED,
                value -> setMode(value ? ChargeMode.SUPER_CHARGED : ChargeMode.MIXED)).allowC2S();
        var muted = new BooleanSyncValue(this::isRangeMessagesMuted, this::setRangeMessagesMuted).allowC2S();

        return MachineUIPanelBuilder.panelBuilder(this)
                .rightConfigurators(flow -> flow
                        .child(new ToggleButton()
                                .value(superMode)
                                .overlay(true, new ItemDrawable(Items.REDSTONE_BLOCK))
                                .overlay(false, new ItemDrawable(Items.ENDER_EYE))
                                .tooltipAutoUpdate(true)
                                .tooltipDynamic(r -> {
                                    ChargeMode shown = superMode.getBoolValue() ? ChargeMode.SUPER_CHARGED : ChargeMode.MIXED;
                                    r.addLine(Component.translatable("tfg.gui.wireless_charger.mode",
                                            shown.getDisplayName()));
                                    r.addLine(Component.translatable(shown.langKey + ".desc")
                                            .withStyle(ChatFormatting.GRAY));
                                    r.addLine(Component.translatable("tfg.gui.wireless_charger.mode.switch")
                                            .withStyle(ChatFormatting.DARK_GRAY));
                                }))
                        .child(new ToggleButton()
                                .value(muted)
                                .overlay(new ItemDrawable(Items.BELL))
                                .tooltipAutoUpdate(true)
                                .tooltipDynamic(r -> r.addLine(Component.translatable(muted.getBoolValue() ? "tfg.gui.wireless_charger.messages.muted" : "tfg.gui.wireless_charger.messages.shown")))));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings) {
        IntSyncValue modeSync = new IntSyncValue(() -> mode.ordinal());
        LongSyncValue storedSync = new LongSyncValue(energyContainer::getEnergyStored);
        IntSyncValue playersSync = new IntSyncValue(() -> previousPlayersInRange.size());
        IntSyncValue activeSync = new IntSyncValue(() -> visuallyActive ? 1 : 0);
        syncManager.syncValue("chargerMode", modeSync);
        syncManager.syncValue("chargerStored", storedSync);
        syncManager.syncValue("chargerPlayers", playersSync);
        syncManager.syncValue("chargerActive", activeSync);

        mainWidget.child(Flow.column()
                .coverChildren()
                .padding(6)
                .childPadding(3)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .child(Text.dynamic(() -> Component.translatable("tfg.gui.wireless_charger.mode",
                        shownMode(modeSync).getDisplayName().copy().withStyle(ChatFormatting.DARK_AQUA))).asWidget())
                .child(Text.dynamic(() -> Component.translatable("tfg.gui.wireless_charger.range",
                        FormattingUtil.formatNumbers(rangeFor(shownMode(modeSync))))).asWidget())
                .child(Text.dynamic(() -> {
                    long amps = shownMode(modeSync).amperage;
                    return Component.translatable("tfg.gui.wireless_charger.input", amps,
                            FormattingUtil.formatNumbers(chargeAmount * amps));
                }).asWidget())
                .child(Text.dynamic(() -> Component.translatable("tfg.gui.wireless_charger.per_item",
                        FormattingUtil.formatNumbers(chargeAmount))).asWidget())
                .child(Text.dynamic(() -> Component.translatable("tfg.gui.wireless_charger.boosted",
                        FormattingUtil.formatNumbers(
                                chargeAmount * Math.min(BOOSTED_MULTIPLIER, shownMode(modeSync).amperage))))
                        .asWidget())
                .child(new Rectangle().color(0xFF555555).asWidget()
                        .height(1).widthRel(1.0f).marginTop(2).marginBottom(2))
                .child(Text.dynamic(() -> Component.translatable("tfg.gui.wireless_charger.stored",
                        FormattingUtil.formatNumbers(storedSync.getLongValue()),
                        FormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()))).asWidget())
                .child(Text.dynamic(() -> Component.translatable("tfg.gui.wireless_charger.players",
                        playersSync.getIntValue())).asWidget())
                .child(Text.dynamic(() -> activeSync.getIntValue() == 1 ? Component.translatable("tfg.gui.wireless_charger.status.charging")
                        .withStyle(ChatFormatting.DARK_GREEN)
                        : Component.translatable("tfg.gui.wireless_charger.status.idle")
                                .withStyle(ChatFormatting.DARK_GRAY))
                        .asWidget()));
    }

    private static ChargeMode shownMode(IntSyncValue modeSync) {
        ChargeMode[] modes = ChargeMode.values();
        int index = modeSync.getIntValue();
        return index >= 0 && index < modes.length ? modes[index] : ChargeMode.SUPER_CHARGED;
    }

    public enum ChargeMode {

        SUPER_CHARGED(8L, 4, "tfg.gui.wireless_charger.mode.super_charged"),
        MIXED(1L, 20, "tfg.gui.wireless_charger.mode.mixed");

        public final long amperage;
        public final int tickRate;
        public final String langKey;

        ChargeMode(long amperage, int tickRate, String langKey) {
            this.amperage = amperage;
            this.tickRate = tickRate;
            this.langKey = langKey;
        }

        public Component getDisplayName() {
            return Component.translatable(langKey);
        }
    }
}
