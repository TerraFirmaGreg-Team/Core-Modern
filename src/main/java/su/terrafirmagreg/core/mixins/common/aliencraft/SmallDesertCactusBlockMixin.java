package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.common.tags.ModBlockTags;
import en.predator97427fr.aliencraft.block.SmallDesertCactusBlock;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = SmallDesertCactusBlock.class, remap = false)
public class SmallDesertCactusBlockMixin {

    // Allows this plant to be placed on any venus blocks instead of just kepler ones

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$mayPlaceOn(BlockState groundState, BlockGetter worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(groundState.is(ModBlockTags.VENUS_STONE_REPLACEABLES));
    }

    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState blockstate, boolean clientSide, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "entityInside", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$entityInside(BlockState blockstate, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity.getType().is(TFGTags.Entities.IgnoresCacti)) {
            ci.cancel();
        }
    }
}
