package su.terrafirmagreg.core.mixins.common.create;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import net.minecraft.world.item.ItemStack;

@Mixin(value = PackagerLinkBlockEntity.class, remap = false)
public abstract class PackagerLinkBlockEntityMixin {

    @Redirect(method = "processRequest", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"), remap = false)
    private int tfg$allowGregTechToolRequests(InventorySummary summary, ItemStack stack) {
        int exactCount = summary.getCountOf(stack);
        if (exactCount > 0 || !(stack.getItem() instanceof IGTTool))
            return exactCount;

        return summary.getTotalOfMatching(other -> ItemStack.isSameItem(other, stack));
    }

    @WrapOperation(method = "processRequest", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/packager/PackagingRequest;create(Lnet/minecraft/world/item/ItemStack;ILjava/lang/String;ILorg/apache/commons/lang3/mutable/MutableBoolean;IILcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;)Lcom/simibubi/create/content/logistics/packager/PackagingRequest;"), remap = false)
    private com.simibubi.create.content.logistics.packager.PackagingRequest tfg$useMatchedGregTechToolStack(
            ItemStack stack,
            int count,
            String address,
            int linkIndex,
            MutableBoolean finalLink,
            int packageCount,
            int orderId,
            PackageOrderWithCrafts context,
            Operation<com.simibubi.create.content.logistics.packager.PackagingRequest> original,
            @Local InventorySummary summary) {
        if (stack.getItem() instanceof IGTTool) {
            ItemStack matchedToolStack = ItemStack.EMPTY;
            for (BigItemStack entry : summary.getStacks()) {
                if (ItemStack.isSameItem(entry.stack, stack)) {
                    matchedToolStack = entry.stack.copyWithCount(1);
                    break;
                }
            }

            if (!matchedToolStack.isEmpty())
                stack = matchedToolStack;
        }

        return original.call(stack, count, address, linkIndex, finalLink, packageCount, orderId, context);
    }

}
