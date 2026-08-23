package su.terrafirmagreg.core.mixins.common.gtceu.tools;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(GTToolItem.class)
public class GTToolItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$allowGtWrenchesOnCreateBlocks(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockEntity be = ctx.getLevel().getBlockEntity(pos);
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (be instanceof KineticBlockEntity && block instanceof IWrenchable wrenchable) {
            Player player = ctx.getPlayer();
            if (player == null || !player.mayBuild())
                return;

            if (player.isShiftKeyDown()) {
                cir.setReturnValue(wrenchable.onSneakWrenched(state, ctx));
            } else {
                cir.setReturnValue(wrenchable.onWrenched(state, ctx));
            }
        }
    }
}
