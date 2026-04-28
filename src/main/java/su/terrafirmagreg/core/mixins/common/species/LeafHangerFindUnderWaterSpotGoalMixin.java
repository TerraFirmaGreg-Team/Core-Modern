package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.ninni.species.server.entity.mob.update_3.LeafHanger;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

@Mixin(value = LeafHanger.FindUnderWaterSpotGoal.class, remap = false)
public class LeafHangerFindUnderWaterSpotGoalMixin {

    @Redirect(method = "getWaterPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"), remap = true)
    private boolean tfg$tick(BlockState instance, Block block) {
        return instance.is(Blocks.WATER)
                || instance.is(TFCBlocks.SALT_WATER.get())
                || instance.is(TFGBlocks.MUDDY_WATER.get());
    }
}
