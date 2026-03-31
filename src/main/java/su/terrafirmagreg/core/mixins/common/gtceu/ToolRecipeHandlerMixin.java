package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler;
import com.llamalad7.mixinextras.sugar.Local;

import su.terrafirmagreg.core.common.tfgt.material.TFGMaterialFlags;

@Mixin(value = ToolRecipeHandler.class, remap = false)
public class ToolRecipeHandlerMixin {

    // Replaces the "does this tool have an LV electric tool" condition with "does this tool head have the material flag"
    // for building recipes for other tiered electric tools

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 0, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricDrill(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_DRILL_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 1, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricChainsaw(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_CHAINSAW_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 2, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricWrench(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_WRENCH_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 3, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricWireCutter(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_WIRE_CUTTER_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 5, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricScrewdriver(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_SCREWDRIVER_HEAD);
    }
}
