package su.terrafirmagreg.core.mixins.common.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = CapabilityProvider.class)
public abstract class CapabilityProviderMixin {
    @Unique
    private static final ThreadLocal<Boolean> IN_FOOD_CHECK = ThreadLocal.withInitial(() -> false);

    @Shadow(remap = false)
    protected abstract CapabilityDispatcher getCapabilities();

    @SuppressWarnings({ "ConstantConditions", "UnstableApiUsage" })
    @Inject(method = "areCapsCompatible(Lnet/minecraftforge/common/capabilities/CapabilityProvider;)Z", at = @At("RETURN"), cancellable = true, remap = false)
    private void foodStacking(CapabilityProvider<?> other, CallbackInfoReturnable<Boolean> cir) {
        // Caps already equal
        if (cir.getReturnValue())
            return;

        // Check if it's ItemStack and cast to ItemStack
        if (!((Object) this instanceof ItemStack a))
            return;
        if (!(other instanceof ItemStack b))
            return;

        // Cheap exit
        if (!a.is(TFCTags.Items.FOODS) || !b.is(TFCTags.Items.FOODS))
            return;

        // Don't recurse
        if (IN_FOOD_CHECK.get())
            return;

        IFood foodA = FoodCapability.get(a);
        IFood foodB = FoodCapability.get(b);
        if (foodA == null || foodB == null)
            return;

        long lifetime = foodA.getRottenDate() - foodA.getCreationDate();
        long diff = Math.abs(foodA.getCreationDate() - foodB.getCreationDate());

        if (diff < lifetime / 100) {
            // Lifetime is within 1% of total lifetime
            try {
                IN_FOOD_CHECK.set(true);
                // Make copies with identical creation date to compare other caps
                final ItemStack aCopy = a.copy();
                final ItemStack bCopy = b.copy();
                final long date = Calendars.get().getTicks();
                FoodCapability.setCreationDate(aCopy, date);
                FoodCapability.setCreationDate(bCopy, date);
                CapabilityDispatcher aCopyCaps = ((CapabilityProviderMixin) (Object) aCopy).getCapabilities();
                CapabilityDispatcher bCopyCaps = ((CapabilityProviderMixin) (Object) bCopy).getCapabilities();
                if (aCopyCaps.areCompatible(bCopyCaps))
                    // Lifetime is within 1% and other caps are the same
                    cir.setReturnValue(true);
            } finally {
                IN_FOOD_CHECK.set(false);
            }
        }
    }
}
