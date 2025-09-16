package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import earth.terrarium.adastra.api.planets.PlanetApi;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CleanroomCondition.class, remap = false)
public class CleanroomConditionMixin {

    @Inject(method = "testCondition", at = @At(value = "RETURN"), cancellable = true)
    void tfg$TestCondition(GTRecipe recipe, RecipeLogic recipeLogic, CallbackInfoReturnable<Boolean> cir){
        Level level = recipeLogic.getMachine().getLevel();
        boolean isOrbit = PlanetApi.API.isSpace(level);

        if (isOrbit) {
            cir.setReturnValue(true);
        }
    }

}
