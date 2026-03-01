package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ConverterTrait.class)
public abstract class ConverterTraitMixin extends NotifiableEnergyContainer {
    @Unique
    private static final Block PORTABLE_ENERGY_INTERFACE = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("createaddition", "portable_energy_interface"));

    public ConverterTraitMixin(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableEnergyContainer;serverTick()V"), remap = false)
    private void tfg$tryFeExtract(CallbackInfo ci) {
        var frontFacing = machine.getFrontFacing();
        var thisEnergyContainer = GTCapabilityHelper.getForgeEnergy(machine.getLevel(),
                machine.getPos(), null);
        for (Direction d : Direction.values()) {
            if (d == frontFacing)
                continue;
            BlockState state = machine.getLevel().getBlockState(machine.getPos().relative(d));
            var targetEnergyContainer = GTCapabilityHelper.getForgeEnergy(machine.getLevel(),
                    machine.getPos().relative(d), null);
            if (targetEnergyContainer != null && targetEnergyContainer.canExtract() && state.is(PORTABLE_ENERGY_INTERFACE)) {
                int energyExtracted = targetEnergyContainer.extractEnergy(
                        thisEnergyContainer.receiveEnergy(
                                FeCompat.toFe(
                                        getEnergyCapacity() - getEnergyStored(), FeCompat.ratio(false)),
                                true),
                        false);
                thisEnergyContainer.receiveEnergy(energyExtracted, false);
            }
        }
    }
}
