package su.terrafirmagreg.core.mixins.common.gtceu.materials;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import su.terrafirmagreg.core.compat.gtceu.TFGPropertyKeys;

@Mixin(value = MaterialProperties.class, remap = false)
public abstract class MaterialPropertiesMixin {

    /**
     * Добавляет наш вариант в список базовых проперти, позволяет добавить только TFC проперти для материала при
     * создании.
     */
    @SuppressWarnings("unused")
    @Shadow
    @Final
    @Mutable
    private static Set<PropertyKey<?>> baseTypes = new HashSet<>(Arrays.asList(PropertyKey.FLUID, PropertyKey.DUST,
            PropertyKey.INGOT, PropertyKey.GEM, PropertyKey.EMPTY, TFGPropertyKeys.TFC_PROPERTY));
}
