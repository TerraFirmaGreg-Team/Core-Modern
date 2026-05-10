package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.item.Item;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStencilPattern;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@SuppressWarnings("unused")
public final class TFGItemsAsphalt {

    public static void init() {
    }

    public static final ItemEntry<Item> ASPHALT_RUBBLE = TFGCore.REGISTRATE.item("asphalt_rubble", Item::new)
            .properties(p -> p.stacksTo(32))
            .defaultModel()
            .register();

    public static final ItemEntry<Item> ASPHALT_BINDER = TFGCore.REGISTRATE.item("asphalt_binder", Item::new)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<Item> TAR_CHUNK = TFGCore.REGISTRATE.item("tar_chunk", Item::new)
            .properties(p -> p.stacksTo(32))
            .defaultModel()
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_LINES = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_lines", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.LINE))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_CROSS = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_cross", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.CROSS))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_ARROW = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_arrow", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.ARROW))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

}
