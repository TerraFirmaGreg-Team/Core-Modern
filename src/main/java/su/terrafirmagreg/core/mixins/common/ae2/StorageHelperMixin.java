package su.terrafirmagreg.core.mixins.common.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.resources.ResourceLocation;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.StorageHelper;

import su.terrafirmagreg.core.compat.ae2.WirelessCardAccessor;
import su.terrafirmagreg.core.compat.kjs.events.TFGAE2PowerConsumption;

@Mixin(value = StorageHelper.class, remap = false)
public abstract class StorageHelperMixin {

    /**
     * Applies dimension-based power multiplier for item transfers when the
     * Interplanetary Wireless Card is actively being used (out of normal range).
     */
    @ModifyArg(method = "poweredInsert(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;Lappeng/api/config/Actionable;)J", at = @At(value = "INVOKE", target = "Lappeng/api/networking/energy/IEnergySource;extractAEPower(DLappeng/api/config/Actionable;Lappeng/api/config/PowerMultiplier;)D", ordinal = 1))
    private static double tfg$poweredInsert(double original, @Local(argsOnly = true) IEnergySource energySource) {
        if (energySource instanceof WirelessCardAccessor accessor && accessor.tfg$isUsingWirelessCard()) {
            if (energySource instanceof appeng.helpers.WirelessTerminalMenuHost menuHost) {
                @SuppressWarnings("resource")
                ResourceLocation dimension = menuHost.getPlayer().level().dimension().location();
                return original * TFGAE2PowerConsumption.powerConsumption.getOrDefault(dimension, 200000D);
            }
        }
        return original;
    }

    /**
     * See {@link #tfg$poweredInsert(double, IEnergySource)}
     */
    @ModifyArg(method = "poweredExtraction(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/storage/MEStorage;Lappeng/api/stacks/AEKey;JLappeng/api/networking/security/IActionSource;Lappeng/api/config/Actionable;)J", at = @At(value = "INVOKE", target = "Lappeng/api/networking/energy/IEnergySource;extractAEPower(DLappeng/api/config/Actionable;Lappeng/api/config/PowerMultiplier;)D", ordinal = 1))
    private static double tfg$poweredExtraction(double original, @Local(argsOnly = true) IEnergySource energySource) {
        if (energySource instanceof WirelessCardAccessor accessor && accessor.tfg$isUsingWirelessCard()) {
            if (energySource instanceof appeng.helpers.WirelessTerminalMenuHost menuHost) {
                @SuppressWarnings("resource")
                ResourceLocation dimension = menuHost.getPlayer().level().dimension().location();
                return original * TFGAE2PowerConsumption.powerConsumption.getOrDefault(dimension, 200000D);
            }
        }
        return original;
    }
}
