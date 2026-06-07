package su.terrafirmagreg.core.mixins.common.tfc.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.feature.plant.CreepingPlantFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = CreepingPlantFeature.class)
public class CreepingPlantFeatureMixin {

    // Makes the creeping plant feature (used by moss, lichen, etc) not replace fluid blocks

    @Redirect(method = "place", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/EnvironmentHelpers;isWorldgenReplaceable(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/core/BlockPos;)Z", remap = false), remap = true)
    private boolean tfg$place(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || Helpers.isBlock(state, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE);
    }
}
