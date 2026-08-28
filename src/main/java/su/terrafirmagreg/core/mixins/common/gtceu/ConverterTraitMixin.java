package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
@Mixin(ConverterTrait.class)
public abstract class ConverterTraitMixin extends NotifiableEnergyContainer {

    // Allows EU <-> FE converters to automatically pull FE out of a portable energy interface

    @Shadow
    public abstract @NotNull ConverterMachine getMachine();

    @Unique
    private static final Block PORTABLE_ENERGY_INTERFACE = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("createaddition", "portable_energy_interface"));

    public ConverterTraitMixin(long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/trait/notifiable/NotifiableEnergyContainer;serverTick()V"), remap = false)
    private void tfg$tryFeExtract(CallbackInfo ci) {
        var frontFacing = getMachine().getFrontFacing();
        var thisEnergyContainer = GTCapabilityHelper.getForgeEnergy(Objects.requireNonNull(getMachine().getLevel()),
                getMachine().getBlockPos(), null);
        for (Direction d : Direction.values()) {
            if (d == frontFacing)
                continue;
            BlockState state = getMachine().getLevel().getBlockState(getMachine().getBlockPos().relative(d));
            var targetEnergyContainer = GTCapabilityHelper.getForgeEnergy(getMachine().getLevel(),
                    getMachine().getBlockPos().relative(d), null);
            if (targetEnergyContainer != null && targetEnergyContainer.canExtract() && state.is(PORTABLE_ENERGY_INTERFACE)) {
                assert thisEnergyContainer != null;
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
