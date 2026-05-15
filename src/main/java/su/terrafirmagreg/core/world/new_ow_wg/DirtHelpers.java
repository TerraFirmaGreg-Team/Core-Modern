package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;

public final class DirtHelpers {

    public static Block getBlock(SoilBlockType type, TFGSoilVariant variant) {
        switch (variant) {
            case ANDISOL -> {
                return TFCBlocks.SOIL.get(type).get(SoilBlockType.Variant.SILTY_LOAM).get();
            }
            case ARIDISOL -> {
                return TFCBlocks.SOIL.get(type).get(SoilBlockType.Variant.SANDY_LOAM).get();
            }
            case ENTISOL -> {
                return TFCBlocks.SOIL.get(type).get(SoilBlockType.Variant.LOAM).get();
            }
            case FLUVISOL -> {
                return TFCBlocks.SOIL.get(type).get(SoilBlockType.Variant.SILT).get();
            }
            case ALFISOL -> {
                switch (type) {
                    case DIRT -> {
                        return TFGBlocks_Earth.ALFISOL_DIRT.get();
                    }
                    case GRASS -> {
                        return TFGBlocks_Earth.ALFISOL_GRASS.get();
                    }
                    case MUD -> {
                        return TFGBlocks_Earth.ALFISOL_MUD.get();
                    }
                    default -> {
                        TFGCore.LOGGER.error("Tried to get a block that may not exist - Type: {}, Variant: ALFISOL", type);
                        return Blocks.MAGENTA_CONCRETE;
                    }
                }
            }
            case MOLLISOL -> {
                switch (type) {
                    case DIRT -> {
                        return TFGBlocks_Earth.MOLLISOL_DIRT.get();
                    }
                    case GRASS -> {
                        return TFGBlocks_Earth.MOLLISOL_GRASS.get();
                    }
                    case MUD -> {
                        return TFGBlocks_Earth.MOLLISOL_MUD.get();
                    }
                    default -> {
                        TFGCore.LOGGER.error("Tried to get a block that may not exist - Type: {}, Variant: MOLLISOL", type);
                        return Blocks.MAGENTA_CONCRETE;
                    }
                }
            }
            case OXISOL -> {
                switch (type) {
                    case DIRT -> {
                        return TFGBlocks_Earth.OXISOL_DIRT.get();
                    }
                    case GRASS -> {
                        return TFGBlocks_Earth.OXISOL_GRASS.get();
                    }
                    case MUD -> {
                        return TFGBlocks_Earth.OXISOL_MUD.get();
                    }
                    default -> {
                        TFGCore.LOGGER.error("Tried to get a block that may not exist - Type: {}, Variant: OXISOL", type);
                        return Blocks.MAGENTA_CONCRETE;
                    }
                }
            }
            case PODZOL -> {
                switch (type) {
                    case DIRT -> {
                        return TFGBlocks_Earth.PODZOL_DIRT.get();
                    }
                    case GRASS -> {
                        return TFGBlocks_Earth.PODZOL_GRASS.get();
                    }
                    case MUD -> {
                        return TFGBlocks_Earth.PODZOL_MUD.get();
                    }
                    default -> {
                        TFGCore.LOGGER.error("Tried to get a block that may not exist - Type: {}, Variant: PODZOL", type);
                        return Blocks.MAGENTA_CONCRETE;
                    }
                }
            }
        }

        TFGCore.LOGGER.error("Tried to get a block that may not exist - Type: {}, Variant: {}", type, variant);
        return Blocks.MAGENTA_CONCRETE;
    }
}
