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

    /**
     * Targets the SMALL_ORES lambda to hide small ore indicators on the map, by modifying the argument to mapColor
     */
    //spotless:off
    @ModifyArg(
        method = "lambda$static$40()Lnet/minecraft/world/level/block/Block;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;mapColor(Lnet/minecraft/world/level/material/MapColor;)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;", remap = true),
        remap = false
    )
    //spotless:on
    private static MapColor tfg$hideIndicatorsOnMap(MapColor original) {
        return MapColor.NONE;
    }
}
