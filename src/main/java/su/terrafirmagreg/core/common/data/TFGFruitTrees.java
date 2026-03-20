package su.terrafirmagreg.core.common.data;

import java.awt.Color;
import java.util.Locale;

import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.food.FoodProperties;

import lombok.Getter;

/**
 * Custom fruit trees added by TFG.
 * Add new entries here to create additional fruit trees with full datagen support.
 * Then you need to add climate data in Kubejs, and finally textures in the core asset location.
 */
@Getter
public enum TFGFruitTrees implements StringRepresentable {

    LAVACADO(
            10,
            new Lifecycle[] {
                    Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.HEALTHY,
                    Lifecycle.HEALTHY, Lifecycle.FLOWERING, Lifecycle.FLOWERING, Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT
            },
            new Color(255, 100, 50).getRGB());

    public static final FoodProperties FRUIT_FOOD = new FoodProperties.Builder().nutrition(4).saturationMod(0.3F).build();

    private final String serializedName;
    private final int defaultGrowthDays;
    private final Lifecycle[] stages;
    private final int floweringLeavesColor;

    /**
     * Constructor for {@link TFGFruitTrees}.
     *
     * @param defaultGrowthDays Default number of days required for growth.
     * @param stages Lifecycle stages is a 12-month cycle. Jan - Dec.
     * @param floweringLeavesColor RGB color of the leaf particles.
     */
    TFGFruitTrees(int defaultGrowthDays, Lifecycle[] stages, int floweringLeavesColor) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.defaultGrowthDays = defaultGrowthDays;
        this.stages = stages;
        this.floweringLeavesColor = floweringLeavesColor;
    }

}
