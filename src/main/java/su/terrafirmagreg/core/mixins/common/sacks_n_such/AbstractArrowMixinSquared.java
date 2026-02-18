package su.terrafirmagreg.core.mixins.common.sacks_n_such;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

@Mixin(value = AbstractArrow.class, priority = 1100)
public abstract class AbstractArrowMixinSquared {

    /** Handles arrow pickup logic
     * @author Ujhik
     * @reason To correct the arrow dupe bug that happens when a player has and stack of arrows in the inventory, no quiver, and picks up a shoot arrow from the ground.
    */
    @SuppressWarnings("CancellableInjectionUsage")
    @TargetHandler(mixin = "mod.traister101.sns.mixins.common.AbstractArrowMixin", name = "tryInsertIntoQuiver")
    @Inject(method = "@MixinSquared:Handler", at = @At("RETURN"))
    private void tfg$modifyReturnBasedOnRemainder(
            Player player,
            CallbackInfoReturnable<Boolean> cir,
            CallbackInfo ci,
            @SuppressWarnings("LocalMayBeArgsOnly") @Local(name = "inventoryRemainder") ItemStack inventoryRemainder) {

        if (inventoryRemainder != null && inventoryRemainder.getCount() < 1) {
            cir.setReturnValue(true);
        }
    }
}
