package su.terrafirmagreg.core.client.screen.ae2;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import lombok.Setter;

import su.terrafirmagreg.core.compat.ae2.terminal.OrderTerminalMenu;

public class OrderStagingPanel implements ICompositeWidget {
    private static final int WIDTH = 69;
    private static final int SLOT_X = 4;
    private static final int SLOT_Y = 4;
    private static final int SLOT_SIZE = 16;
    private static final int SLOT_STEP = 18;
    private static final int COLUMNS = 3;
    private static final int SCROLLBAR_X = 58;
    private static final int SCROLLBAR_Y = 4;
    private static final int FRAME_HEIGHT = 6;
    private static final int SLOTS_TOP = 3;
    private static final int MIN_CONFIRM_STRIP = 18;
    private static final int MAX_CONFIRM_HEIGHT = 20;

    private final OrderTerminalMenu menu;
    private final Blitter top;
    private final Blitter row;
    private final Blitter rowsEnd;
    private final Blitter strip;
    private final Blitter bottom;
    private final Scrollbar scrollbar = new Scrollbar(Scrollbar.SMALL);
    private int x;
    private int y;
    @Setter
    private int height;

    public OrderStagingPanel(OrderTerminalMenu menu, Blitter top, Blitter row, Blitter rowsEnd, Blitter strip, Blitter bottom) {
        this.menu = menu;
        this.top = top;
        this.row = row;
        this.rowsEnd = rowsEnd;
        this.strip = strip;
        this.bottom = bottom;
        scrollbar.setCaptureMouseWheel(false);
    }

    private int slotRows() {
        int available = height - FRAME_HEIGHT - (menu.staging ? MIN_CONFIRM_STRIP : 0);
        return Math.max(1, available / SLOT_STEP);
    }

    private int stripHeight() {
        return height - FRAME_HEIGHT - slotRows() * SLOT_STEP;
    }

    private int slotsBottom() {
        return SLOTS_TOP + slotRows() * SLOT_STEP;
    }

    public int getConfirmHeight() {
        return Math.min(stripHeight() - 2, MAX_CONFIRM_HEIGHT);
    }

    public int getConfirmY() {
        return slotsBottom() + (stripHeight() + 1 - getConfirmHeight()) / 2;
    }

    private List<GenericStack> stacks() {
        if (!menu.staging) {
            return menu.getWaiting();
        }
        return menu.getStaged().stream().map(stack -> new GenericStack(stack.what(), stack.amount())).toList();
    }

    private int slotAt(Point mouse) {
        int relX = mouse.getX() - x - SLOT_X;
        int relY = mouse.getY() - y - SLOT_Y;
        if (relX < 0 || relY < 0 || relX % SLOT_STEP >= SLOT_SIZE || relY % SLOT_STEP >= SLOT_SIZE) {
            return -1;
        }
        int column = relX / SLOT_STEP;
        int slotRow = relY / SLOT_STEP;
        return column < COLUMNS && slotRow < slotRows() ? (scrollbar.getCurrentScroll() + slotRow) * COLUMNS + column : -1;
    }

    @Override
    public boolean isVisible() {
        return menu.hasSelectedDestination();
    }

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
        scrollbar.setPosition(new Point(x + SCROLLBAR_X, y + SCROLLBAR_Y));
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return isVisible() ? new Rect2i(x, y, WIDTH, height) : new Rect2i(0, 0, 0, 0);
    }

    @Override
    public void tick() {
        scrollbar.tick();
    }

    @Override
    public void updateBeforeRender() {
        int rows = slotRows();
        int stackRows = (stacks().size() + COLUMNS - 1) / COLUMNS;
        scrollbar.setHeight(rows * SLOT_STEP - 2);
        scrollbar.setRange(0, Math.max(0, stackRows - rows), 1);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        int drawX = bounds.getX() + x;
        int drawY = bounds.getY() + y;
        top.dest(drawX, drawY).blit(guiGraphics);
        drawY += top.getSrcHeight();
        for (int i = 1; i < slotRows(); i++) {
            row.dest(drawX, drawY).blit(guiGraphics);
            drawY += row.getSrcHeight();
        }
        rowsEnd.dest(drawX, drawY).blit(guiGraphics);
        drawY += rowsEnd.getSrcHeight();
        for (int i = 0; i < stripHeight(); i++) {
            strip.dest(drawX, drawY + i).blit(guiGraphics);
        }
        bottom.dest(drawX, drawY + stripHeight()).blit(guiGraphics);
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        var minecraft = Minecraft.getInstance();
        var stacks = stacks();
        int hovered = slotAt(mouse);
        int first = scrollbar.getCurrentScroll() * COLUMNS;
        for (int index = first; index < Math.min(stacks.size(), first + COLUMNS * slotRows()); index++) {
            int slotX = x + SLOT_X + (index - first) % COLUMNS * SLOT_STEP;
            int slotY = y + SLOT_Y + (index - first) / COLUMNS * SLOT_STEP;
            if (index == hovered) {
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x60FFFFFF);
            }
            var stack = stacks.get(index);
            AEKeyRendering.drawInGui(minecraft, guiGraphics, slotX, slotY, stack.what());
            StackSizeRenderer.renderSizeLabel(guiGraphics, minecraft.font, slotX, slotY, stack.what().formatAmount(stack.amount(), AmountFormat.SLOT));
        }
        scrollbar.drawForegroundLayer(guiGraphics, bounds, mouse);
    }

    @Override
    public boolean onMouseDown(Point mouse, int button) {
        if (mouse.getY() >= y + slotsBottom()) {
            return false;
        }
        if (mouse.isIn(scrollbar.getBounds())) {
            scrollbar.onMouseDown(mouse, button);
        } else if (menu.staging) {
            int index = slotAt(mouse);
            if (index >= 0 && index < menu.getStaged().size()) {
                clickStaged(index, button);
            }
        }
        return true;
    }

    private void clickStaged(int index, int button) {
        var stack = menu.getStaged().get(index);
        if (Screen.hasShiftDown()) {
            menu.editStaged(new OrderTerminalMenu.StagedEdit(index, stack.amount(), 0));
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            menu.openStagedAmount(index);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            menu.editStaged(new OrderTerminalMenu.StagedEdit(index, stack.amount(), stack.amount() - stack.what().getAmountPerUnit()));
        }
    }

    @Override
    public boolean onMouseUp(Point mouse, int button) {
        return scrollbar.onMouseUp(mouse, button);
    }

    @Override
    public boolean wantsAllMouseUpEvents() {
        return scrollbar.wantsAllMouseUpEvents();
    }

    @Override
    public boolean onMouseDrag(Point mouse, int button) {
        return scrollbar.onMouseDrag(mouse, button);
    }

    @Override
    public boolean onMouseWheel(Point mouse, double delta) {
        scrollbar.onMouseWheel(mouse, delta);
        return true;
    }

    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        var stacks = stacks();
        int index = slotAt(new Point(mouseX, mouseY));
        if (index < 0 || index >= stacks.size()) {
            return null;
        }
        var stack = stacks.get(index);
        var lines = new ArrayList<>(AEKeyRendering.getTooltip(stack.what()));
        lines.add(Component.literal(stack.what().formatAmount(stack.amount(), AmountFormat.FULL)));
        if (menu.staging) {
            if (menu.getStaged().get(index).craftMissing()) {
                lines.add(Component.translatable("gui.tfg.order_terminal.staged.craft"));
            }
            lines.add(Component.translatable("gui.tfg.order_terminal.staged.edit"));
            lines.add(Component.translatable("gui.tfg.order_terminal.staged.remove_one"));
            lines.add(Component.translatable("gui.tfg.order_terminal.staged.remove"));
        }
        return new Tooltip(lines);
    }
}
