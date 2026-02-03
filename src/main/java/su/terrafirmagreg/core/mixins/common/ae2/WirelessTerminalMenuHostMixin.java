package su.terrafirmagreg.core.mixins.common.ae2;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.helpers.WirelessTerminalMenuHost;

import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;

@Mixin(value = WirelessTerminalMenuHost.class, remap = false)
public abstract class WirelessTerminalMenuHostMixin extends ItemMenuHost {

    @Shadow
    private double currentDistanceFromGrid;

    public WirelessTerminalMenuHostMixin(Player player, @Nullable Integer slot, ItemStack itemStack) {
        super(player, slot, itemStack);
    }

    /**
     * If we're out of range but an interplanetary wireless card is installed, use TFGAE2PowerConsumption/100 values
     * to set currentDistanceFromGrid. This sets a high idle power drain dependent on dimension.
     */
    @Redirect(method = "checkWirelessRange", at = @At(value = "INVOKE", target = "Lappeng/helpers/WirelessTerminalMenuHost;rangeCheck()Z"))
    private boolean tfg$checkWirelessRange(WirelessTerminalMenuHost instance) {
        boolean inRange = instance.rangeCheck();

        if (inRange) {
            return true;
        }

        if (this.getUpgrades().isInstalled(TFGItems.WIRELESS_CARD.get())) {
            ResourceLocation dimension = this.getPlayer().level().dimension().location();
            double operationCost = TFGAE2PowerConsumption.powerConsumption.getOrDefault(dimension, 10000D);
            this.currentDistanceFromGrid = operationCost / 100.0; // Magic number that feels about right
            return true;
        }

        return false;
    }
}
