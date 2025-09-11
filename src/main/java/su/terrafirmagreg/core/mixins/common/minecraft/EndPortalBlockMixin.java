package su.terrafirmagreg.core.mixins.common.minecraft;

import static su.terrafirmagreg.core.common.TFGHelpers.sendChatMessagePortalsIsDisabled;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin extends Block {

    protected EndPortalBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    private void tfg$entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, CallbackInfo ci) {
        sendChatMessagePortalsIsDisabled(pLevel, pEntity);
        ci.cancel();
    }

}
