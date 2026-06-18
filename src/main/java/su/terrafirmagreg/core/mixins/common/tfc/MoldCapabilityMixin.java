package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

@Mixin(targets = "net.dries007.tfc.common.items.MoldItem$MoldCapability", remap = false)
public abstract class MoldCapabilityMixin {

    @Shadow
    protected abstract void load();

    /**
     * @author Ujhik
     * @reason To fix ingot molds having different heat values on server and client because of a dummy initial value on
     * heatCapacity messing up with the forge Capability sync system generating inconsistencies.
     * By initializing it to the correct value, we ensure the temperature calculations stay consistent between client and server
     */
    @Inject(method = "<init>(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/tags/TagKey;)V", at = @At("TAIL"), remap = false)
    private void onInit(ItemStack stack, int capacity, TagKey<Fluid> fluidTag, CallbackInfo ci) {
        this.load();
    }
}
