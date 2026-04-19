package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.chunk.storage.SectionStorage;

@Mixin(SectionStorage.class)
public interface SectionStorageAccessor<R> {
    @Invoker(value = "getOrLoad")
    Optional<R> invoke$getOrLoad(long sectionKey);
}
