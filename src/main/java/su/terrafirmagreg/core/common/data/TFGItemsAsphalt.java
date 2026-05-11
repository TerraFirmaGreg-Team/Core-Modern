package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.Item;

import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings("unused")
public final class TFGItemsAsphalt {

    public static void init() {
    }

    public static final ItemEntry<Item> ASPHALT_RUBBLE = TFGCore.REGISTRATE.item("asphalt_rubble", Item::new)
            .properties(p -> p.stacksTo(32))
            .defaultModel()
            .register();

}
