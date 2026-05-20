package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.event.WearableAccessoryHandler;

@Mixin(SnowLayerBlock.class)
public class SnowLayerBlockSnowshoesMixin {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void tfg$snowshoesStandOnSnowLayer(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return;
        }
        Entity entity = entityContext.getEntity();
        if (!(entity instanceof Player player) || !WearableAccessoryHandler.hasSnowshoesEquipped(player)) {
            return;
        }

        int height = state.getValue(SnowLayerBlock.LAYERS) * 2;
        cir.setReturnValue(Block.box(0, 0, 0, 16, height, 16));
    }
}
