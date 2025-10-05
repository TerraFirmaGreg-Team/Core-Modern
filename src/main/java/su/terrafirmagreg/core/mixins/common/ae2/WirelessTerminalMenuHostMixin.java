package su.terrafirmagreg.core.mixins.common.ae2;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.helpers.WirelessTerminalMenuHost;

import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;

@Mixin(value = WirelessTerminalMenuHost.class, remap = false)
public abstract class WirelessTerminalMenuHostMixin extends ItemMenuHost {
    @Shadow
    private @Nullable IWirelessAccessPoint myWap;

    @Shadow
    public abstract boolean rangeCheck();

    public WirelessTerminalMenuHostMixin(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
    }

    /**
     * In english, this basically checks if the terminal has the wireless card installed and if the access point exists.
     * If that check fails, we check if we're out of range.
     */
    @Redirect(method = "checkWirelessRange", at = @At(value = "INVOKE", target = "Lappeng/helpers/WirelessTerminalMenuHost;rangeCheck()Z"))
    private boolean tfg$checkWirelessRange(WirelessTerminalMenuHost instance) {
        return (this.getUpgrades().isInstalled(TFGItems.WIRELESS_CARD.get()) && this.myWap != null)
                || instance.rangeCheck();
    }

    /**
     * If we're in range of the access point,
     * it doesn't matter if we have an upgrade or not, the consumption will stay the same.
     * If we're out of range, we'll use the tweaked consumption rate.
     */
    @ModifyArg(method = "extractAEPower", at = @At(value = "INVOKE", target = "Lappeng/items/tools/powered/WirelessTerminalItem;usePower(Lnet/minecraft/world/entity/player/Player;DLnet/minecraft/world/item/ItemStack;)Z"), index = 1)
    private double tfg$extractAEPower(double amount) {
        return this.rangeCheck() ? amount : amount * TFGAE2PowerConsumption.powerConsumption.getOrDefault(this.getPlayer().level().dimension().location(), 1D);
    }
}
