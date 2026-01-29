package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import earth.terrarium.adastra.common.items.vehicles.RocketItem;
import earth.terrarium.adastra.common.items.vehicles.VehicleItem;
import earth.terrarium.adastra.common.tags.ModFluidTags;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;

import su.terrafirmagreg.core.common.data.entities.rocket.RocketHelper;

@Mixin(value = RocketItem.class, remap = false)
@Debug(export = true)
public abstract class RocketItemMixin extends VehicleItem {

    @Unique
    private final RocketItem tfg$self = (RocketItem) (Object) this;

    public RocketItemMixin(Supplier<EntityType<?>> type, Properties properties) {
        super(type, properties);
    }

    @Override
    public WrappedItemFluidContainer getFluidContainer(ItemStack holder) {
        return new WrappedItemFluidContainer(holder,
                new SimpleFluidContainer(RocketHelper.ROCKET_FUEL_CAP.get(tfg$self.type()), 1,
                        (t, f) -> f.is(ModFluidTags.FUEL)));
    }
}
