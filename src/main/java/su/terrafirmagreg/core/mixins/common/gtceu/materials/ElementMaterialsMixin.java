package su.terrafirmagreg.core.mixins.common.gtceu.materials;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.common.data.materials.ElementMaterials;

@Mixin(value = ElementMaterials.class, remap = false)
public abstract class ElementMaterialsMixin {

    /**
     * Отключение инстурментов из железа GTCEu.
     */
    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/Material$Builder;toolStats(Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;)Lcom/gregtechceu/gtceu/api/data/chemical/material/Material$Builder;", ordinal = 1), remap = false)
    private static Material.Builder tfg$register$toolStats$iron(Material.Builder instance, ToolProperty toolProperty) {
        return instance;
    }
}
