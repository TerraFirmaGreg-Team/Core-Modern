package su.terrafirmagreg.core.mixins.common.gtceu.tools;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.ToolHeadReplaceRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;

@Mixin(value = ToolHeadReplaceRecipe.class, remap = false)
public class ToolHeadReplaceRecipeMixin {

    // matches() accepts combos whose (material, toolType) isn't registered in TOOL_ITEMS,
    // causing assemble() to NPE on the subsequent .get() call.
    // Mixin can be removed if https://github.com/GregTechCEu/GregTech-Modern/pull/4809 is merged and released
    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/item/IGTTool;getElectricTier()I"), cancellable = true)
    private void tfg$rejectUnregisteredToolCombo(CraftingContainer inv, Level level, CallbackInfoReturnable<Boolean> cir, @Local(name = "tool") IGTTool tool,
            @Local(name = "toolHead") MaterialEntry toolHead, @Local(name = "output") GTToolType[] output) {
        if (output == null || output[tool.getElectricTier()] == null)
            return;
        if (GTMaterialItems.TOOL_ITEMS.get(toolHead.material(), output[tool.getElectricTier()]) == null) {
            cir.setReturnValue(false);
        }
    }
}
