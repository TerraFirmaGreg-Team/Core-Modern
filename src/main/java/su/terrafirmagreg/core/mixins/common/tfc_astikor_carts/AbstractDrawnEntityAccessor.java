package su.terrafirmagreg.core.mixins.common.tfc_astikor_carts;

import de.mennomax.astikorcarts.entity.AbstractDrawnEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractDrawnEntity.class, remap = false)
public interface AbstractDrawnEntityAccessor {
    @Accessor
    Entity getPulling();
}
