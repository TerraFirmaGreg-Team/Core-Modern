package su.terrafirmagreg.core.client.util;

import java.util.HashMap;
import java.util.Map;

import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeSaplingBlock;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import su.terrafirmagreg.core.common.block.palmtree.PalmTreeSaplingBlock;

/**
 * Utility class for caching sapling growth days and providing tooltip color based on growth days.
 */
@SuppressWarnings("deprecation")
public class SaplingGrowthCache {
    private static final Map<Block, Integer> CACHE = new HashMap<>();
    private static int minBound = 0;
    private static int maxBound = 0;
    private static boolean initialized = false;

    /**
     * Initializes the sapling growth cache if it hasn't been happened yet.
     */
    public static void initializeIfNeeded() {
        if (initialized)
            return;

        int overallMin = Integer.MAX_VALUE;
        int overallMax = Integer.MIN_VALUE;

        // Cache sapling min and max growth days.
        for (Block block : BuiltInRegistries.BLOCK) {
            int days = -1;
            if (block instanceof TFCSaplingBlock b)
                days = b.getDaysToGrow();
            else if (block instanceof FruitTreeSaplingBlock b)
                days = b.getTreeGrowthDays();
            else if (block instanceof PalmTreeSaplingBlock b)
                days = b.getTreeGrowthDays();

            if (days > 0) {
                CACHE.put(block, days);
                if (days < overallMin)
                    overallMin = days;
                if (days > overallMax)
                    overallMax = days;
            }
        }

        if (!CACHE.isEmpty()) {
            minBound = overallMin;
            maxBound = overallMax;
        }
        initialized = true;
    }

    /**
     * Invalidates the sapling growth cache when needed.
     */
    public static void invalidate() {
        CACHE.clear();
        minBound = 0;
        maxBound = 0;
        initialized = false;
    }

    /**
     * Resets the sapling growth cache during resource reloads.
     */
    @SubscribeEvent
    public static void onResourceReload(AddReloadListenerEvent event) {
        SaplingGrowthCache.invalidate();
    }

    /**
     * Returns sapling growth color based on its growth days compared to the average.
     */
    public static ChatFormatting getGrowthColor(Block block) {
        initializeIfNeeded();
        if (!CACHE.containsKey(block) || minBound == maxBound) {
            return ChatFormatting.GOLD;
        }

        int days = CACHE.get(block);
        double range = maxBound - minBound;
        double dynamicThird = range / 3.0;

        if (days <= minBound + dynamicThird) {
            return ChatFormatting.DARK_GREEN;
        } else if (days <= minBound + (dynamicThird * 2)) {
            return ChatFormatting.GOLD;
        } else {
            return ChatFormatting.DARK_RED;
        }
    }
}
