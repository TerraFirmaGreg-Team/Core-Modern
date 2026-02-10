package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeBridge;

@Mixin(Biome.class)
public abstract class BiomeMixin implements IBiomeBridge {

    // null = not initialized yet
    // Optional.empty() = no extension
    // Optional.of() = extension
    @Unique
    private @Nullable Optional<BiomeExtension> tfg$cachedExtension;

    @Nullable
    @Override
    @SuppressWarnings("OptionalAssignedToNull")
    public BiomeExtension tfg$getExtension(@NotNull Supplier<BiomeExtension> init) {
        if (tfg$cachedExtension == null) {
            tfg$cachedExtension = Optional.ofNullable(init.get());
        }
        return tfg$cachedExtension.orElse(null);
    }
}
