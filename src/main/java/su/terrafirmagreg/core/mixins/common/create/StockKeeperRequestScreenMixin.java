package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;

import net.minecraft.world.item.ItemStack;

@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperRequestScreenMixin {

    @Redirect(method = "revalidateOrders", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"), remap = false)
    private int tfg$getCountDamageableTools(InventorySummary summary, ItemStack stack) {
        int exactCount = summary.getCountOf(stack);
        if (exactCount > 0 || !stack.isDamageableItem())
            return exactCount;

        if (summary.getTotalOfMatching(other -> ItemStack.isSameItem(other, stack)) > 0)
            return 1;

        return 0;
    }

}
