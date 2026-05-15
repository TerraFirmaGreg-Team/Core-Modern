package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;

@Mixin(PoiSection.class)
public interface PoiSectionAccessor {
    @Accessor("byType")
    Map<Holder<PoiType>, Set<PoiRecord>> accessor$byType();
}
