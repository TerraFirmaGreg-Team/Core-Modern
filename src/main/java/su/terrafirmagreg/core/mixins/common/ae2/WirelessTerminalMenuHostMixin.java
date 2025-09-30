package su.terrafirmagreg.core.mixins.common.ae2;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.helpers.WirelessTerminalMenuHost;

import su.terrafirmagreg.core.common.data.TFGItems;

@Mixin(value = WirelessTerminalMenuHost.class, remap = false)
public abstract class WirelessTerminalMenuHostMixin extends ItemMenuHost {
    @Shadow
    private @Nullable IWirelessAccessPoint myWap;

    public WirelessTerminalMenuHostMixin(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
    }

    @Redirect(method = "checkWirelessRange", at = @At(value = "INVOKE", target = "Lappeng/helpers/WirelessTerminalMenuHost;rangeCheck()Z"))
    private boolean tfg$checkWirelessRange(WirelessTerminalMenuHost instance) {
        return (this.getUpgrades().isInstalled(TFGItems.WIRELESS_CARD.get()) && this.myWap != null)
                || !instance.rangeCheck();
    }
}
