package su.terrafirmagreg.core.mixins.common.tfc;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;

import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = VerticalSupportBlock.class, remap = false)
public abstract class VerticalSupportBlockMixin extends Block {

    public VerticalSupportBlockMixin(Properties properties) {
        super(properties);
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
     * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever possible. Implementing/overriding is fine.
     */
    @Deprecated
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BlockState newState = state;

        switch (rotation) {
            case CLOCKWISE_90: {
                newState = newState.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.WEST));
                newState = newState.setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.EAST));
                newState = newState.setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.NORTH));
                newState = newState.setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.SOUTH));
                break;
            }
            case COUNTERCLOCKWISE_90: {
                newState = newState.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.EAST));
                newState = newState.setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.WEST));
                newState = newState.setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.SOUTH));
                newState = newState.setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.NORTH));
                break;
            }
            case CLOCKWISE_180: {
                newState = newState.setValue(BlockStateProperties.NORTH, state.getValue(BlockStateProperties.SOUTH));
                newState = newState.setValue(BlockStateProperties.SOUTH, state.getValue(BlockStateProperties.NORTH));
                newState = newState.setValue(BlockStateProperties.EAST, state.getValue(BlockStateProperties.WEST));
                newState = newState.setValue(BlockStateProperties.WEST, state.getValue(BlockStateProperties.EAST));
                break;
            }

        }
        return newState;
    }

	// Make support blocks not suffocate (helps with mobs)
	@ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static ExtendedProperties tfg$init(ExtendedProperties value) {
		return value.isSuffocating((s, b, p) -> false);
	}
}
