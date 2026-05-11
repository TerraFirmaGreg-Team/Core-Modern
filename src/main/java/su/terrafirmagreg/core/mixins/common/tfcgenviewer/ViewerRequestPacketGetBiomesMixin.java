package su.terrafirmagreg.core.mixins.common.tfcgenviewer;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.notenoughmail.tfcgenviewer.network.packets.ViewerRequestPacket;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

@Mixin(value = ViewerRequestPacket.class, remap = false)
public class ViewerRequestPacketGetBiomesMixin {

    /**
     * Vanilla TFCGenViewer packs only IDs from {@link net.dries007.tfc.world.biome.TFCBiomes}. TFG adds
     * {@code tfg:earth/...} biomes ({@link TFGBiomes}) that must be present client-side when
     * {@link com.notenoughmail.tfcgenviewer.color.FeatureColors#prime} builds per-biome climate/feature
     * maps — otherwise lookups for reef / newgen keys miss and previews fall back as if oldgen.
     */
    @WrapMethod(method = "getBiomes", remap = false)
    private static Map<ResourceKey<Biome>, Biome> tfg$mergeTfgEarthBiomesIntoViewerPacket(
            RegistryAccess registryAccess,
            Operation<Map<ResourceKey<Biome>, Biome>> original) {
        final Map<ResourceKey<Biome>, Biome> base = original.call(registryAccess);
        final Map<ResourceKey<Biome>, Biome> merged = new HashMap<>(base);
        final Registry<Biome> biomes = registryAccess.registryOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> key : TFGBiomes.getAllKeys()) {
            if (!merged.containsKey(key)) {
                biomes.getHolder(key).ifPresent(h -> merged.put(key, h.value()));
            }
        }
        return Map.copyOf(merged);
    }
}
