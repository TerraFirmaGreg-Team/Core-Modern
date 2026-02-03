package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.api.recipe.ingredient.NBTPredicateIngredient;

import net.minecraft.network.FriendlyByteBuf;

@Mixin(NBTPredicateIngredient.Serializer.class)
public class NBTPredicateIngredientSerializerMixin {

    /**
     * Quick fix for a bug where the serializer would write the entire ingredient to NBT, rather than only the predicate.
     * @author Mqrius
     * @reason Bug in the serializer
     */
    @Overwrite(remap = false)
    public void write(FriendlyByteBuf buffer, NBTPredicateIngredient ingredient) {
        buffer.writeItem(((NBTPredicateIngredientAccessor) (Object) ingredient).getStack());
        buffer.writeUtf(((NBTPredicateIngredientAccessor) (Object) ingredient).getPredicate().toJson().toString());
    }
}
