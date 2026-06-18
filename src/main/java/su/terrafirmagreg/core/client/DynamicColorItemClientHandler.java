package su.terrafirmagreg.core.client;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

/**
 * Handles dynamic color item rendering based on NBT data for items in the {@code #tfg:dynamic_color} tag.
 * NBT data should look like: {"dynamic_color": "#FF0000"} or {"dynamic_color": "0xFF0000"}.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DynamicColorItemClientHandler {

    private static final Map<String, Integer> COLOR_CACHE = new ConcurrentHashMap<>();

    /**
     * Retrieves the color value from cache, or decodes it.
     * @param hexColor The hex color string.
     */
    private static int getCachedDynamicColor(String hexColor) {
        return COLOR_CACHE.computeIfAbsent(hexColor, key -> {
            try {
                return Color.decode(key).getRGB() & 0xFFFFFF;
            } catch (Exception e) {
                return 0xFFFFFF;
            }
        });
    }

    /**
     * Registers the color provider for dynamic color items when nbt is updated.
     * @param event The tags updated event.
     */
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {

        ItemColor colorProvider = (stack, tintIndex) -> {
            // Only tint layer 1.
            if (tintIndex != 1) {
                return 0xFFFFFF;
            }

            // Check for the NBT tag "dynamic_color".
            if (stack.hasTag() && stack.getTag() != null) {
                if (stack.getTag().contains("dynamic_color")) {
                    String hexColor = stack.getTag().getString("dynamic_color");
                    return getCachedDynamicColor(hexColor);
                }
            }
            return 0xFFFFFF;
        };

        // Register the color provider for items in the #tfg:dynamic_color tag.
        var tag = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(TFGTags.Items.DYNAMIC_COLOR);
        tag.forEach(item -> Minecraft.getInstance().getItemColors().register(colorProvider, item));
    }
}
