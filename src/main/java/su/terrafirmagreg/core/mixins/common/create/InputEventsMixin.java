package su.terrafirmagreg.core.mixins.common.create;

import com.simibubi.create.foundation.events.InputEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = InputEvents.class, remap = false)
public class InputEventsMixin {
    @Inject(method = "onClickInput(Lnet/minecraftforge/client/event/InputEvent$InteractionKeyMappingTriggered;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$onClickInput(InputEvent.InteractionKeyMappingTriggered event, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack itemInHand = mc.player.getItemInHand(event.getHand());
            if (itemInHand.is(TFGTags.Items.Chains))
                ci.cancel();
        }
    }
}
