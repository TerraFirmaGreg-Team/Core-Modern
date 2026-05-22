package su.terrafirmagreg.core.common.block.girder;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;

public interface TFGGirderData {
    @Nullable
    Block tfg$getGirderBlock();

    void tfg$setGirderBlock(@Nullable Block block);
}
