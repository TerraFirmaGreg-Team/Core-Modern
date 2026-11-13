package su.terrafirmagreg.core.api.pattern;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.predicates.PredicateBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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

    // Three Tiers of Substructures all using the same Pattern but can have different blocks though like
    // the EBF all needs to match for the multiblock to form. It's easier to do it that way that to write
    // 6 substructures but it could also be done. To see in the future.

    private static Map<Block, Integer> boilerModuleCasingTiers;
    private static Map<Block, Integer> boosterCoreTiers;
    private static Map<Block, Integer> catalystCoreTiers;

    private static Map<Block, Integer> getBoilerModuleCasingTiers() {
        if (boilerModuleCasingTiers == null) {
            boilerModuleCasingTiers = new LinkedHashMap<>();
            boilerModuleCasingTiers.put(GTBlocks.CASING_BRONZE_BRICKS.get(), 1);
            boilerModuleCasingTiers.put(GTBlocks.CASING_STEEL_SOLID.get(), 2);
            boilerModuleCasingTiers.put(GTBlocks.CASING_STAINLESS_CLEAN.get(), 3);
        }
        return boilerModuleCasingTiers;
    }

    private static Map<Block, Integer> getBoosterCoreTiers() {
        if (boosterCoreTiers == null) {
            boosterCoreTiers = new LinkedHashMap<>();
            boosterCoreTiers.put(GTBlocks.CASING_BRONZE_PIPE.get(), 1);
            boosterCoreTiers.put(GTBlocks.CASING_STEEL_PIPE.get(), 2);
            boosterCoreTiers.put(GTBlocks.CASING_TITANIUM_PIPE.get(), 3);
        }
        return boosterCoreTiers;
    }

    private static Map<Block, Integer> getCatalystCoreTiers() {
        if (catalystCoreTiers == null) {
            catalystCoreTiers = new LinkedHashMap<>();
            catalystCoreTiers.put(GTBlocks.CASING_BRONZE_GEARBOX.get(), 1);
            catalystCoreTiers.put(GTBlocks.CASING_STEEL_GEARBOX.get(), 2);
            catalystCoreTiers.put(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get(), 3);
        }
        return catalystCoreTiers;
    }

    public static int getBoilerModuleTier(Block block) {
        Integer tier = getBoilerModuleCasingTiers().get(block);
        if (tier == null)
            tier = getBoosterCoreTiers().get(block);
        if (tier == null)
            tier = getCatalystCoreTiers().get(block);
        return tier != null ? tier : -1;
    }

    public static Block getBoilerModuleCasingForTier(int tier) {
        for (var entry : getBoilerModuleCasingTiers().entrySet()) {
            if (entry.getValue() == tier)
                return entry.getKey();
        }
        return GTBlocks.CASING_BRONZE_BRICKS.get();
    }

    public static MultiPredicate boilerModuleCasings() {
        return Predicates.blocks("BoilerModuleCasings", List.copyOf(getBoilerModuleCasingTiers().keySet()));
    }

    public static MultiPredicate boosterCores() {
        return Predicates.blocks("BoilerBoosterCores", List.copyOf(getBoosterCoreTiers().keySet()));
    }

    public static MultiPredicate catalystCores() {
        return Predicates.blocks("BoilerCatalystCores", List.copyOf(getCatalystCoreTiers().keySet()));
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
