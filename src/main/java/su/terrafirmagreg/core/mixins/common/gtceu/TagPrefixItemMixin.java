package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import tfchotornot.common.HNTags;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = TagPrefixItem.class, remap = false)
public abstract class TagPrefixItemMixin {

    // If you have a hot ingot in your hand, you will not take damage if you are holding tongs or wearing a protective chestpiece

    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0), remap = true, cancellable = true)
    private void tfg$handleHotItems(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
        if (entity instanceof Player player) {
            var stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (stackInOffHand.is(HNTags.Items.INSULATING)) {
                ToolHelper.damageItem(stackInOffHand, player);
                ci.cancel();
            } else if (player.getItemBySlot(EquipmentSlot.CHEST).is(TFGTags.Items.HotProtectionEquipment)) {
                ci.cancel();
            }
        }
    }

    // This one doesn't seem to be used by gregtech at the moment, but in case that changes...

    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 1), remap = true, cancellable = true)
    private void tfg$handleColdItems(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
        if (entity instanceof Player player) {
            var stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (stackInOffHand.is(HNTags.Items.INSULATING)) {
                ToolHelper.damageItem(stackInOffHand, player);
                ci.cancel();
            } else if (player.getItemBySlot(EquipmentSlot.CHEST).is(TFGTags.Items.ColdProtectionEquipment)) {
                ci.cancel();
            }
        }
    }
}
