package su.terrafirmagreg.core.common.blockentity;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blockentities.BerryBushBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

/**
 * A berry bush block entity that uses TFG's own block entity type instead of TFC's.
 */
public class TFGBerryBushBlockEntity extends BerryBushBlockEntity {

    /**
     * Constructs a new {@link TFGBerryBushBlockEntity}.
     *
     * @param pos The position of the block entity.
     * @param state The block state of the block entity.
     */
    public TFGBerryBushBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.FRUIT_TREE_BERRY_BUSH.get(), pos, state);
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return TFGBlockEntities.FRUIT_TREE_BERRY_BUSH.get();
    }
}
