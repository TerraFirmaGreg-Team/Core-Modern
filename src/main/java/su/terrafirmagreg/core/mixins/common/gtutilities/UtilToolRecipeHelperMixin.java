package su.terrafirmagreg.core.mixins.common.gtutilities;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.data.recipes.FinishedRecipe;
import net.neganote.gtutilities.common.tools.UtilToolType;
import net.neganote.gtutilities.common.tools.recipe.UtilToolRecipeHelper;

import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialFlags;

@Mixin(value = UtilToolRecipeHelper.class, remap = false)
public abstract class UtilToolRecipeHelperMixin {

    // Replaces the "does this tool have an LV electric tool" condition with "does this tool head have the material flag"
    // for building recipes for other tiered electric tools

    @Shadow
    private static void addElectricToolRecipe(Consumer<FinishedRecipe> provider, TagPrefix toolHead, GTToolType[] toolItems, Material material) {
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 0, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricChainsaw(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_CHAINSAW_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 1, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricDrill(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_DRILL_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 2, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricWirecutter(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_WIRE_CUTTER_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 4, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricWrench(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_WRENCH_HEAD);
    }

    @Redirect(method = "processElectricTool", at = @At(value = "INVOKE", ordinal = 5, target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/ToolProperty;hasType(Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;)Z"), remap = false)
    private static boolean tfg$processElectricScrewdriver(ToolProperty property, GTToolType toolType, @Local(argsOnly = true) Material material) {
        return material.hasFlag(TFGMaterialFlags.GENERATE_SCREWDRIVER_HEAD);
    }

    @Inject(method = "processElectricTool", at = @At("TAIL"), remap = false)
    private static void tfg$processElectricBuzzsaw(Consumer<FinishedRecipe> provider, ToolProperty property, Material material, CallbackInfo ci) {
        if (!material.hasFlag(TFGMaterialFlags.GENERATE_BUZZSAW_BLADE))
            return;

        if (property.hasType(UtilToolType.BUZZSAW_MV)) {
            addElectricToolRecipe(provider, TagPrefix.toolHeadBuzzSaw, new GTToolType[] { UtilToolType.BUZZSAW_MV }, material);
        } else if (property.hasType(UtilToolType.BUZZSAW_HV)) {
            addElectricToolRecipe(provider, TagPrefix.toolHeadBuzzSaw, new GTToolType[] { UtilToolType.BUZZSAW_HV }, material);
        } else if (property.hasType(UtilToolType.BUZZSAW_EV)) {
            addElectricToolRecipe(provider, TagPrefix.toolHeadBuzzSaw, new GTToolType[] { UtilToolType.BUZZSAW_EV }, material);
        } else if (property.hasType(UtilToolType.BUZZSAW_IV)) {
            addElectricToolRecipe(provider, TagPrefix.toolHeadBuzzSaw, new GTToolType[] { UtilToolType.BUZZSAW_IV }, material);
        } else if (property.hasType(UtilToolType.BUZZSAW_LuV)) {
            addElectricToolRecipe(provider, TagPrefix.toolHeadBuzzSaw, new GTToolType[] { UtilToolType.BUZZSAW_LuV }, material);
        } else if (property.hasType(UtilToolType.BUZZSAW_ZPM)) {
            addElectricToolRecipe(provider, TagPrefix.toolHeadBuzzSaw, new GTToolType[] { UtilToolType.BUZZSAW_ZPM }, material);
        }
    }
}
