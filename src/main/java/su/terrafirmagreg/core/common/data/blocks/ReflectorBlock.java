package su.terrafirmagreg.core.common.data.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blockentity.ReflectorBlockEntity;

public class ReflectorBlock extends Block implements EntityBlock {

    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

    public ReflectorBlock(Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter world, BlockPos pos) {
        return state.getValue(LIGHT_LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReflectorBlockEntity(TFGBlockEntities.REFLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type == TFGBlockEntities.REFLECTOR_BLOCK_ENTITY.get()) {
            return (level1, pos, state1, blockEntity) -> ReflectorBlockEntity.tick(level1, pos, state1,
                    (ReflectorBlockEntity) blockEntity);
        }
        return null;
    }

}
