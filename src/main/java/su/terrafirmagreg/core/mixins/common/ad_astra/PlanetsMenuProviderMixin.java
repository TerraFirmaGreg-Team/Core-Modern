package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.*;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;

import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import earth.terrarium.adastra.common.planets.AdAstraData;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;

import su.terrafirmagreg.core.common.data.screens.TFGPlanetsMenu;
import su.terrafirmagreg.core.common.data.utils.LaunchPositionHandler;

@Mixin(value = PlanetsMenuProvider.class, remap = false)
@Debug(export = true)
public class PlanetsMenuProviderMixin implements MenuConstructor {

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TFGPlanetsMenu(containerId, inventory, Set.of(), Map.of(), Object2BooleanMaps.emptyMap(), Set.of(), Set.of());
    }

    @Inject(method = "writeExtraData", at = @At("TAIL"))
    private void tfg$addNewLandingCords(ServerPlayer player, FriendlyByteBuf buffer, CallbackInfo ci) {
        System.out.println("tfg$addNewLandingCords was called");
        ServerLevel level = player.serverLevel();

        List<CompoundTag> planetsData = new ArrayList<>();
        AdAstraData.planets().forEach((dimension, planet) -> {
            Optional<CompoundTag> planetPosData = LaunchPositionHandler.getPosDataNBT(player, level, dimension);
            planetPosData.ifPresent(planetsData::add);
        });
        if (!planetsData.isEmpty()) {
            System.out.println(planetsData.size());
            planetsData.forEach(System.out::println);
            buffer.writeVarInt(planetsData.size());
            planetsData.forEach(buffer::writeNbt);
        }

        System.out.println("tfg$addNewLandingCords finished");
    }
}
