package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.gregtechceu.gtceu.api.recipe.ingredient.NBTPredicateIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;

import net.minecraft.world.item.ItemStack;

@Mixin(NBTPredicateIngredient.class)
public interface NBTPredicateIngredientAccessor {

    @Accessor(remap = false)
    ItemStack getStack();

    @Accessor(remap = false)
    NBTPredicate getPredicate();
}
