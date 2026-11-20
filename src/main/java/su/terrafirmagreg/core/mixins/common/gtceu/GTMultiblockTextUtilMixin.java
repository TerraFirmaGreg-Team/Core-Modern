package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.world.item.ItemStack;

@Mixin(value = GTMultiblockTextUtil.class, remap = false)
public abstract class GTMultiblockTextUtilMixin {

    // Output Display in Multiblock GUI will now show icons of food that can't decay
    @ModifyVariable(method = "createItemLineForOutput", at = @At("STORE"), ordinal = 0)
    private static ItemStack tfg$displayFoodAsNonDecaying(ItemStack stack) {
        return FoodCapability.setStackNonDecaying(stack);
    }
}
