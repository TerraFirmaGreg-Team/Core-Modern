package su.terrafirmagreg.core.common.data;

import java.util.Locale;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.palmtree.PalmClusterBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmTreeSaplingBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

/**
 * Enum of palm trees.
 * Palm trees only accept {@link Lifecycle}'s of FRUITING and DORMANT.
 * FRUITING will determine which months will produce fruit clusters from their "head" blocks.
 * This enum will automatically register Heads, Fruits, and Clusters
 */
public enum PalmTrees implements StringRepresentable {
    COCONUT(10, 150, 3, 6, 0, 0, 8, "square", true, true, new Lifecycle[] {
            Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING,
            Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING
    }),
    OIL_PALM(7, 200, 0, 2, 3, 6, 4, "double_bundle", false, false, new Lifecycle[] {
            Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING,
            Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING
    }),
    DATE(9, 255, 2, 5, 4, 8, 4, "bundle", false, false, new Lifecycle[] {
            Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT,
            Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING
    }),
    ACAI(10, 255, 1, 4, 4, 8, 5, "string", false, false, new Lifecycle[] {
            Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT,
            Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT
    });

    private final String serializedName;
    @Getter
    private final int defaultGrowthDays;
    @Getter
    private final int foliageColorIndex;
    @Getter
    private final int minGrowthSize;
    @Getter
    private final int maxGrowthSize;
    @Getter
    private final int minDrops;
    @Getter
    private final int maxDrops;
    @Getter
    private final int clusterAges;
    @Getter
    private final String clusterModelShape;
    @Getter
    private final boolean specialCluster;
    @Getter
    private final boolean specialFruit;
    @Getter
    private final Lifecycle[] stages;

    @Getter
    private final IntegerProperty clusterAgeProperty;

    /**
     * Creates a new palm tree.
     *
     * @param defaultGrowthDays Default number of days required for growth.
     * @param foliageColorIndex Sets the color of the leaves based on TFC foliage index (foliage.png). Range from 0 to 255
     * @param minGrowthSize Sets the minimum number of stage 2 trunk blocks for the final growth tree size. (5 blocks will always be placed below)
     * @param maxGrowthSize Sets the maximum number of stage 2 trunk blocks for the final growth tree size. (5 blocks will always be placed below)
     * @param minDrops Sets the minimum number of fruit drops when mature.
     * @param maxDrops Sets the maximum number of fruit drops when mature.
     * @param clusterAges Sets the number of age states for the cluster block.
     * @param clusterModelShape Sets the model shape for the cluster block. Available options: "square", "bundle", "double_bundle", "string"
     * @param specialCluster If false, the cluster block will be automatically generated with {@link PalmClusterBlock}. If true, a dedicated class should be made.
     * @param specialFruit If false, the fruit will be automatically generated as an item. If true, a dedicated class should be made.
     * @param stages Lifecycle stages. Only valid stages are {@link Lifecycle#FRUITING} and {@link Lifecycle#DORMANT}.
     */
    PalmTrees(int defaultGrowthDays, int foliageColorIndex, int minGrowthSize, int maxGrowthSize, int minDrops, int maxDrops, Integer clusterAges, String clusterModelShape, boolean specialCluster,
            boolean specialFruit,
            Lifecycle[] stages) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.defaultGrowthDays = defaultGrowthDays;
        this.foliageColorIndex = foliageColorIndex;
        this.minGrowthSize = minGrowthSize;
        this.maxGrowthSize = maxGrowthSize;
        this.minDrops = minDrops;
        this.maxDrops = maxDrops;
        this.specialCluster = specialCluster;
        this.specialFruit = specialFruit;
        this.clusterAges = clusterAges;
        this.clusterModelShape = clusterModelShape;
        this.stages = stages;
        this.clusterAgeProperty = IntegerProperty.create("age", 0, clusterAges - 1);
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

    public @Nullable ItemEntry<? extends Item> getDroppedFruit() {
        if (!this.isSpecialFruit()) {
            return TFGBlocks_PalmTrees.PALM_FRUITS.get(this);
        }
        return null;
    }
}
