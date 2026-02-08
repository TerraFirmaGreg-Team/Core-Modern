package su.terrafirmagreg.core.compat.starcatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for handling Starcatcher fish delegation for `mob_type` extracting items.
 */
public class StarcatcherFishVariants {

    public record FishVariant(String fishItemName, int baseColor, int overlayColor) {
    }

    /**
     * Cached fish info.
     */
    public record CachedFishInfo(String mobType, boolean isStarcatcherFish, String fishName) {
    }

    private static final Map<String, FishVariant> FISH_VARIANTS = new HashMap<>();
    private static final Map<Integer, CachedFishInfo> FISH_INFO_CACHE = new ConcurrentHashMap<>();

    private static CachedFishInfo getCachedFishInfo(ItemStack stack) {
        if (!stack.hasTag()) {
            return new CachedFishInfo("", false, null);
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("mob_type")) {
            return new CachedFishInfo("", false, null);
        }

        int nbtHash = tag.hashCode();
        return FISH_INFO_CACHE.computeIfAbsent(nbtHash, key -> {
            String mobType = tag.getString("mob_type");
            boolean isStarcatcher = FISH_VARIANTS.containsKey(mobType);
            String fishName = null;

            if (isStarcatcher && mobType.startsWith("starcatcher:")) {
                fishName = mobType.substring("starcatcher:".length());
            }

            return new CachedFishInfo(mobType, isStarcatcher, fishName);
        });
    }

    public static void initializeFishVariants() {
        addFishVariant("starcatcher:obidontiee", 0x4682B4, 0xC0C0C0);
        addFishVariant("starcatcher:silverveil_perch", 0xC0C0C0, 0x4169E1);
        addFishVariant("starcatcher:elderscale", 0x696969, 0xFFD700);
        addFishVariant("starcatcher:driftfin", 0x87CEEB, 0xFFFFFF);
        addFishVariant("starcatcher:twilight_koi", 0x483D8B, 0xFF69B4);
        addFishVariant("starcatcher:thunder_bass", 0x2F4F4F, 0xFFFF00);
        addFishVariant("starcatcher:lightning_bass", 0x191970, 0x00FFFF);
        addFishVariant("starcatcher:sludge_catfish", 0x556B2F, 0x8B4513);
        addFishVariant("starcatcher:lily_snapper", 0x228B22, 0xFFB6C1);
        addFishVariant("starcatcher:sage_catfish", 0x9ACD32, 0x8FBC8F);
        addFishVariant("starcatcher:pale_carp", 0xF5F5DC, 0xD3D3D3);
        addFishVariant("starcatcher:pale_pinfish", 0xFFF8DC, 0xFFC0CB);
        addFishVariant("starcatcher:pinfish", 0xFFB6C1, 0xFF1493);
        addFishVariant("starcatcher:frostjaw_trout", 0xB0E0E6, 0xFFFFFF);
        addFishVariant("starcatcher:crystalback_trout", 0xE0FFFF, 0x00CED1);
        addFishVariant("starcatcher:aurora", 0x7FFFD4, 0xFF69B4);
        addFishVariant("starcatcher:wintery_pike", 0xF0F8FF, 0x4682B4);
        addFishVariant("starcatcher:sandtail", 0xF4A460, 0xDEB887);
        addFishVariant("starcatcher:mirage_carp", 0xFFE4B5, 0xFF6347);
        addFishVariant("starcatcher:scorchfish", 0xFF4500, 0xFFD700);
        addFishVariant("starcatcher:cactifish", 0x228B22, 0xFF69B4);
        addFishVariant("starcatcher:agave_bream", 0x9ACD32, 0x32CD32);
        addFishVariant("starcatcher:sunny_sturgeon", 0xFFD700, 0xFF8C00);
        addFishVariant("starcatcher:rockgill", 0x696969, 0x2F4F4F);
        addFishVariant("starcatcher:peakdweller", 0x778899, 0xFFFFFF);
        addFishVariant("starcatcher:sun_seeking_carp", 0xFFA500, 0xFFFF00);
        addFishVariant("starcatcher:blossomfish", 0xFFB6C1, 0xFF69B4);
        addFishVariant("starcatcher:petaldrift_carp", 0xFFC0CB, 0xFFB6C1);
        addFishVariant("starcatcher:pink_koi", 0xFF69B4, 0xFFFFFF);
        addFishVariant("starcatcher:morganite", 0xE6E6FA, 0xFF69B4);
        addFishVariant("starcatcher:rose_siamese_fish", 0xFF1493, 0x8B0000);
        addFishVariant("starcatcher:vesani", 0xDDA0DD, 0xFF69B4);
        addFishVariant("starcatcher:crystalback_sturgeon", 0xE0FFFF, 0xB0E0E6);
        addFishVariant("starcatcher:icetooth_sturgeon", 0xF0F8FF, 0x00CED1);
        addFishVariant("starcatcher:boreal", 0x4682B4, 0xFFFFFF);
        addFishVariant("starcatcher:crystalback_boreal", 0x87CEEB, 0x00CED1);
        addFishVariant("starcatcher:silverfin_pike", 0xC0C0C0, 0x708090);
        addFishVariant("starcatcher:carpenjoe", 0xDAA520, 0x8B4513);
        addFishVariant("starcatcher:willow_bream", 0x9ACD32, 0x556B2F);
        addFishVariant("starcatcher:drifting_bream", 0x87CEEB, 0x4682B4);
        addFishVariant("starcatcher:downfall_bream", 0x2F4F4F, 0x87CEEB);
        addFishVariant("starcatcher:hollowbelly_darter", 0xF5F5DC, 0x8B4513);
        addFishVariant("starcatcher:mistback_chub", 0xD3D3D3, 0x696969);
        addFishVariant("starcatcher:bluegigi", 0x0000FF, 0x87CEEB);
        addFishVariant("starcatcher:frostgill_chub", 0xB0E0E6, 0xE0FFFF);
        addFishVariant("starcatcher:crystalback_minnow", 0xE0FFFF, 0x00CED1);
        addFishVariant("starcatcher:azure_crystalback_minnow", 0xF0FFFF, 0x0000FF);
        addFishVariant("starcatcher:blue_crystal_fin", 0x0000FF, 0x00CED1);
        addFishVariant("starcatcher:ironjaw_herring", 0x708090, 0x2F4F4F);
        addFishVariant("starcatcher:deepjaw_herring", 0x2F4F4F, 0x000080);
        addFishVariant("starcatcher:dusktail_snapper", 0x483D8B, 0xFF4500);
        addFishVariant("starcatcher:joel", 0x32CD32, 0xFF69B4);
        addFishVariant("starcatcher:redscaled_tuna", 0xFF0000, 0xDC143C);
        addFishVariant("starcatcher:bigeye_tuna", 0x4682B4, 0x000080);
        addFishVariant("starcatcher:sea_bass", 0x2F4F4F, 0x4682B4);
        addFishVariant("starcatcher:shroomfish", 0xFF0000, 0xFFFFFF);
        addFishVariant("starcatcher:sporefish", 0x8B4513, 0xF5DEB3);
        addFishVariant("starcatcher:gold_fan", 0xFFD700, 0xFFA500);
        addFishVariant("starcatcher:geode_eel", 0x663399, 0x9966CC);
        addFishVariant("starcatcher:whiteveil", 0xFFFFFF, 0xD3D3D3);
        addFishVariant("starcatcher:black_eel", 0x000000, 0x2F4F4F);
        addFishVariant("starcatcher:amethystback", 0x9966CC, 0x663399);
        addFishVariant("starcatcher:stonefish", 0x696969, 0x2F4F4F);
        addFishVariant("starcatcher:fossilized_angelfish", 0xF5DEB3, 0xD2B48C);
        addFishVariant("starcatcher:dripfin", 0xD2B48C, 0x8B7355);
        addFishVariant("starcatcher:yellowstone_fish", 0xFFFF00, 0xD2B48C);
        addFishVariant("starcatcher:lush_pike", 0x228B22, 0x32CD32);
        addFishVariant("starcatcher:vivid_moss", 0x9ACD32, 0x228B22);
        addFishVariant("starcatcher:the_quarrish", 0x556B2F, 0x9ACD32);
        addFishVariant("starcatcher:ghostly_pike", 0xF5F5F5, 0xD3D3D3);
        addFishVariant("starcatcher:aquamarine_pike", 0x7FFFD4, 0x00CED1);
        addFishVariant("starcatcher:garnet_mackerel", 0x8B0000, 0xDC143C);
        addFishVariant("starcatcher:bright_amethyst_snapper", 0xDA70D6, 0x9370DB);
        addFishVariant("starcatcher:dark_amethyst_snapper", 0x663399, 0x4B0082);
        addFishVariant("starcatcher:deepslatefish", 0x2F4F4F, 0x000000);
        addFishVariant("starcatcher:sculkfish", 0x001a1a, 0x00CED1);
        addFishVariant("starcatcher:ward", 0x008080, 0x40E0D0);
        addFishVariant("starcatcher:glowing_dark", 0x000080, 0x00FFFF);
        addFishVariant("starcatcher:suneater", 0xFFD700, 0xFF4500);
        addFishVariant("starcatcher:pyrotrout", 0xFF6347, 0xFF0000);
        addFishVariant("starcatcher:obsidian_eel", 0x000000, 0xFF4500);
        addFishVariant("starcatcher:molten_shrimp", 0xFF4500, 0xFFD700);
        addFishVariant("starcatcher:obsidian_crab", 0x000000, 0x8B0000);
        addFishVariant("starcatcher:scorched_bloodsucker", 0x8B0000, 0xFF0000);
        addFishVariant("starcatcher:molten_deepslate_crab", 0x2F4F4F, 0xFF4500);
        addFishVariant("starcatcher:embergill", 0xFF4500, 0xFFD700);
        addFishVariant("starcatcher:scalding_pike", 0xFF0000, 0xFF4500);
        addFishVariant("starcatcher:cinder_squid", 0x8B0000, 0xFF6347);
        addFishVariant("starcatcher:lava_crab", 0xFF4500, 0xFFD700);
        addFishVariant("starcatcher:magma_fish", 0xFF6347, 0xFF4500);
        addFishVariant("starcatcher:glowstone_seeker", 0xFFD700, 0xFFFF00);
        addFishVariant("starcatcher:glowstone_pufferfish", 0xFFFF00, 0xFFD700);
        addFishVariant("starcatcher:willish", 0x000080, 0x00FFFF);
        addFishVariant("starcatcher:cerberay", 0x8B0000, 0xFF0000);
        addFishVariant("starcatcher:charfish", 0x2F2F2F, 0xFFFFFF);
        addFishVariant("starcatcher:chorus_crab", 0x9966CC, 0xDA70D6);
        addFishVariant("starcatcher:end_glow", 0x483D8B, 0x9370DB);
        addFishVariant("starcatcher:voidbiter", 0x000000, 0x483D8B);
    }

    private static void addFishVariant(String fishName, int baseColor, int overlayColor) {
        FISH_VARIANTS.put(fishName, new FishVariant(fishName, baseColor, overlayColor));
    }

    public static FishVariant getFishVariant(String fishName) {
        return FISH_VARIANTS.get(fishName);
    }

    /**
     * Check if an item contains a Starcatcher fish mob type nbt.
     */
    public static boolean isStarcatcherFish(ItemStack stack) {
        return getCachedFishInfo(stack).isStarcatcherFish;
    }

    /**
     * Get the colors for a Starcatcher fish.
     * @param stack The ItemStack to check.
     * @param tintIndex The color layer index.
     * @return The color for this layer.
     */
    public static int getStarcatcherFishColor(ItemStack stack, int tintIndex) {
        CachedFishInfo fishInfo = getCachedFishInfo(stack);
        if (!fishInfo.isStarcatcherFish) {
            return 0xFFFFFF;
        }

        FishVariant variant = getFishVariant(fishInfo.mobType);
        if (variant != null) {
            return (tintIndex == 1) ? variant.baseColor : variant.overlayColor;
        }

        return 0xFFFFFF;
    }

    /**
     * Get the fish name for display in tooltips.
     * @param stack The ItemStack to check.
     * @return The fish name for translation.
     */
    public static String getFishName(ItemStack stack) {
        return getCachedFishInfo(stack).fishName;
    }
}
