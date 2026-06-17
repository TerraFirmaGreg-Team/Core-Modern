package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "setHoverName", at = @At("TAIL"))
    private void tfg$playerHeadRename(Component name, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() != Items.PLAYER_HEAD) {
            return;
        }

        String username = name.getString().trim();
        if (username.isEmpty()) {
            stack.removeTagKey("SkullOwner");
            stack.resetHoverName();
            return;
        }

        stack.getOrCreateTag().putString("SkullOwner", username);
        stack.resetHoverName();
    }
}
