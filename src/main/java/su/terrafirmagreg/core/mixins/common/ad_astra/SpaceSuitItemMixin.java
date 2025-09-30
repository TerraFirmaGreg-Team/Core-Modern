package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = SpaceSuitItem.class, remap = false)
public abstract class SpaceSuitItemMixin {

    @Mutable
    @Shadow
    @Final
    protected long tankSize;

    @Shadow
    public static long getOxygenAmount(Entity entity) {
        return 0;
    } //There is probably a better way of doing this (static abstract doesn't work)

    /**
     * Increases the size of the space suit's fluid tanks
     */
    @Inject(method = "<init>", at = @At(value = "TAIL", target = "Learth/terrarium/adastra/common/items/armor/SpaceSuitItem;<init>(L;L;JL;)V"), remap = false)
    private void tfg$SpaceSuitItem(ArmorMaterial material, ArmorItem.Type type, long tankSize,
            Item.Properties properties, CallbackInfo ci) {
        this.tankSize = (long) (tankSize * 2.5);
    }

    /**
     * @author Pyritie
     * @reason Change to our own fluid tags
     */
    @Overwrite()
    public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
        return new WrappedItemFluidContainer(
                holder,
                new SimpleFluidContainer(
                        FluidConstants.fromMillibuckets(tankSize),
                        1,
                        (t, f) -> f.is(TFGTags.Fluids.BreathableCompressedAir)));
    }

    /**
     * @author Bumperdo09
     * @reason Change comparison from > to >=. ALlows for space suit to consume all gas in the suit.
     * Allows for changing the type of gas used in the suit safely without needing a pressured environment
     */
    @Overwrite()
    public static boolean hasOxygen(Entity entity) {
        return getOxygenAmount(entity) >= FluidConstants.fromMillibuckets(1);
    }

}
