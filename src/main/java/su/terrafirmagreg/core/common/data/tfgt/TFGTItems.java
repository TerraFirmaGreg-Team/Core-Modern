package su.terrafirmagreg.core.common.data.tfgt;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import su.terrafirmagreg.core.TFGCore;

public class TFGTItems {
    public static void init() {

    }

    public static ItemEntry<Item> ITEM_RAILGUN_SHELL = TFGCore.REGISTRATE.item("item_railgun_shell", (p) -> {
        p.stacksTo(4);
        return new Item(p);
    }).register();
    public static ItemEntry<Item> SPENT_RAILGUN_SHELL = TFGCore.REGISTRATE.item("spent_railgun_shell", (p) -> {
        p.stacksTo(4);
        return new Item(p);
    }).register();
}
