package su.terrafirmagreg.core.common.block.girder;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;

/***
 * Credit: Create: More Girders
 */
public interface TFGGirderData {
    @Nullable
    Block tfg$getGirderBlock();

    void tfg$setGirderBlock(@Nullable Block block);
}
