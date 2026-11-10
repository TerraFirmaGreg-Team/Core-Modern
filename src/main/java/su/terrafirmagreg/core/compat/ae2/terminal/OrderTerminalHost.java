package su.terrafirmagreg.core.compat.ae2.terminal;

import appeng.api.storage.ITerminalHost;

public interface OrderTerminalHost extends ITerminalHost {
    OrderTerminalLogic getOrderLogic();
}
