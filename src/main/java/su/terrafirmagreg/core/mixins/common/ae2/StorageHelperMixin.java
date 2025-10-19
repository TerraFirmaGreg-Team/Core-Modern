package su.terrafirmagreg.core.mixins.common.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.StorageHelper;
import appeng.helpers.WirelessTerminalMenuHost;

import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;

@Mixin(value = StorageHelper.class, remap = false)
public abstract class StorageHelperMixin {

    /**
     * If we're in range of the access point,
     * it doesn't matter if we have an upgrade or not, the consumption will stay the same.
     * If we're out of range, we'll use the tweaked consumption rate.
     */
    @ModifyArg(method = "poweredInsert(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;Lappeng/api/config/Actionable;)J", at = @At(value = "INVOKE", target = "Lappeng/api/networking/energy/IEnergySource;extractAEPower(DLappeng/api/config/Actionable;Lappeng/api/config/PowerMultiplier;)D", ordinal = 1))
    private static double tfg$poweredInsert(double original, @Local(argsOnly = true) IEnergySource energySource) {
        if (energySource instanceof WirelessTerminalMenuHost menuHost) {
            return menuHost.rangeCheck() ? original : original * TFGAE2PowerConsumption.powerConsumption.getOrDefault(menuHost.getPlayer().level().dimension().location(), 1D);
        }
        return original;
    }

    /**
     * See {@link #tfg$poweredInsert(double, IEnergySource)}
     */
    @ModifyArg(method = "poweredExtraction(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;Lappeng/api/config/Actionable;)J", at = @At(value = "INVOKE", target = "Lappeng/api/networking/energy/IEnergySource;extractAEPower(DLappeng/api/config/Actionable;Lappeng/api/config/PowerMultiplier;)D", ordinal = 1))
    private static double tfg$poweredExtraction(double original, @Local(argsOnly = true) IEnergySource energySource) {
        if (energySource instanceof WirelessTerminalMenuHost menuHost) {
            return menuHost.rangeCheck() ? original : original * TFGAE2PowerConsumption.powerConsumption.getOrDefault(menuHost.getPlayer().level().dimension().location(), 1D);
        }
        return original;
    }
}
