package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.common.data.materials.SecondDegreeMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SecondDegreeMaterials.class, remap = false)
public class SecondDegreeMixin {

    /**
     * Отключение инстурментов из кремения GTCEu.
     * */
    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/Material$Builder;toolStats(Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;)Lcom/gregtechceu/gtceu/api/data/chemical/material/Material$Builder;", ordinal = 5), remap = false)
    private static Material.Builder disableSterlingSilverTools(Material.Builder instance, ToolProperty toolProperty) {
        return instance;
    }


}
