package su.terrafirmagreg.core.mixins.client.afc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.therighthon.afc.compat.jade.TapBlockEntityProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import su.terrafirmagreg.core.common.environment.IGreenhouseReceiver;

@Mixin(value = TapBlockEntityProvider.class, remap = false)
public abstract class TapBlockEntityProviderMixin {

    @Inject(method = "appendServerData(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/BlockAccessor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putFloat(Ljava/lang/String;F)V", ordinal = 3))
    private void tfg$injectGreenhouseValidityOnAppendServer(CompoundTag tag, BlockAccessor accessor, CallbackInfo ci, @Local(name = "compound") CompoundTag compound) {
        compound.putBoolean("inValidGreenhouse", accessor.getBlockEntity() instanceof IGreenhouseReceiver receiver && receiver.tfg$isInsideValidGreenhouse());
    }

    @WrapOperation(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lsnownee/jade/api/ITooltip;add(Lnet/minecraft/network/chat/Component;)V", ordinal = 5))
    private static void tfg$overrideTooltip(ITooltip tooltip, Component component, Operation<Void> original, @Local(name = "treeTapData") CompoundTag treeTapData) {
        if (treeTapData.getBoolean("inValidGreenhouse")) {
            tooltip.add(Component.translatable("tfg.tooltip.firmalife_inside_valid_greenhouse").withStyle(ChatFormatting.GREEN));
        } else {
            original.call(tooltip, component);
        }
    }
}
