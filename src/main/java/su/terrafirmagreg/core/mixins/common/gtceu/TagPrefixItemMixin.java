package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(value = TagPrefixItem.class, remap = false)
public abstract class TagPrefixItemMixin {

    /**
     * Если в руке горячий слиток, то если у вас в руках щипцы, то вы не будете получать урон.
     */
    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), remap = true, cancellable = true)
    private void tfg$inventoryTick$livingEntity$getItemBySlot(ItemStack stack, Level level, Entity entity, int slotId,
            boolean isSelected, CallbackInfo ci) {
//        if (entity instanceof Player player) {
//            var stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
//            if (stackInOffHand.is(TFGTags.Items.Tongs)) {
//                ToolHelper.damageItem(stackInOffHand, player);
//                ci.cancel();
//            }
//        }
    }

}
