package su.terrafirmagreg.core.mixins.common.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.player.Player;

import appeng.items.tools.powered.WirelessTerminalItem;

import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;

@Mixin(value = WirelessTerminalItem.class, remap = false)
public class WirelessTerminalItemMixin {
    @ModifyVariable(method = "usePower", at = @At("HEAD"), argsOnly = true, index = 2)
    private double tfg$usePower(double value, @Local(argsOnly = true) Player player) {
        return value * TFGAE2PowerConsumption.powerConsumption.getOrDefault(player.level().dimension().location(), 1D);
    }
}
