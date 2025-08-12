package su.terrafirmagreg.core.common.data.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import su.terrafirmagreg.core.common.data.blocks.ReflectorBlock;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class ReflectorBlockEntity extends BlockEntity {

    public ReflectorBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.REFLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    // Static tick method called each tick by the ticker
    public static void tick(Level level, BlockPos pos, BlockState state, ReflectorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        long dayTime = level.getDayTime() % 24000L;
        int lightLevel;
        if (dayTime < 400) lightLevel = 5;
        else if (dayTime < 4000) lightLevel = 10;
        else if (dayTime < 8000) lightLevel = 15;
        else if (dayTime < 10000) lightLevel = 10;
        else if (dayTime < 13000) lightLevel = 5;
        else lightLevel = 0;

        BlockState currentState = level.getBlockState(pos);
        int currentLight = currentState.getValue(ReflectorBlock.LIGHT_LEVEL);
        if (currentLight != lightLevel) {
            level.setBlock(pos, currentState.setValue(ReflectorBlock.LIGHT_LEVEL, lightLevel), 3);
        }
    }
}
