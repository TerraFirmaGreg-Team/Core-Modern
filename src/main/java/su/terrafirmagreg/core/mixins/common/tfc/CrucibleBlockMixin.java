package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;

import net.dries007.tfc.common.blocks.devices.CrucibleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(value = CrucibleBlock.class)
public abstract class CrucibleBlockMixin {

    /**
     * Fixes a compat issue with the mechanical arm and crucibles
     */

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void tfg$use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        var heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmItem) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
