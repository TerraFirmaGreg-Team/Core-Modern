package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.negodya1.vintageimprovements.content.kinetics.centrifuge.CentrifugeBlockEntity;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

@Mixin(value = CentrifugeBlockEntity.class, remap = false)
public class CentrifugeBlockEntityMixin {
    @ModifyExpressionValue(method = "addToGoggleTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", remap = true))
    private MutableComponent tfg$useItemDisplayName(MutableComponent original, @Local ItemStack stackInSlot) {
        return stackInSlot.getHoverName().copy();
    }
}
