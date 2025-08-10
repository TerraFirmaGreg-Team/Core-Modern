package su.terrafirmagreg.core.mixins.common.hang_glider;

import fuzs.hangglider.world.item.GliderItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GliderItem.class, remap = false)
public abstract class GliderItemMixin {
    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At(value = "INVOKE", target = "Lfuzs/hangglider/helper/PlayerGlidingHelper;isValidGlider(Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true, remap = true)
    private void testing(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (true) {
            System.out.println("get fucked lmao");
            cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(usedHand)));
        }
    }
}
