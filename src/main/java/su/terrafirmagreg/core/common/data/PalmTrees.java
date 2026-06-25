package su.terrafirmagreg.core.common.data;

import java.util.Locale;
import java.util.function.Supplier;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

/**
 * Enum of palm trees.
 * Palm trees only accept {@link Lifecycle}'s of FRUITING and DORMANT.
 * FRUITING will determine which months will produce fruit clusters from their "head" blocks.
 * This enum will automatically register Heads, Fruits, and Clusters
 */
public enum PalmTrees implements StringRepresentable {
    COCONUT(new Lifecycle[] {
            Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING,
            Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING
    });

    private final String serializedName;
    @Getter
    private final Lifecycle[] stages;

    /**
     * Creates a new palm tree.
     * @param stages Lifecycle stages.
     */
    PalmTrees(Lifecycle[] stages) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.stages = stages;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static void init() {
        for (PalmTrees tree : values()) {
            tree.getClimateRange();
        }
    }

    public Supplier<ClimateRange> getClimateRange() {
        ResourceLocation id = TFGCore.id("palm_tree/" + serializedName);
        var entry = ClimateRange.MANAGER.register(id);
        return () -> {
            try {
                return entry.get();
            } catch (Exception e) {
                return null;
            }
        };
    }

    public BlockEntry<? extends Block> getFruitClusterBlock() {
        return TFGBlocks_PalmTrees.PALM_CLUSTERS.get(this);
    }

    public BlockEntry<? extends Block> getDroppedFruitBlock() {
        return TFGBlocks_PalmTrees.PALM_FRUITS.get(this);
    }
}
