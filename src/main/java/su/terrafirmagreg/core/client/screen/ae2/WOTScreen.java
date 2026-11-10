package su.terrafirmagreg.core.client.screen.ae2;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.BackgroundPanel;
import appeng.menu.SlotSemantics;
import de.mari_023.ae2wtlib.terminal.ScrollingUpgradesPanel;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wut.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.wut.TerminalSelectionButton;

import su.terrafirmagreg.core.compat.ae2.terminal.OrderTerminalMenu;
import su.terrafirmagreg.core.compat.ae2.terminal.WOTMenu;

public class WOTScreen extends OrderTerminalScreen implements IUniversalTerminalCapable {
    private static final int UPGRADES_STAGING_GAP = 2;

    public WOTScreen(OrderTerminalMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        if (((WOTMenu) menu).isWUT()) {
            addToLeftToolbar(new TerminalSelectionButton((WTMenuHost) getMenu().getHost(), this::storeState));
        }
        widgets.add("scrollingUpgrades", new ScrollingUpgradesPanel(getMenu().getSlots(SlotSemantics.UPGRADE),
                getMenu().getHost(), widgets) {
            @Override
            public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
                // stop above the staging panel (top at cellsTop() - 1), measured from where the style placed this panel
                setSize(0, cellsTop() - 1 - UPGRADES_STAGING_GAP - getBounds().getY());
                super.populateScreen(addWidget, bounds, screen);
            }
        });
        widgets.add("singularityBackground", new BackgroundPanel(style.getImage("singularityBackground")));
    }
}
