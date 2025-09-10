package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.ArcFurnaceLogic;

import net.minecraft.world.item.ItemStack;

@Mixin(value = ArcFurnaceLogic.class, remap = false)
public abstract class ArcFurnaceLogicMixin {

    // Prevents tools from being recycled into more ingots than they started with

    @Redirect(method = "search", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/common/machine/trait/customlogic/ArcFurnaceLogic;applyDurabilityRecipe(Ljava/lang/String;Lnet/minecraft/world/item/ItemStack;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;FFJI)Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;"), remap = false)
    public GTRecipe tfg$search(ArcFurnaceLogic instance, String id, ItemStack inputStack, Material mat,
            float fullAmount, float durability, long voltage, int durationFactor) {
        return null;
    }

}
