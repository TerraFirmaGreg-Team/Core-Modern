package su.terrafirmagreg.core.mixins.client.tfc;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.util.Drinkable;
import net.minecraft.world.level.material.Fluid;

@Mixin(value = Drinkable.class, remap = false)
public class DrinkableMixin {
    /** Returns the drinkable for a fluid, used when drinking from containers
     * @author Ujhik
     * @reason To bypass tfc:alcohol drinkable that is hardcoded in firmaLife, so rose, sparkling and dessert wines return their correct drinkable (that has the corresponding buffing effects) instead of a generic tfc:alcohol) that does nothing
     */
    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private static void tfg$postFilterAlcohol(Fluid fluid, CallbackInfoReturnable<Drinkable> cir) {
        final String TFC_ALCOHOL_ID = "tfc:alcohol";
        Drinkable original = cir.getReturnValue();
        if (original == null || !original.getId().toString().equals(TFC_ALCOHOL_ID))
            return;

        // Get all Drinkables for this fluid
        Collection<Drinkable> cachedMatchingDrinkables = Drinkable.CACHE.getAll(fluid);

        // Only intervene if there are more than 1 drinkable
        if (cachedMatchingDrinkables.size() < 2)
            return;

        // Find the next non-alcohol match
        for (Drinkable candidateDrinkable : cachedMatchingDrinkables) {
            if (candidateDrinkable.matches(fluid) && !candidateDrinkable.getId().toString().equals(TFC_ALCOHOL_ID)) {
                cir.setReturnValue(candidateDrinkable);
                break;
            }
        }
    }
}
