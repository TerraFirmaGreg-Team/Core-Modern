package su.terrafirmagreg.core.common.data;

import java.util.Locale;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.util.calendar.Month;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

public enum PalmTrees implements StringRepresentable {
    COCONUT(Month.OCTOBER, Month.DECEMBER);

    private final String serializedName;
    @Getter
    private final Month startMonth;
    @Getter
    private final Month endMonth;

    PalmTrees(Month startMonth, Month endMonth) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.startMonth = startMonth;
        this.endMonth = endMonth;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public BlockEntry<? extends Block> getFruitClusterBlock() {
        return TFGBlocks_PalmTrees.PALM_CLUSTERS.get(this);
    }

    public BlockEntry<? extends Block> getDroppedFruitBlock() {
        return TFGBlocks_PalmTrees.PALM_FRUITS.get(this);
    }
}
