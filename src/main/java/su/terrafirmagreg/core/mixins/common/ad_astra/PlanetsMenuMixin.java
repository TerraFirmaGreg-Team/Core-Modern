package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import earth.terrarium.adastra.common.menus.PlanetsMenu;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import su.terrafirmagreg.core.common.data.utils.LaunchPositionHandler;

@Mixin(value = PlanetsMenu.class, remap = false)
@Debug(export = true)
public abstract class PlanetsMenuMixin {

    @Mutable
    @Unique
    @Final
    protected Set<CompoundTag> tfg$planetPosData;
    @Unique
    private FriendlyByteBuf tfg$tempBuf;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    private void tfg$grabBuf(int containerId, Inventory inventory, FriendlyByteBuf buf, CallbackInfo ci) {
        System.out.println(buf);
        tfg$tempBuf = buf;
        System.out.println(tfg$tempBuf);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Ljava/util/Set;Ljava/util/Map;Lit/unimi/dsi/fastutil/objects/Object2BooleanMap;Ljava/util/Set;)V", at = @At("TAIL"))
    private void tfg$addPlanetPosData(int containerId, Inventory inventory, Set disabledPlanets, Map spaceStations, Object2BooleanMap claimedChunks, Set spawnLocations, CallbackInfo ci) {
        System.out.println(tfg$tempBuf);
        tfg$planetPosData = LaunchPositionHandler.getPlanetPosDataFromBuffer(tfg$tempBuf);
        System.out.println(tfg$planetPosData.stream().map(CompoundTag::getAsString));
    }
}
