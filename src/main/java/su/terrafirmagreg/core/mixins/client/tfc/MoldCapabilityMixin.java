package su.terrafirmagreg.core.mixins.client.tfc;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.HeatHandler;
import net.dries007.tfc.util.Metal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

@Mixin(targets = "net.dries007.tfc.common.items.MoldItem$MoldCapability", remap = false)
public class MoldCapabilityMixin {
    @Final
    @Shadow
    private FluidTank tank;

    @Final
    @Shadow
    private HeatHandler heat;

    @Shadow
    private boolean initialized;

    /**
     * @author Ujhik
     * @reason To fix ingot molds having different heat values on server and client because of a dummy initial value on heatCapacity messing up with the forge Capability sync system generating inconsistencies. By initializing it to the correct value, we ensure the temperature calculations stay consistent between client and server
     */
    @Inject(method = "<init>(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/tags/TagKey;)V", at = @At("TAIL"), remap = false)
    private void onInit(ItemStack stack, int capacity, TagKey<Fluid> fluidTag, CallbackInfo ci) {
        final CompoundTag tankTag = stack.getTagElement("tank");
        if (tankTag != null) {
            tank.readFromNBT(tankTag);
            initialized = true;
        }

        // recalculate heat capacity based on actual fluid contents
        final FluidStack fluid = tank.getFluid();
        final Metal metal = Metal.get(fluid.getFluid());
        float value = HeatCapability.POTTERY_HEAT_CAPACITY;
        if (!fluid.isEmpty() && metal != null) {
            value += metal.getHeatCapacity(fluid.getAmount());
        }
        heat.setHeatCapacity(value);
    }
}
