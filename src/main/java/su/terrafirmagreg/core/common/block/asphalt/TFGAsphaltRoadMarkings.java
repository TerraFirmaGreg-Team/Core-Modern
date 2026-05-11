package su.terrafirmagreg.core.common.block.asphalt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class TFGAsphaltRoadMarkings {
    private static final Map<ResourceLocation, AsphaltRoadMarkingMask> STENCIL_MASKS = new HashMap<>();

    private TFGAsphaltRoadMarkings() {
    }

    public static void registerStencil(String itemId, String maskId) {
        ResourceLocation item = ResourceLocation.tryParse(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Invalid asphalt road stencil item id: " + itemId);
        }

        AsphaltRoadMarkingMask mask = AsphaltRoadMarkingMask.fromSerializedName(maskId);
        if (mask.isNone()) {
            throw new IllegalArgumentException("Asphalt road stencil cannot use mask=none");
        }

        STENCIL_MASKS.put(item, mask);
    }

    public static Optional<AsphaltRoadMarkingMask> maskForStencil(ItemStack stack) {
        ResourceLocation item = itemId(stack);
        if (item == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(STENCIL_MASKS.get(item));
    }

    @Nullable
    private static ResourceLocation itemId(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }
}
