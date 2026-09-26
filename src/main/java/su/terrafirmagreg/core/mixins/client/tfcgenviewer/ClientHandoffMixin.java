package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket;
import com.notenoughmail.tfcgenviewer.util.ClientHandoff;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

@Mixin(value = ClientHandoff.class, remap = false)
public class ClientHandoffMixin {

    /** @ModifyArgs on ViewWorldScreen ctor can load Args$1 in a classloader that fails; filter at the handoff call. */
    @Unique
    private static final ThreadLocal<ViewerResponsePacket> TFG_TFCGEN_VIEWER_PACKET = new ThreadLocal<>();

    @ModifyArg(method = "viewWorld", at = @At(value = "INVOKE", target = "Lcom/notenoughmail/tfcgenviewer/screen/ViewWorldScreen;<init>(Ljava/util/List;JLnet/dries007/tfc/world/settings/Settings;ZZZIILjava/util/Map;Ljava/util/Map;Ljava/util/Map;)V"), index = 10, remap = false)
    private static Map<TagKey<Biome>, List<ResourceKey<Biome>>> tfg$filterBiomeTagKeysForViewWorld(
            Map<TagKey<Biome>, List<ResourceKey<Biome>>> biomeTags) {
        final ViewerResponsePacket packet = TFG_TFCGEN_VIEWER_PACKET.get();
        if (packet == null) {
            return biomeTags;
        }
        return tfg$retainTagKeysPresentInPacket(packet, biomeTags);
    }

    @Unique
    private static Map<TagKey<Biome>, List<ResourceKey<Biome>>> tfg$retainTagKeysPresentInPacket(
            ViewerResponsePacket info, Map<TagKey<Biome>, List<ResourceKey<Biome>>> biomeTags) {
        final Map<ResourceKey<Biome>, Biome> biomeInformation = info.biomeInfo();
        if (biomeInformation == null || biomeTags == null) {
            return biomeTags;
        }
        final Set<ResourceKey<Biome>> valid = biomeInformation.keySet();
        final Map<TagKey<Biome>, List<ResourceKey<Biome>>> out = new HashMap<>(biomeTags.size());
        for (var e : biomeTags.entrySet()) {
            out.put(e.getKey(), e.getValue().stream().filter(valid::contains).toList());
        }
        return out;
    }
}
