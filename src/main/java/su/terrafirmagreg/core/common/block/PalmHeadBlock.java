package su.terrafirmagreg.core.common.block;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import su.terrafirmagreg.core.common.blockentity.PalmHeadBlockEntity;
import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class PalmHeadBlock extends Block implements EntityBlock {

    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    private final PalmTrees tree;

    public PalmHeadBlock(Properties properties, PalmTrees tree) {
        super(properties);
        this.tree = tree;
        this.registerDefaultState(this.stateDefinition.any().setValue(NATURAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NATURAL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TFGBlockEntities.PALM_HEADS.get(tree).get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof PalmHeadBlockEntity palmHead) {
                PalmHeadBlockEntity.serverTick(l, p, s, palmHead);
            }
        };
    }
}
