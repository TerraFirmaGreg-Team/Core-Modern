package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import earth.terrarium.adastra.common.planets.AdAstraData;

import su.terrafirmagreg.core.common.data.utils.LaunchPositionHandler;

@Mixin(value = PlanetsMenuProvider.class, remap = false)
public class PlanetsMenuProviderMixin {

    @Inject(method = "writeExtraData", at = @At("TAIL"))
    private void tfg$addNewLandingCords(ServerPlayer player, FriendlyByteBuf buffer, CallbackInfo ci) {
        ServerLevel level = player.serverLevel();

        List<CompoundTag> planetsData = new ArrayList<>();
        AdAstraData.planets().forEach((dimension, planet) -> {
            Optional<CompoundTag> planetPosData = LaunchPositionHandler.getPosDataNBT(player, level, dimension);
            planetPosData.ifPresent(planetsData::add);
        });
        if (!planetsData.isEmpty()) {
            buffer.writeInt(planetsData.size());
            planetsData.forEach(buffer::writeNbt);
        }

    }
}
