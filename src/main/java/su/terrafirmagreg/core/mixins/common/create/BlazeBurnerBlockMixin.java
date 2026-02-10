package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Mixin to prevent solid fuel from being burned in the Blaze Burner by hand.
 * Plays a sound when the interaction fails.
 */
@Mixin(value = BlazeBurnerBlock.class)
public abstract class BlazeBurnerBlockMixin {
    @Inject(method = "use", at = @At(value = "RETURN"), cancellable = true)
    private void injected(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() == InteractionResult.SUCCESS
                && level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
            Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
