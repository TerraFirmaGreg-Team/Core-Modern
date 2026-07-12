package su.terrafirmagreg.core.utils;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

@Getter
public enum MineshaftPlaceableLamp {
    BRONZE(TFCBlocks.METALS.get(Metal.Default.BRONZE).get(Metal.BlockType.LAMP).get(), MineshaftHelpers.FALLEN_BRONZE_LAMP),
    BLACK_BRONZE(TFCBlocks.METALS.get(Metal.Default.BLACK_BRONZE).get(Metal.BlockType.LAMP).get(), MineshaftHelpers.FALLEN_BLACK_BRONZE_LAMP),
    BISMUTH_BRONZE(TFCBlocks.METALS.get(Metal.Default.BISMUTH_BRONZE).get(Metal.BlockType.LAMP).get(), MineshaftHelpers.FALLEN_BISMUTH_BRONZE_LAMP),
    WROUGHT_IRON(TFCBlocks.METALS.get(Metal.Default.WROUGHT_IRON).get(Metal.BlockType.LAMP).get(), MineshaftHelpers.FALLEN_WROUGHT_IRON_LAMP);

    private final Block hangingLamp;
    private final Block fallenLamp;

    MineshaftPlaceableLamp(Block hangingLamp, Block fallenLamp) {
        this.hangingLamp = hangingLamp;
        this.fallenLamp = fallenLamp;
    }

}
