package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStencilPattern;
import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

@SuppressWarnings("unused")
public final class TFGItemsAsphalt {

    public static void init() {
    }

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> ASPHALT_MIX_BUCKET = TFGCore.REGISTRATE.item("asphalt_mix_bucket",
            p -> new BucketItem(TFGFluids.ASPHALT_MIX.getSource(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_LINES = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_lines", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.LINE))
            .properties(p -> p.stacksTo(16))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("item/asphalt_road/" + ctx.getName())))
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_CROSS = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_cross", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.CROSS))
            .properties(p -> p.stacksTo(16))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("item/asphalt_road/" + ctx.getName())))
            .register();

    public static final ItemEntry<RoadMarkingStencilItem> ASPHALT_ROAD_STENCIL_ARROW = TFGCore.REGISTRATE
            .item("asphalt_road_stencil_arrow", p -> new RoadMarkingStencilItem(p, AsphaltRoadStencilPattern.ARROW))
            .properties(p -> p.stacksTo(16))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("item/asphalt_road/" + ctx.getName())))
            .register();

}
