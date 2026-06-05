package su.terrafirmagreg.core.mixins.client.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.client.forge.ForgeClientEventListener;

import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mixin(value = ForgeClientEventListener.class, remap = false)
public class GTForgeClientEventListenerMixin {
    /**
     * @author Sakura
     * @reason Disables that damn FOV change when walking on blocks so we can reuse tags in block runner.
     */
    @Overwrite
    @SubscribeEvent
    public static void updateFOV(ComputeFovModifierEvent event) {
    }
}
