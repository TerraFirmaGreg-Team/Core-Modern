package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.animal.camel.Camel;

@Mixin(value = Camel.class)
public interface CamelAccessor {
    @Invoker("getBodyAnchorAnimationYOffset")
    double invoke$getBodyAnchorAnimationYOffset(boolean firstPassenger, float partialTick);
}
