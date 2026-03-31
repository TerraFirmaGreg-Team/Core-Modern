package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import dev.latvian.mods.kubejs.item.ingredient.TagContext;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;

/**
 * Resolves fluid tags via KubeJS TagContext during recipe loading, since the registry hasn't bound tags yet.
 * Remove this mixin with the next release of GT (after 7.5.2)
 */
@Mixin(value = FluidIngredient.TagValue.class, remap = false)
public class FluidIngredientTagValueMixin {

    @Shadow
    @Final
    private TagKey<Fluid> tag;

    @Inject(method = "getFluids", at = @At("HEAD"), cancellable = true)
    private void tfg$resolveFluidsFromTagContext(CallbackInfoReturnable<Collection<Fluid>> cir) {
        if (!GTCEu.Mods.isKubeJSLoaded())
            return;
        if (RecipesEventJS.instance == null)
            return;

        var holders = TagContext.INSTANCE.getValue().getTag(tag);
        ArrayList<Fluid> list = Lists.newArrayList();
        for (Holder<Fluid> holder : holders) {
            list.add(holder.value());
        }
        cir.setReturnValue(list);
    }
}
