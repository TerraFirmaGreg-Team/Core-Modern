package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Camel.class)
public interface CamelAccessor {
    @Invoker("getBodyAnchorAnimationYOffset")
    double invoke$getBodyAnchorAnimationYOffset(boolean firstPassenger, float partialTick);
}
