package su.terrafirmagreg.core.mixins.common.firmalife;

import com.eerussianguy.firmalife.common.blocks.greenhouse.GreenhousePortBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import su.terrafirmagreg.core.common.data.blockentity.GTGreenhousePortBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

@Mixin(GreenhousePortBlock.class)
public abstract class MixinGreenhousePortBlock implements EntityBlock {

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GTGreenhousePortBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TFGBlockEntities.GT_GREENHOUSE_PORT.get()
                ? (lvl, pos, st, be) -> {
            if (be instanceof GTGreenhousePortBlockEntity port) {
                GTGreenhousePortBlockEntity.serverTick(lvl, pos, st, port);
            }
        }
                : null;
    }
}