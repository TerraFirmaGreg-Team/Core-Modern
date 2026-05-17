package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.forge.ForgeCommonEventListener;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mixin(value = ForgeCommonEventListener.class, remap = false)
public class GTForgeCommonEventListenerMixin {
    /**
     * @author Sakura
     * @reason Messes with block runner for some unknown reason.
     */
    @Overwrite
    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
    }
}
