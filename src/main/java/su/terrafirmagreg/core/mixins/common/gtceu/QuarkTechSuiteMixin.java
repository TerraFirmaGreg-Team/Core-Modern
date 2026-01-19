package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = QuarkTechSuite.class)
public class QuarkTechSuiteMixin {

    // Cancel QuarkTech helmet feeding when player is in PlayerRevive bleeding state
    // This prevents food from being consumed without effect
    @SuppressWarnings("resource") // Don't want to close player.level() in a `finally` clause
    @Inject(method = "supplyFood", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$preventQuarkFeedingWhenBleeding(IElectricItem item, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getPersistentData().getBoolean("playerrevive:bleeding") || player.level().isClientSide) {
            cir.setReturnValue(false);
        }
    }

    // Only eat food, don't eat blacklisted food
    @WrapOperation(method = "supplyFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;"), remap = false)
    private FoodProperties tfg$checkFoodTag(ItemStack stack, LivingEntity player, Operation<FoodProperties> original) {
        if (stack.is(TFCTags.Items.FOODS) && !stack.is(TFGTags.Items.AutoEatBlacklist)) {
            return original.call(stack, player);
        }
        return null;
    }
}
