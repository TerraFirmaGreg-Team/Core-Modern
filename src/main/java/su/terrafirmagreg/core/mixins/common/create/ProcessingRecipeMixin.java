package su.terrafirmagreg.core.mixins.common.create;

import java.util.Objects;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.world.item.crafting.RecipeType;

import su.terrafirmagreg.core.TFGCore;

@Mixin(value = ProcessingRecipe.class, remap = false)
public abstract class ProcessingRecipeMixin {

    @Shadow
    private RecipeType<?> type;
    @Unique
    private static boolean tfg$hasAnnounced = false;

    @Redirect(method = "validate", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V", ordinal = 2), remap = false)
    private void tfg$validate(Logger instance, String s) {
        if (Objects.equals(type.toString(), "rolling") && !tfg$hasAnnounced) {
            tfg$hasAnnounced = true;
            TFGCore.LOGGER.info("Supressed CreateAdditions rolling recipe log spam! Enjoy!");
        }

    }

}
