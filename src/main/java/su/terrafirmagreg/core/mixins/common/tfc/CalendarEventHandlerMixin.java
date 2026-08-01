package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.util.calendar.CalendarEventHandler;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;

@Mixin(value = CalendarEventHandler.class, remap = false)
public abstract class CalendarEventHandlerMixin {

    /**
     * TFC 3 reads {@code getDayTime()} from the sleeping dimension, which breaks calendar sync in Nether/Beneath.
     * {@link su.terrafirmagreg.core.utils.CalendarSleepHelper} handles those dimensions instead.
     */
    @Inject(method = "onPlayerWakeUp", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$skipNonOverworldSleep(PlayerWakeUpEvent event, CallbackInfo ci) {
        if (!event.getEntity().getCommandSenderWorld().dimension().equals(Level.OVERWORLD)) {
            ci.cancel();
        }
    }
}
