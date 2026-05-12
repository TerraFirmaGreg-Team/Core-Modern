package su.terrafirmagreg.core.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingMask;

@SuppressWarnings("unused")
public final class TFGItemsAsphalt {

    private static final Map<ResourceLocation, AsphaltRoadMarkingMask> STENCIL_MASKS = new HashMap<>();

    public static void init() {
        // register all road marking stencils
        for (AsphaltRoadMarkingMask mask : AsphaltRoadMarkingMask.values()) {
            if (mask.isNone()) {
                continue;
            }

            String stencilItemName = "asphalt_road_stencil_" + mask.getSerializedName();

            TFGCore.REGISTRATE.item(stencilItemName, Item::new)
                    .tag(TFGTags.Items.ROAD_MARKING_STENCILS)
                    .defaultModel()
                    .register();

            STENCIL_MASKS.put(TFGCore.id(stencilItemName), mask);
        }
    }

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> ASPHALT_MIX_BUCKET = TFGCore.REGISTRATE.item("asphalt_mix_bucket",
            p -> new BucketItem(TFGFluids.ASPHALT_MIX.getSource(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .defaultModel()
            .register();

    public static Optional<AsphaltRoadMarkingMask> maskForStencil(ItemStack stack) {
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (item == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(STENCIL_MASKS.get(item));
    }
}
