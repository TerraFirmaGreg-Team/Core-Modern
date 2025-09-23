package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.electric.FisherMachine;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = FisherMachine.class, remap = false)
public abstract class FisherMachineMixin {

    /**
     * Разрешает рыболовство на реках и в океанах.
     */
    @Redirect(method = "updateHasWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/world/level/material/Fluid;)Z"), remap = true)
    private boolean tfg$updateHasWater$fluidState$is(FluidState instance, Fluid fluid) {
        return instance.is(Fluids.WATER) || instance.is(TFCFluids.RIVER_WATER.get())
                || instance.is(TFCFluids.SALT_WATER.source().get());
    }

    /**
     * Разрешает класть любые нитки с тегом forge:string в рыболов
     */
    @Redirect(method = "updateFishingUpdateSubscription", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), remap = true)
    private boolean tfg$updateFishingUpdateSubscription$itemStack$is(ItemStack instance, Item item) {
        return instance.is(TFGTags.Items.Strings);
    }

    /**
     * Разрешает класть любые нитки с тегом forge:string в рыболов
     */
    @Redirect(method = "createBaitItemHandler", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableItemStackHandler;setFilter(Ljava/util/function/Predicate;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableItemStackHandler;"), remap = false)
    private NotifiableItemStackHandler tfg$createBaitItemHandler$notifiableItemStackHandler$setFilter(
            NotifiableItemStackHandler instance, Predicate<ItemStack> filter) {
        return instance.setFilter((item) -> item.is(TFGTags.Items.Strings));
    }
}
