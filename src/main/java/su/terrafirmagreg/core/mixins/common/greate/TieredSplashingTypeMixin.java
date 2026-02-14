package su.terrafirmagreg.core.mixins.common.greate;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes.SplashingType;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.recipe.RecipeFinder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;

import electrolyte.greate.content.kinetics.fan.TieredEncasedFanBlockEntity;
import electrolyte.greate.content.processing.recipe.TieredProcessingRecipe;
import electrolyte.greate.foundation.data.recipe.TieredRecipeConditions;
import electrolyte.greate.foundation.recipe.TieredRecipeApplier;
import electrolyte.greate.registry.ModRecipeTypes;

@Mixin(targets = "electrolyte.greate.content.kinetics.fan.processing.GreateFanProcessingTypes$TieredSplashingType", remap = false)
public abstract class TieredSplashingTypeMixin {

    // Use our own cache key, to avoid needing @Shadow for the private key in Greate
    @Unique
    private static final Object TFG_SPLASHING_CACHE_KEY = new Object();

    // Prefer recipes that actually specify fluid + circuit, and prefer higher tier
    @Unique
    private static final Comparator<Recipe<?>> TFG_SPLASHING_PRIORITY = Comparator.<Recipe<?>, Boolean>comparing(r -> r instanceof ProcessingRecipe<?> pr && !pr.getFluidIngredients().isEmpty())
            .reversed()
            .thenComparing(r -> r instanceof TieredProcessingRecipe<?> tpr && tpr.getCircuitNumber() != -1, Comparator.reverseOrder())
            .thenComparingInt(r -> r instanceof TieredProcessingRecipe<?> tpr ? tpr.getRecipeTier() : Integer.MIN_VALUE)
            .reversed()
            .thenComparingInt(r -> {
                if (r instanceof ProcessingRecipe<?> pr && !pr.getFluidIngredients().isEmpty())
                    return pr.getFluidIngredients().get(0).getRequiredAmount();
                return 0;
            }).reversed();

    @Inject(method = "canProcess(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;ILelectrolyte/greate/content/kinetics/fan/TieredEncasedFanBlockEntity;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$canProcessPreferTiered(ItemStack stack, Level level, int machineTier, TieredEncasedFanBlockEntity fanBE,
            CallbackInfoReturnable<Boolean> cir) {
        Recipe<?> best = core_Modern$findBestTieredSplashingRecipe(stack, level, machineTier, fanBE);
        if (best != null) {
            cir.setReturnValue(true);
            return;
        }

        // Fallback to Create original logic
        cir.setReturnValue(((SplashingType) (Object) this).canProcess(stack, level));
    }

    @Inject(method = "process(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;ILelectrolyte/greate/content/kinetics/fan/TieredEncasedFanBlockEntity;)Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$processPreferTiered(ItemStack stack, Level level, int machineTier, TieredEncasedFanBlockEntity fanBE,
            CallbackInfoReturnable<List<ItemStack>> cir) {
        Recipe<?> best = core_Modern$findBestTieredSplashingRecipe(stack, level, machineTier, fanBE);
        if (best != null) {
            cir.setReturnValue(TieredRecipeApplier.applyRecipeOn(level, stack, best, machineTier, true, fanBE));
            return;
        }

        // Fallback to Create original logic
        cir.setReturnValue(((SplashingType) (Object) this).process(stack, level));
    }

    @Unique
    private static Recipe<?> core_Modern$findBestTieredSplashingRecipe(ItemStack stack, Level level, int machineTier, TieredEncasedFanBlockEntity fanBE) {
        List<Recipe<?>> recipes = RecipeFinder.get(
                TFG_SPLASHING_CACHE_KEY,
                level,
                p -> p.getType() == ModRecipeTypes.SPLASHING.getType());

        Optional<IFluidHandler> fh = fanBE.getCapability(ForgeCapabilities.FLUID_HANDLER).resolve();

        return fh.flatMap(iFluidHandler -> recipes.stream()
                .filter(TieredRecipeConditions.firstIngredientMatches(stack))
                .filter(TieredRecipeConditions.firstIngredientCountMatches(stack))
                .filter(TieredRecipeConditions.firstFluidMatches(iFluidHandler.getFluidInTank(0)))
                .filter(TieredRecipeConditions.isEqualOrAboveTier(machineTier))
                .filter(TieredRecipeConditions.circuitMatches(fanBE.getTargetCircuit().getValue()))
                .max(TFG_SPLASHING_PRIORITY)).orElse(null);
    }
}
