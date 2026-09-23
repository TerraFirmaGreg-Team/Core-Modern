package su.terrafirmagreg.core.api.pattern;

import java.util.LinkedHashMap;
import java.util.Map;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.world.level.block.Block;

import appeng.core.definitions.AEBlocks;
import net.minecraft.world.level.block.state.BlockState;

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

    public static int getTierForBlock(Block block) {
        Integer tier = getBuddingTiers().get(block);
        return tier != null ? tier : -1;
    }

    public static Block getBuddingBlockForTier(int tier) {
        for (var entry : getBuddingTiers().entrySet()) {
            if (entry.getValue() == tier)
                return entry.getKey();
        }
        return AEBlocks.QUARTZ_BLOCK.block();
    }

    public static boolean isBudding(BlockState state) {
        var block = state.getBlock();
        return buddingTiers.containsKey(block);
    }

    public static BasePredicate buddingBlocks() {
        return new PredicateBuilder("budding")
                .predicate(ctx -> {
                    var state = ctx.state();
                    return getBuddingTiers().entrySet().stream().anyMatch(v -> state.is(v.getKey()));
                })
                .candidates(getBuddingTiers().keySet().stream()
                        .map(b -> BlockInfo.fromBlockState(b.defaultBlockState())))
                .build();
    }
}
