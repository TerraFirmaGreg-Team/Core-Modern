package su.terrafirmagreg.core.mixins.common.createliquidfuel;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.forsteri.createliquidfuel.core.LiquidBurnerFuelJsonLoader;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import su.terrafirmagreg.core.network.packet.FuelSyncPacket;

/**
 * Capture the finalized blaze burner liquid fuel map to send to the client and populate emi.
 */
@Mixin(value = LiquidBurnerFuelJsonLoader.class, remap = false)
public class LiquidBurnerFuelJsonLoaderMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void tfg$syncFuelsToClient(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
        FuelSyncPacket.capturedJsonData = new HashMap<>(map);
    }
}
