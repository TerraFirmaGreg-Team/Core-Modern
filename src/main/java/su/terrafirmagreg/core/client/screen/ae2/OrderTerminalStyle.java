package su.terrafirmagreg.core.client.screen.ae2;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;

import su.terrafirmagreg.core.mixins.client.ae2.ScreenStyleAccessor;

public class OrderTerminalStyle extends TerminalStyle {
    public static final int CELL_STEP = 22;
    private static final int MIN_DRAWN_ROWS = 2;

    private final int endHeight;

    private OrderTerminalStyle(TerminalStyle base, int endHeight) {
        setHeader(base.getHeader());
        setFirstRow(base.getFirstRow());
        setRow(base.getRow());
        setLastRow(base.getLastRow());
        setBottom(base.getBottom());
        setSlotsPerRow(base.getSlotsPerRow());
        setSortable(base.isSortable());
        setSupportsAutoCrafting(base.isSupportsAutoCrafting());
        setShowTooltipsWithItemInHand(base.isShowTooltipsWithItemInHand());
        this.endHeight = endHeight;
    }

    public static ScreenStyle install(ScreenStyle style) {
        if (!(style.getTerminalStyle() instanceof OrderTerminalStyle)) {
            var terminalStyle = new OrderTerminalStyle(style.getTerminalStyle(), style.getImage("bottomEnd").getSrcHeight());
            ((ScreenStyleAccessor) style).tfg$setTerminalStyle(terminalStyle);
        }
        return style;
    }

    @Override
    public int getPossibleRows(int availableHeight) {
        int fixedHeight = getHeader().getSrcHeight() + getBottom().getSrcHeight() + endHeight - MIN_DRAWN_ROWS * CELL_STEP;
        return (availableHeight - fixedHeight) / (getRow().getSrcHeight() + CELL_STEP);
    }

    @Override
    public int getScreenHeight(int rows) {
        return super.getScreenHeight(rows) + Math.max(0, rows - MIN_DRAWN_ROWS) * CELL_STEP + endHeight;
    }

    public int getFirstCellY(int rows) {
        return getHeader().getSrcHeight() + rows * getRow().getSrcHeight() + getBottom().getSrcHeight() - CELL_STEP;
    }
}
