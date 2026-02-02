package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.Set;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import earth.terrarium.adastra.common.menus.PlanetsMenu;

import su.terrafirmagreg.core.common.data.utils.LaunchPositionHandler;

@Mixin(value = PlanetsMenu.class, remap = false)
@Debug(export = true)
public abstract class PlanetsMenuMixin {

    @Unique
    protected Set<CompoundTag> tfg$planetPosData;

    protected PlanetsMenuMixin(Set<CompoundTag> planetPosData) {
        this.tfg$planetPosData = planetPosData;
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    private void tfg$addToBufferedInit(int containerId, Inventory inventory, FriendlyByteBuf buf, CallbackInfo ci) {
        tfg$planetPosData = LaunchPositionHandler.getPlanetPosDataFromBuffer(buf);
        System.out.println(tfg$planetPosData);
    }
}
