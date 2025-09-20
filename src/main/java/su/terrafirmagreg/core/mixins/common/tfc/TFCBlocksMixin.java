package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.function.ToIntFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;

@Mixin(value = TFCBlocks.class, remap = false)
public class TFCBlocksMixin {

    /**
     * @author Pyritie
     * @reason Adds support for mars water. Required if we want rock spikes to be mars-waterlogged
     */
    @Overwrite
    public static ToIntFunction<BlockState> lavaLoggedBlockEmission() {
        return state -> state.getValue(TFGBlockProperties.SPACE_WATER_AND_LAVA).is(((IFluidLoggable) state.getBlock()).getFluidProperty().keyFor(Fluids.LAVA)) ? 15 : 0;
    }
}
