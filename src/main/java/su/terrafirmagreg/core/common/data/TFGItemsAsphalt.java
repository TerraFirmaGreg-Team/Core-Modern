package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStencilPattern;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@SuppressWarnings("unused")
public final class TFGItemsAsphalt {

    public static void init() {
    }

    /** Drops from breaking {@code tfg:asphalt_road*} blocks (not pouring). */
    public static final ItemEntry<Item> ASPHALT_RUBBLE = TFGCore.REGISTRATE.item("asphalt_rubble", Item::new)
            .properties(p -> p.stacksTo(64))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated"))
                    .texture("layer0", TFGCore.id("item/" + ctx.getName())))
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_LINES = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_lines", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.LINE))
            .properties(p -> p.stacksTo(16))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                    TFGCore.id("item/" + ctx.getName())))
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_CROSS = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_cross", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.CROSS))
            .properties(p -> p.stacksTo(16))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                    TFGCore.id("item/" + ctx.getName())))
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_ARROW = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_arrow", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.ARROW))
            .properties(p -> p.stacksTo(16))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                    TFGCore.id("item/" + ctx.getName())))
            .register();

}
