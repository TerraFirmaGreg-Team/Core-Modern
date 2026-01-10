package su.terrafirmagreg.core.mixins.common.gtceu.materials;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.llamalad7.mixinextras.sugar.Local;

import su.terrafirmagreg.core.common.data.tfgt.TFGTMedicalConditions;

@Mixin(value = Material.Builder.class)
public class MaterialBuilderMixin {

    // Changes radioactive materials to get a different condition than carcinogenic

    @ModifyArg(method = "radioactiveHazard", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/MaterialProperties;setProperty(Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/PropertyKey;Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/IMaterialProperty;)V"), index = 1, remap = false)
    private IMaterialProperty tfg$radioactiveHazard(IMaterialProperty value, @Local(argsOnly = true) float multiplier) {
        return new HazardProperty(HazardProperty.HazardTrigger.ANY, TFGTMedicalConditions.RADIOACTIVE, multiplier, true);
    }
}
