package su.terrafirmagreg.core.mixins.common.gtceu;

import dev.latvian.mods.kubejs.script.ScriptType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.compat.kjs.events.TFGMaterialInfoModification;
import su.terrafirmagreg.core.compat.kjs.events.TFGStartupEvents;

@Mixin(value = com.gregtechceu.gtceu.common.CommonProxy.class, remap = false)
public abstract class CommonProxy {

    @Inject(method = "loadComplete", at = @At(value = "TAIL"), remap = false)
    private void onLoadComplete(CallbackInfo ci) {
        TFGStartupEvents.MATERIAL_INFO_MODIFICATION.post(ScriptType.STARTUP, new TFGMaterialInfoModification());
        System.out.println("asdasd");
    }
}
