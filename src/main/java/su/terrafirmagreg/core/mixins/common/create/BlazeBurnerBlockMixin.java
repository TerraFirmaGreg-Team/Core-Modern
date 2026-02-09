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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(value = BlazeBurnerBlock.class)
public abstract class BlazeBurnerBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void injected(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
            Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "tryInsert", at = @At("HEAD"), cancellable = true, remap = false)
    private static void doNotInsertInSpace(BlockState state, Level level, BlockPos pos, ItemStack stack, boolean doNotConsume, boolean forceOverflow, boolean simulate,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
            cir.setReturnValue(InteractionResultHolder.fail(ItemStack.EMPTY));
        }
    }
}
