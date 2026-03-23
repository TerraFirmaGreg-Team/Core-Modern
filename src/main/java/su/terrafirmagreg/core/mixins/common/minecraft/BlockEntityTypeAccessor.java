package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin({ BlockEntityType.class })
public interface BlockEntityTypeAccessor {
    @Accessor("validBlocks")
    Set<Block> tfg$getValidBlocks();

    @Accessor("validBlocks")
    @Mutable
    void tfg$setValidBlocks(Set<Block> var1);
}
