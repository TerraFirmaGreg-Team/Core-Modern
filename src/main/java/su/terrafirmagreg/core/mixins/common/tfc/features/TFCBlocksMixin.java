package su.terrafirmagreg.core.mixins.common.tfc.features;

import java.util.function.ToIntFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

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

    @ModifyArg(method = "lambda$static$40()Lnet/minecraft/world/level/block/Block;", at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/state/BlockBehaviour$Properties.mapColor (Lnet/minecraft/world/level/material/MapColor;)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;"))
    private static MapColor tfg$hideIndicatorsOnMap(MapColor p_285137_) {
        return MapColor.NONE;
    }
}
