package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import com.gregtechceu.gtceu.data.recipe.MaterialInfoLoader;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.compat.kjs.events.TFGMaterialInfoModification;
import su.terrafirmagreg.core.compat.kjs.events.TFGStartupEvents;

@Mixin(value = MaterialInfoLoader.class, remap = false)
public abstract class MaterialInfoLoaderMixin {

    /**
     * Запускает наш метод для регистрации информации о предмете в системе GTM.
     * */
    @Inject(method = "init", at = @At(value = "TAIL"), remap = false)
    private static void tfg$init(CallbackInfo ci) {
        TFGStartupEvents.MATERIAL_INFO_MODIFICATION.post(ScriptType.STARTUP, new TFGMaterialInfoModification());
    }
}
