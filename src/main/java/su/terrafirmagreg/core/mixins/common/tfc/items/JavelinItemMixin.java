package su.terrafirmagreg.core.mixins.common.tfc.items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.dries007.tfc.common.items.JavelinItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.entity.projectile.ILeashedJavelin;

/**
 * Mixin to JavelinItem to add leashing functionality.
 */
@Mixin(value = JavelinItem.class, remap = false)
public class JavelinItemMixin {

    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tfg$onReleaseUsing(ItemStack stack, Level level, LivingEntity entity, int ticksLeft, CallbackInfo ci, Player player, int i, ThrownJavelin javelin) {
        InteractionHand otherHand = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (player.getItemInHand(otherHand).is(Items.LEAD)) {
            if (javelin instanceof ILeashedJavelin leashed) {
                leashed.tfg$setLeashed(player);
            }
        }
    }
}
