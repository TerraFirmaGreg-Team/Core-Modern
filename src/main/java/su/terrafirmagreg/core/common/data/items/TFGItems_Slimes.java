package su.terrafirmagreg.core.common.data.items;

import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.Item;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

@SuppressWarnings("unused")
public class TFGItems_Slimes {
    public static void init() {

    }

    public static final ItemEntry<Item> PLANT_SLIME_BALL = TFGCore.REGISTRATE.item("slime/slime_ball/plant", Item::new)
            .defaultModel()
            .tag(TFGTags.Items.SLIME_BALL)
            .register();

    public static final ItemEntry<Item> GLOWBERRY_SLIME_BALL = TFGCore.REGISTRATE.item("slime/slime_ball/glowberry", Item::new)
            .defaultModel()
            .tag(TFGTags.Items.SLIME_BALL)
            .register();

    public static final ItemEntry<Item> LATEX_SLIME_BALL = TFGCore.REGISTRATE.item("slime/slime_ball/latex", Item::new)
            .defaultModel()
            .tag(TFGTags.Items.SLIME_BALL)
            .register();
}
