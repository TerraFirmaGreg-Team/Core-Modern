package su.terrafirmagreg.core.mixins.common.hang_glider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import fuzs.hangglider.world.item.GliderItem;

import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = GliderItem.class, remap = false)
public abstract class GliderItemMixin {

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At(value = "HEAD"), cancellable = true, remap = true)
    private void tfg$cancelSpaceGliding(Level level, Player player, InteractionHand usedHand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ForgeConfigSpec.BooleanValue gliderPlanetToggle = TFGConfig.SERVER.glidersWorkOnPlanets.get(level.dimension());
        if (gliderPlanetToggle != null && !gliderPlanetToggle.get()) {
            if (!level.isClientSide()) {
                MutableComponent chatMessage = Component.translatable("tfg.hangglider.disabled_dimension")
                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
                player.sendSystemMessage(chatMessage);
            }
            cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(usedHand)));
        }
    }
}
