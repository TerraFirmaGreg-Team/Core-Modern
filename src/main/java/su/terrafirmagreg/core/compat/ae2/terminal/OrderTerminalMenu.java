package su.terrafirmagreg.core.compat.ae2.terminal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.PacketDistributor;

import appeng.api.stacks.GenericStack;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;
import lombok.Getter;

import su.terrafirmagreg.core.compat.ae2.provider.StorageProviderHost;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.OrderDestinationsPacket;

public class OrderTerminalMenu extends MEStorageMenu {
    public static final MenuType<OrderTerminalMenu> TYPE = MenuTypeBuilder
            .create((id, inventory, host) -> new OrderTerminalMenu(id, inventory, host), OrderTerminalPart.class)
            .build("tfg_order_terminal");

    private static final String ACTION_SELECT = "selectDestination";
    private static final String ACTION_ORDER = "order";
    private static final String ACTION_ORDER_BATCH = "orderBatch";
    private static final String ACTION_RETURN = "returnBuffer";
    private static final String ACTION_OPEN_AMOUNT = "openAmount";
    private static final String ACTION_SET_STAGING = "setStaging";
    private static final String ACTION_CONFIRM_STAGED = "confirmStaged";
    private static final String ACTION_EDIT_STAGED = "editStaged";
    private static final String ACTION_OPEN_STAGED_AMOUNT = "openStagedAmount";
    private static final int SYNC_INTERVAL_TICKS = 20;
    private static final int MAX_ID_LENGTH = 36;
    private static final int MAX_SYNCED_WAITING = 512;

    public record OrderRequest(long serial, long amount, boolean craftMissing) {
    }

    public record OrderBatch(List<OrderRequest> requests) {
    }

    public record StagedEdit(int index, long expectedAmount, long amount) {
    }

    protected final OrderTerminalHost terminal;
    protected final OrderTerminalLogic logic;

    @GuiSync(110)
    public String selectedDestination = "";

    @GuiSync(111)
    public boolean staging;

    @Getter
    private List<OrderDestination> destinations = List.of();
    @Getter
    private List<GenericStack> waiting = List.of();
    @Getter
    private List<StagedStack> staged = List.of();
    @Nullable
    private OrderDestinationsPacket lastSynced;
    private int syncCountdown;

    public static void init() {
    }

    public OrderTerminalMenu(int id, Inventory playerInventory, OrderTerminalHost terminal) {
        this(TYPE, id, playerInventory, terminal);
    }

    protected OrderTerminalMenu(MenuType<?> type, int id, Inventory playerInventory, OrderTerminalHost terminal) {
        super(type, id, playerInventory, terminal);
        this.terminal = terminal;
        logic = terminal.getOrderLogic();
        if (isServerSide()) {
            selectedDestination = logic.getLastDestination();
        }
        registerClientAction(ACTION_SELECT, String.class, this::selectDestination);
        registerClientAction(ACTION_ORDER, OrderRequest.class, this::order);
        registerClientAction(ACTION_ORDER_BATCH, OrderBatch.class, this::orderBatch);
        registerClientAction(ACTION_RETURN, String.class, this::returnBuffer);
        registerClientAction(ACTION_OPEN_AMOUNT, OrderRequest.class, this::openAmount);
        registerClientAction(ACTION_SET_STAGING, Boolean.class, this::setStaging);
        registerClientAction(ACTION_CONFIRM_STAGED, this::confirmStaged);
        registerClientAction(ACTION_EDIT_STAGED, StagedEdit.class, this::editStaged);
        registerClientAction(ACTION_OPEN_STAGED_AMOUNT, Integer.class, this::openStagedAmount);
    }

    @Override
    protected boolean hideViewCells() {
        return true;
    }

    public void applySync(OrderDestinationsPacket packet) {
        destinations = packet.destinations();
        waiting = packet.waiting();
        staged = packet.staged();
    }

    public boolean hasSelectedDestination() {
        for (var destination : destinations) {
            if (destination.id().equals(selectedDestination)) {
                return true;
            }
        }
        return false;
    }

    public void selectDestination(String id) {
        if (id.length() > MAX_ID_LENGTH) {
            return;
        }
        selectedDestination = id;
        if (isClientSide()) {
            sendClientAction(ACTION_SELECT, id);
        } else {
            logic.setLastDestination(id);
            lastSynced = null;
        }
    }

    public void setStaging(Boolean enabled) {
        staging = enabled;
        if (isClientSide()) {
            sendClientAction(ACTION_SET_STAGING, enabled);
        } else {
            logic.setStaging(enabled);
        }
    }

    public void confirmStaged() {
        if (isClientSide()) {
            sendClientAction(ACTION_CONFIRM_STAGED);
        } else if (canInteractWithGrid()) {
            logic.confirmStaged(selectedDestination, getActionSource());
            lastSynced = null;
        }
    }

    public void editStaged(StagedEdit edit) {
        if (isClientSide()) {
            sendClientAction(ACTION_EDIT_STAGED, edit);
            var edited = new ArrayList<>(staged);
            if (edit.amount() > 0) {
                var stack = edited.get(edit.index());
                edited.set(edit.index(), new StagedStack(stack.what(), edit.amount(), stack.craftMissing()));
            } else {
                edited.remove(edit.index());
            }
            staged = edited;
            return;
        }
        var stack = stagedAt(edit.index());
        if (stack != null && stack.amount() == edit.expectedAmount()) {
            logic.setStagedAmount(selectedDestination, stack.what(), edit.amount());
            lastSynced = null;
        }
    }

    public void openStagedAmount(Integer index) {
        if (isClientSide()) {
            sendClientAction(ACTION_OPEN_STAGED_AMOUNT, index);
            return;
        }
        var stack = stagedAt(index);
        var locator = getLocator();
        if (stack != null && locator != null && getPlayer() instanceof ServerPlayer player) {
            OrderAmountMenu.open(player, locator, stack.what(), stack.amount(), stack.craftMissing(), selectedDestination, true);
        }
    }

    @Nullable
    private StagedStack stagedAt(int index) {
        var stacks = logic.getStaged(selectedDestination);
        return index >= 0 && index < stacks.size() ? stacks.get(index) : null;
    }

    public void order(OrderRequest request) {
        if (isClientSide()) {
            sendClientAction(ACTION_ORDER, request);
        } else {
            dispatch(request);
        }
    }

    public void orderBatch(OrderBatch batch) {
        if (isClientSide()) {
            sendClientAction(ACTION_ORDER_BATCH, batch);
        } else {
            batch.requests().forEach(this::dispatch);
        }
    }

    public void returnBuffer(String id) {
        if (isClientSide()) {
            sendClientAction(ACTION_RETURN, id);
            return;
        }
        var grid = logic.getGrid();
        var host = grid != null ? StorageProviderHost.find(grid, id) : null;
        if (host != null && canInteractWithGrid()) {
            host.getLogic().returnAll();
            lastSynced = null;
        }
    }

    public void openAmount(OrderRequest request) {
        if (isClientSide()) {
            sendClientAction(ACTION_OPEN_AMOUNT, request);
            return;
        }
        var what = getStackBySerial(request.serial());
        var locator = getLocator();
        if (what != null && locator != null && getPlayer() instanceof ServerPlayer player) {
            OrderAmountMenu.open(player, locator, what, what.getAmountPerUnit(), request.craftMissing(), selectedDestination, false);
        }
    }

    private void dispatch(OrderRequest request) {
        var what = getStackBySerial(request.serial());
        if (what != null && request.amount() > 0 && canInteractWithGrid()) {
            logic.dispatch(selectedDestination, what, request.amount(), request.craftMissing(), getActionSource());
            lastSynced = null;
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            staging = logic.isStaging();
        }
        super.broadcastChanges();
        if (isClientSide() || !(getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (lastSynced != null && --syncCountdown > 0) {
            return;
        }
        syncCountdown = SYNC_INTERVAL_TICKS;

        var current = new ArrayList<OrderDestination>();
        List<GenericStack> currentWaiting = List.of();
        var grid = logic.getGrid();
        if (grid != null) {
            for (var host : StorageProviderHost.allOn(grid)) {
                var destination = OrderDestination.of(host);
                current.add(destination);
                if (destination.id().equals(selectedDestination)) {
                    var buffer = host.getLogic().getBuffer();
                    currentWaiting = List.copyOf(buffer.subList(0, Math.min(buffer.size(), MAX_SYNCED_WAITING)));
                }
            }
            current.sort(Comparator.comparing((OrderDestination d) -> d.name().getString()).thenComparing(OrderDestination::id));
        }
        var packet = new OrderDestinationsPacket(current, currentWaiting, List.copyOf(logic.getStaged(selectedDestination)));
        if (!packet.equals(lastSynced)) {
            lastSynced = packet;
            TFGNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
