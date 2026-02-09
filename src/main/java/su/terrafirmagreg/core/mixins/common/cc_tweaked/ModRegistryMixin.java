package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.item.CreativeModeTab;

import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.turtle.items.TurtleItem;

@Mixin(value = ModRegistry.class, remap = false)
public class ModRegistryMixin {

    @Inject(method = "addTurtle", at = @At(value = "INVOKE", target = "dan200/computercraft/impl/TurtleUpgrades.getVanillaUpgrades ()Ljava/util/stream/Stream;"), cancellable = true)
    private static void tfg$removeTurtleStream(CreativeModeTab.Output out, TurtleItem turtle, CallbackInfo ci) {
        ci.cancel();
    }
}
