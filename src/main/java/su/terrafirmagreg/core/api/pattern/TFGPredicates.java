package su.terrafirmagreg.core.api.pattern;

import java.util.LinkedHashMap;
import java.util.Map;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.world.level.block.Block;

import appeng.core.definitions.AEBlocks;

public class TFGPredicates {

    private static Map<Block, Integer> buddingTiers;

    private static Map<Block, Integer> getBuddingTiers() {
        if (buddingTiers == null) {
            buddingTiers = new LinkedHashMap<>();
            buddingTiers.put(AEBlocks.QUARTZ_BLOCK.block(), 0);
            buddingTiers.put(AEBlocks.DAMAGED_BUDDING_QUARTZ.block(), 1);
            buddingTiers.put(AEBlocks.CHIPPED_BUDDING_QUARTZ.block(), 2);
            buddingTiers.put(AEBlocks.FLAWED_BUDDING_QUARTZ.block(), 3);
            buddingTiers.put(AEBlocks.FLAWLESS_BUDDING_QUARTZ.block(), 4);
        }
        return buddingTiers;
    }

    public static Block getBuddingBlockForTier(int tier) {
        for (var entry : getBuddingTiers().entrySet()) {
            if (entry.getValue() == tier)
                return entry.getKey();
        }
        return AEBlocks.QUARTZ_BLOCK.block();
    }

    public static TraceabilityPredicate buddingBlocks() {
        return new TraceabilityPredicate(blockWorldState -> {
            var state = blockWorldState.getBlockState();
            for (var entry : getBuddingTiers().entrySet()) {
                if (state.is(entry.getKey())) {
                    blockWorldState.getMatchContext().set("BuddingTier", entry.getValue());
                    blockWorldState.getMatchContext().set("BuddingPos", blockWorldState.getPos());
                    return true;
                }
            }
            return false;
        }, () -> getBuddingTiers().keySet().stream()
                .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                .toArray(BlockInfo[]::new));
    }
}
