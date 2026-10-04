package su.terrafirmagreg.core.mixins.common.forge;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import mod.traister101.sns.common.items.ContainerItem;

/** Overrides the check whether two items are equal to work with gt tools and sns sacks, this is required for create logistics. */
@Mixin(value = ItemHandlerHelper.class)
public abstract class ItemHandlerHelperMixin {
    @Inject(method = "canItemStacksStack(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$relaxedStacking(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir) {
        if (!(a.getItem() instanceof IGTTool || a.getItem() instanceof ContainerItem))
            return;
        if (a.isEmpty() || b.isEmpty() || a.getItem() != b.getItem() || a.getMaxStackSize() != 1 || b.getMaxStackSize() != 1
                || a.getDamageValue() != b.getDamageValue() || !a.areCapsCompatible(b) || !Objects.equals(a.getEnchantmentTags(), b.getEnchantmentTags()))
            return;

        // GT.Tool is lazily generated, we want to only check durability and behaviors (e.g. drill range)
        if (a.getItem() instanceof IGTTool) {
            CompoundTag ta = a.getTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);
            CompoundTag tb = b.getTagElement(ToolHelper.BEHAVIOURS_TAG_KEY);
            if (((ta == null || ta.isEmpty()) && (tb == null || tb.isEmpty())) || Objects.equals(ta, tb))
                cir.setReturnValue(true);
        }

        // SNS Sacks send data in the share tag but not in the main nbt data
        if (a.getItem() instanceof ContainerItem && Objects.equals(a.getShareTag(), b.getShareTag()))
            cir.setReturnValue(true);
    }
}
