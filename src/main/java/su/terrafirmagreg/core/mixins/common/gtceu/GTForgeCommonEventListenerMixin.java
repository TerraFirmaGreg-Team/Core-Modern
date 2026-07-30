package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.common.CommonEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.terrafirmagreg.core.common.ForgeCommonEventListener;

@Mixin(value = CommonEventListener.class, remap = false)
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
