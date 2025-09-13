package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.beneath.common.blocks.LavaAqueductBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = LavaAqueductBlock.class)
public class LavaAqueductBlockMixin {

    /**
     * Prevent picking up lava from aqueducts, as that would allow you to get infinite lava
     */

    @Inject(method = "pickupBlock", at = @At("HEAD"), cancellable = true)
    public void tfg$pickupBlock(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1f, 1f);
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
