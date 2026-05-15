package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.levelgen.Beardifier;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;

@Mixin(Beardifier.class)
public interface BeardifierAccessor {

    @Accessor("pieceIterator")
    ObjectListIterator<Beardifier.Rigid> tfg$getPieceIterator();
}
