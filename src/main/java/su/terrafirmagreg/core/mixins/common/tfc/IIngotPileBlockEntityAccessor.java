package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.common.blockentities.IngotPileBlockEntity;

@Mixin(value = IngotPileBlockEntity.class, remap = false)
public interface IIngotPileBlockEntityAccessor {

    @Accessor
    List<?> getEntries();
}
