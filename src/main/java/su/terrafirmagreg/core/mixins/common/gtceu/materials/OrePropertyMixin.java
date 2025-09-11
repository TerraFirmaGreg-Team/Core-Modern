package su.terrafirmagreg.core.mixins.common.gtceu.materials;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;

@Mixin(value = OreProperty.class, remap = false)
public abstract class OrePropertyMixin {

    @Shadow
    @Final
    private List<Material> oreByProducts;

    // Clear the byproduct list before adding new ones, otherwise it can't be edited in kjs
    @Inject(method = "setOreByProducts*", at = @At("HEAD"), remap = false)
    public void tfg$setOreByProducts(Material[] materials, CallbackInfo ci) {
        this.oreByProducts.clear();
    }
}
