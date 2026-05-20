package su.terrafirmagreg.core.mixins.common.tfc;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.common.recipes.ingredients.DelegateIngredient;
import net.minecraft.world.item.crafting.Ingredient;

@Mixin(value = DelegateIngredient.class, remap = false)
public interface IDelegateIngredientAccessor {
    @Accessor("delegate")
    @Nullable
    Ingredient getDelegate();
}
