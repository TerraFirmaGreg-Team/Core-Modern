package su.terrafirmagreg.core.mixins.common.tfcimprovedbadlands;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import tfcimprovedbadlands.IBEventHandler;

import su.terrafirmagreg.core.TFGCore;

@Mixin(IBEventHandler.class)
public class IBEventHandlerMixin {

    /**
     * Remove vestigial ChunkLoadEvent listener registration.
     * event handler stores ChunkData in a static field that's never used anywhere else in the code.
     * It does so by calling TFC worldgen methods in a non-worldgen context, which can cause NPEs.
     * Removing this listener entirely doesn't change the actual worldgen, which is done with mixins rather than
     * through this listener.
     */
    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;addListener(Lnet/minecraftforge/eventbus/api/EventPriority;Ljava/util/function/Consumer;)V", ordinal = 1), remap = false)
    private static void tfg$skipChunkLoadListener(
            IEventBus instance,
            EventPriority priority,
            Consumer<?> listener,
            Operation<Void> original) {
        TFGCore.LOGGER.info("TFG: Disabled TFC Improved Badlands ChunkLoadEvent listener");
    }
}
