package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.LerpFloatLayer;

@Mixin(value = ChunkData.class, remap = false)
public interface ChunkDataAccessor {
    @Accessor("rainfallLayer")
    LerpFloatLayer tfg$getRainfallLayer();

    @Accessor("rainfallLayer")
    void tfg$setRainfallLayer(LerpFloatLayer layer);
}
