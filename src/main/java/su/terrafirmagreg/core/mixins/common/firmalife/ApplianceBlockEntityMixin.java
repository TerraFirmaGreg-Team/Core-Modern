package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.eerussianguy.firmalife.common.blockentities.ApplianceBlockEntity;

import net.dries007.tfc.common.blockentities.IHeatable;

@Mixin(value = ApplianceBlockEntity.class, remap = false)
public abstract class ApplianceBlockEntityMixin implements IHeatable {

    @Shadow
    public abstract float getTemperature();
}
