package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.AbstractArrow;

/**
 * Accessor for AbstractArrow class.
 */
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {

    @Accessor("inGround")
    boolean is$inGround();
}
