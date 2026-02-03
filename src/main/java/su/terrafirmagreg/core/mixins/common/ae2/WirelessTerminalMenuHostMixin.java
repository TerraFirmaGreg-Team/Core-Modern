package su.terrafirmagreg.core.mixins.common.ae2;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.helpers.WirelessTerminalMenuHost;

import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.compat.ae2.WirelessCardAccessor;
import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;

@Mixin(value = WirelessTerminalMenuHost.class, remap = false)
public abstract class WirelessTerminalMenuHostMixin extends ItemMenuHost implements WirelessCardAccessor {

    @Shadow
    private double currentDistanceFromGrid;

    @Shadow
    @Nullable
    private IWirelessAccessPoint myWap;

    @Shadow
    @Final
    private IGrid targetGrid;

    @Unique
    private boolean tfg$usingWirelessCard = false;

    public WirelessTerminalMenuHostMixin(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
    }

    /**
     * If we're out of range but an interplanetary wireless card is installed, use TFGAE2PowerConsumption/100 values
     * to set currentDistanceFromGrid, and use any active WAP on the grid.
     */
    @Inject(method = "rangeCheck", at = @At("RETURN"), cancellable = true)
    private void tfg$rangeCheck(CallbackInfoReturnable<Boolean> cir) {
        if (this.myWap != null) {
            this.tfg$usingWirelessCard = false;
            return;
        }

        // If card is installed and we have a grid, grab any WAP on the grid
        if (this.getUpgrades().isInstalled(TFGItems.WIRELESS_CARD.get()) && this.targetGrid != null) {
            for (var wap : this.targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                if (wap.isActive()) {
                    this.myWap = wap;
                    this.tfg$usingWirelessCard = true;

                    // Set distance dependent on dimension
                    @SuppressWarnings("resource")
                    ResourceLocation dimension = this.getPlayer().level().dimension().location();
                    double operationCost = TFGAE2PowerConsumption.powerConsumption.getOrDefault(dimension, 200000D);
                    this.currentDistanceFromGrid = operationCost / 100.0; // Magic number that feels about right

                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        this.tfg$usingWirelessCard = false;
    }

    @Override
    @Unique
    public boolean tfg$isUsingWirelessCard() {
        return this.tfg$usingWirelessCard;
    }
}
