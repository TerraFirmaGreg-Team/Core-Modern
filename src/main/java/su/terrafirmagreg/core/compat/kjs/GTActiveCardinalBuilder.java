package su.terrafirmagreg.core.compat.kjs;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.integration.kjs.builders.block.ActiveBlockBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * This builder adds a new GT active block that can be rotated in cardinal directions.
 */
public class GTActiveCardinalBuilder extends ActiveBlockBuilder {

    /**
     * Instantiates a new Gt active cardinal builder.
     *
     * @param id the id
     */
    public GTActiveCardinalBuilder(ResourceLocation id) {
        super(id);
        property(BlockStateProperties.HORIZONTAL_FACING);

    }

    @Override
    public Block createObject() {
        return new ActiveBlock(createProperties()) {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(BlockStateProperties.HORIZONTAL_FACING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                return this.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
            }

            @Override
            public BlockState rotate(BlockState state, Rotation rotation) {
                return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
            }

            @Override
            public BlockState mirror(BlockState state, Mirror mirror) {
                return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
            }
        };
    }
}
