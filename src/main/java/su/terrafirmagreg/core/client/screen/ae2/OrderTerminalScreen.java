package su.terrafirmagreg.core.client.screen.ae2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.ToggleButton;
import appeng.menu.me.common.GridInventoryEntry;

import su.terrafirmagreg.core.compat.ae2.provider.StorageProviderLogic;
import su.terrafirmagreg.core.compat.ae2.terminal.OrderDestination;
import su.terrafirmagreg.core.compat.ae2.terminal.OrderTerminalMenu;

public class OrderTerminalScreen extends MEStorageScreen<OrderTerminalMenu> {
    private static final int CELL_X = 8;
    private static final int CELL_WIDTH = 146;
    private static final int CELL_HEIGHT = 21;
    private static final int CELL_STEP = OrderTerminalStyle.CELL_STEP;
    private static final int MIN_CELLS_WITHOUT_REPEAT = 2;
    private static final int SEARCH_X = 7;
    private static final int SEARCH_ABOVE_CELLS = 15;
    private static final int TOGGLE_X = 152;
    private static final int TOGGLE_ABOVE_CELLS = 18;
    private static final int SCROLLBAR_X = 156;
    private static final int PANEL_X = 173;
    private static final int CONFIRM_IN_PANEL_X = 3;
    private static final int RETURN_SIZE = 9;
    private static final int RETURN_MARGIN = 3;
    private static final float STATUS_SCALE = 0.75f;

    private final OrderTerminalStyle terminalStyle;
    private final Blitter cellRow;
    private final Blitter bottomEnd;
    private final AETextField destinationSearch;
    private final Scrollbar destinationScrollbar;
    private final ToggleButton stagingToggle;
    private final OrderStagingPanel stagingPanel;
    private final Button stagingConfirm;
    private List<OrderDestination> filtered = List.of();

    public OrderTerminalScreen(OrderTerminalMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, OrderTerminalStyle.install(style));
        terminalStyle = (OrderTerminalStyle) style.getTerminalStyle();
        cellRow = style.getImage("cellRow");
        bottomEnd = style.getImage("bottomEnd");
        destinationSearch = widgets.addTextField("destinationSearch");
        destinationSearch.setPlaceholder(Component.translatable("gui.tfg.order_terminal.search_destinations"));
        destinationScrollbar = widgets.addScrollBar("destinationScrollbar");
        destinationScrollbar.setCaptureMouseWheel(false);
        stagingToggle = new ToggleButton(Icon.VALID, Icon.INVALID, menu::setStaging);
        stagingToggle.setTooltipOn(List.of(Component.translatable("gui.tfg.order_terminal.staging"), Component.translatable("gui.tfg.order_terminal.staging.on")));
        stagingToggle.setTooltipOff(List.of(Component.translatable("gui.tfg.order_terminal.staging"), Component.translatable("gui.tfg.order_terminal.staging.off")));
        widgets.add("stagingToggle", stagingToggle);
        stagingPanel = new OrderStagingPanel(menu, style.getImage("stagingTop"), style.getImage("stagingRow"), style.getImage("stagingRowsEnd"), style.getImage("stagingStrip"),
                style.getImage("stagingBottom"));
        widgets.add("stagingPanel", stagingPanel);
        stagingConfirm = widgets.addButton("stagingConfirm", Component.translatable("gui.tfg.order_terminal.confirm"), menu::confirmStaged);
    }

    @Override
    public void init() {
        super.init();
        int top = cellsTop();
        destinationSearch.move(new Point(leftPos + SEARCH_X, topPos + top - SEARCH_ABOVE_CELLS));
        destinationScrollbar.setPosition(new Point(SCROLLBAR_X, top));
        destinationScrollbar.setHeight(visibleCells() * CELL_STEP - 1);
        stagingToggle.setX(leftPos + TOGGLE_X);
        stagingToggle.setY(topPos + top - TOGGLE_ABOVE_CELLS);
        stagingPanel.setHeight(visibleCells() * CELL_STEP + 1);
        stagingPanel.setPosition(new Point(PANEL_X, top - 1));
        stagingConfirm.setX(leftPos + PANEL_X + CONFIRM_IN_PANEL_X);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        int y = offsetY + cellsTop() + CELL_STEP;
        for (int i = MIN_CELLS_WITHOUT_REPEAT; i < visibleCells(); i++) {
            cellRow.dest(offsetX, y).blit(guiGraphics);
            y += CELL_STEP;
        }
        bottomEnd.dest(offsetX, y).blit(guiGraphics);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        stagingToggle.setState(menu.staging);
        stagingConfirm.visible = menu.staging && menu.hasSelectedDestination();
        stagingConfirm.active = !menu.getStaged().isEmpty();
        stagingConfirm.setY(topPos + cellsTop() - 1 + stagingPanel.getConfirmY());
        stagingConfirm.setHeight(stagingPanel.getConfirmHeight());
        var query = destinationSearch.getValue().toLowerCase(Locale.ROOT);
        var matches = new ArrayList<OrderDestination>();
        for (var destination : menu.getDestinations()) {
            if (destination.name().getString().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(destination);
            }
        }
        filtered = matches;
        destinationScrollbar.setRange(0, Math.max(0, filtered.size() - visibleCells()), 1);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);
        if (filtered.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.tfg.order_terminal.no_destinations"), CELL_X + 3, cellsTop() + 7, 0xFFFFFF, true);
            return;
        }

        int guiMouseX = mouseX - offsetX;
        int guiMouseY = mouseY - offsetY;
        int hovered = cellAt(guiMouseX, guiMouseY);
        for (int i = 0; i < visibleCells(); i++) {
            int index = destinationScrollbar.getCurrentScroll() + i;
            if (index < filtered.size()) {
                drawCell(guiGraphics, filtered.get(index), cellsTop() + i * CELL_STEP, i == hovered, guiMouseX, guiMouseY);
            }
        }
    }

    private void drawCell(GuiGraphics guiGraphics, OrderDestination destination, int y, boolean hovered, int mouseX, int mouseY) {
        if (destination.id().equals(menu.selectedDestination)) {
            guiGraphics.fill(CELL_X, y, CELL_X + CELL_WIDTH, y + CELL_HEIGHT, 0x60FFFFFF);
        } else if (hovered) {
            guiGraphics.fill(CELL_X, y, CELL_X + CELL_WIDTH, y + CELL_HEIGHT, 0x30FFFFFF);
        }

        AEKeyRendering.drawInGui(minecraft, guiGraphics, CELL_X + 2, y + 2, destination.icon());

        int textX = CELL_X + 21;
        int textWidth = CELL_WIDTH - 21 - RETURN_SIZE - 2 * RETURN_MARGIN;
        guiGraphics.drawString(font, font.plainSubstrByWidth(destination.name().getString(), textWidth), textX, y + 2, 0xFFFFFF, true);

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(textX, y + 12, 0);
        pose.scale(STATUS_SCALE, STATUS_SCALE, 1);
        guiGraphics.drawString(font, statusText(destination), 0, 0, statusColor(destination.status()), true);
        pose.popPose();

        if (hasPending(destination)) {
            int buttonX = CELL_X + CELL_WIDTH - RETURN_SIZE - RETURN_MARGIN;
            int buttonY = y + (CELL_HEIGHT - RETURN_SIZE) / 2;
            boolean overButton = mouseX >= buttonX && mouseX < buttonX + RETURN_SIZE && mouseY >= buttonY && mouseY < buttonY + RETURN_SIZE;
            guiGraphics.fill(buttonX, buttonY, buttonX + RETURN_SIZE, buttonY + RETURN_SIZE, overButton ? 0xFFC65C5C : 0xFF8B3A3A);
            guiGraphics.drawCenteredString(font, "x", buttonX + RETURN_SIZE / 2 + 1, buttonY, 0xFFFFFF);
        }
    }

    private static boolean hasPending(OrderDestination destination) {
        return destination.waitingStacks() > 0 || destination.crafts() > 0;
    }

    private static Component statusText(OrderDestination destination) {
        return switch (destination.status()) {
            case IDLE -> Component.translatable("gui.tfg.order_terminal.status.idle");
            case WAITING -> Component.translatable("gui.tfg.order_terminal.status.waiting", destination.waitingStacks());
            case CRAFTING -> Component.translatable("gui.tfg.order_terminal.status.crafting", destination.crafts());
            case WAITING_FOR_CPU -> Component.translatable("gui.tfg.order_terminal.status.waiting_for_cpu");
            case MISSING_INGREDIENTS -> Component.translatable("gui.tfg.order_terminal.status.missing_ingredients");
            case OFFLINE -> Component.translatable("gui.tfg.order_terminal.status.offline");
        };
    }

    private static int statusColor(StorageProviderLogic.Status status) {
        return switch (status) {
            case IDLE -> 0xD0D0D0;
            case WAITING, CRAFTING -> 0xFFE680;
            case WAITING_FOR_CPU, MISSING_INGREDIENTS, OFFLINE -> 0xFF9090;
        };
    }

    private int visibleCells() {
        return terminalStyle.getPossibleRows(imageHeight);
    }

    protected int cellsTop() {
        return terminalStyle.getFirstCellY(visibleCells());
    }

    private int cellAt(double guiX, double guiY) {
        if (guiX < CELL_X || guiX >= CELL_X + CELL_WIDTH || guiY < cellsTop()) {
            return -1;
        }
        int cell = (int) (guiY - cellsTop()) / CELL_STEP;
        boolean inCell = cell < visibleCells() && (guiY - cellsTop()) % CELL_STEP < CELL_HEIGHT;
        return inCell && destinationScrollbar.getCurrentScroll() + cell < filtered.size() ? cell : -1;
    }

    @Nullable
    private OrderDestination destinationAt(double guiX, double guiY) {
        int cell = cellAt(guiX, guiY);
        return cell >= 0 ? filtered.get(destinationScrollbar.getCurrentScroll() + cell) : null;
    }

    private boolean isOverReturnButton(OrderDestination destination, double guiX, double guiY) {
        int buttonX = CELL_X + CELL_WIDTH - RETURN_SIZE - RETURN_MARGIN;
        int cellTop = cellsTop() + cellAt(guiX, guiY) * CELL_STEP;
        int buttonY = cellTop + (CELL_HEIGHT - RETURN_SIZE) / 2;
        return hasPending(destination) && guiX >= buttonX && guiX < buttonX + RETURN_SIZE && guiY >= buttonY && guiY < buttonY + RETURN_SIZE;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (destinationSearch.isMouseOver(x, y) && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            destinationSearch.setValue("");
        }
        double guiX = x - leftPos;
        double guiY = y - topPos;
        var destination = destinationAt(guiX, guiY);
        if (destination != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isOverReturnButton(destination, guiX, guiY)) {
                menu.returnBuffer(destination.id());
            } else {
                menu.selectDestination(destination.id().equals(menu.selectedDestination) ? "" : destination.id());
            }
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        if (cellAt(x - leftPos, y - topPos) >= 0 || destinationSearch.isMouseOver(x, y)) {
            destinationScrollbar.setCurrentScroll(destinationScrollbar.getCurrentScroll() - (int) Math.signum(wheelDelta));
            return true;
        }
        return super.mouseScrolled(x, y, wheelDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (destinationSearch.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                destinationSearch.setFocused(false);
                setFocused(null);
            } else {
                destinationSearch.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (destinationSearch.isFocused()) {
            return destinationSearch.charTyped(character, modifiers);
        }
        return super.charTyped(character, modifiers);
    }

    @Override
    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry entry, int mouseButton, ClickType clickType) {
        if (entry == null || !menu.getCarried().isEmpty() || !menu.hasSelectedDestination()) {
            super.handleGridInventoryEntryMouseClick(entry, mouseButton, clickType);
            return;
        }
        long serial = entry.getSerial();
        var wantsCraft = clickType == ClickType.CLONE || hasControlDown() || entry.getStoredAmount() == 0;
        if (wantsCraft && entry.isCraftable()) {
            menu.openAmount(new OrderTerminalMenu.OrderRequest(serial, 0, true));
        } else if (clickType == ClickType.QUICK_MOVE) {
            menu.openAmount(new OrderTerminalMenu.OrderRequest(serial, 0, false));
        } else if (clickType == ClickType.PICKUP) {
            long amount = mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? entry.getWhat().getAmountPerUnit() : stackSize(entry.getWhat());
            menu.order(new OrderTerminalMenu.OrderRequest(serial, amount, false));
        }
    }

    private static long stackSize(AEKey what) {
        return what instanceof AEItemKey itemKey ? itemKey.getMaxStackSize() : what.getAmountPerUnit();
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        double guiX = x - leftPos;
        double guiY = y - topPos;
        var destination = destinationAt(guiX, guiY);
        if (destination == null) {
            super.renderTooltip(guiGraphics, x, y);
            return;
        }
        var lines = new ArrayList<Component>();
        if (isOverReturnButton(destination, guiX, guiY)) {
            lines.add(Component.translatable("gui.tfg.order_terminal.return"));
        } else {
            lines.add(destination.name());
            for (var stack : destination.preview()) {
                lines.add(Component.literal(stack.what().formatAmount(stack.amount(), AmountFormat.FULL) + " ").append(stack.what().getDisplayName()));
            }
            int hidden = destination.waitingStacks() - destination.preview().size();
            if (hidden > 0) {
                lines.add(Component.translatable("gui.tfg.order_terminal.more", hidden));
            }
        }
        drawTooltip(guiGraphics, x, y, lines);
    }
}
