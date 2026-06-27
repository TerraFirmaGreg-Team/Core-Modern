package su.terrafirmagreg.core.common.data;

import java.util.Locale;
import java.util.function.Supplier;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.palmtree.PalmTreeSaplingBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

/**
 * Enum of palm trees.
 * Palm trees only accept {@link Lifecycle}'s of FRUITING and DORMANT.
 * FRUITING will determine which months will produce fruit clusters from their "head" blocks.
 * This enum will automatically register Heads, Fruits, and Clusters
 */
public enum PalmTrees implements StringRepresentable {
    COCONUT(10, new Lifecycle[] {
            Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING,
            Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING
    });

    private final String serializedName;
    @Getter
    private final int defaultGrowthDays;
    @Getter
    private final Lifecycle[] stages;

    /**
     * Creates a new palm tree.
     *
     * @param defaultGrowthDays Default number of days required for growth.
     * @param stages             Lifecycle stages.
     */
    PalmTrees(int defaultGrowthDays, Lifecycle[] stages) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.defaultGrowthDays = defaultGrowthDays;
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

    public Integer daysToGrow() {
        return defaultGrowthDays;
    }

    public Block createSapling() {
        return new PalmTreeSaplingBlock(
                ExtendedProperties.of(MapColor.PLANT).noCollission().randomTicks().strength(0).sound(SoundType.GRASS).blockEntity(TFCBlockEntities.TICK_COUNTER).flammableLikeLeaves(),
                TFGBlocks_PalmTrees.GROWING_PALM_HEADS.get(this), this::daysToGrow, this.getClimateRange(), stages);
    }

    public Block createPottedSapling() {
        return new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, TFGBlocks_PalmTrees.PALM_SAPLINGS.get(this), BlockBehaviour.Properties.copy(Blocks.POTTED_ACACIA_SAPLING));
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
