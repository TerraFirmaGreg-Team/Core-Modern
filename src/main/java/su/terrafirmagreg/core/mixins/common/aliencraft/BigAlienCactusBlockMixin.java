package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import en.predator97427fr.aliencraft.block.BigAlienCactusBlock;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = BigAlienCactusBlock.class, remap = false)
public class BigAlienCactusBlockMixin {

    // Remove cactus damage
    @Inject(method = "entityInside", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity.getType().is(TFGTags.Entities.IgnoresCacti)) {
            ci.cancel();
        }
    }
}
