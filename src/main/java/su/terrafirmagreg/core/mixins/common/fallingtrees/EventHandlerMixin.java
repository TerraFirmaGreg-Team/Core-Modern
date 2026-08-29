package su.terrafirmagreg.core.mixins.common.fallingtrees;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import me.pandamods.fallingtrees.event.EventHandler;

@Mixin(value = EventHandler.class, remap = false)
public class EventHandlerMixin {

    @Redirect(method = "makeTreeFall(Lme/pandamods/fallingtrees/api/Tree;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/player/Player;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", remap = true))
    private static void tfg$logDamageLikeBlockBreak(ItemStack stack, int amount, LivingEntity entity, Consumer<LivingEntity> onBroken) {
        if (stack.getItem() instanceof IGTTool) {
            ToolHelper.damageItem(stack, entity, amount);
        } else {
            stack.hurtAndBreak(amount, entity, onBroken);
        }
    }
}
