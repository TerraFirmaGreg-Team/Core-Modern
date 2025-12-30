package su.terrafirmagreg.core.mixins.common.grappling_hook;

import org.spongepowered.asm.mixin.Mixin;

import com.yyon.grapplinghook.items.GrapplehookItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Make GrapplehookItem unrepairable through vanilla Minecraft repairs

@Mixin(value = GrapplehookItem.class, remap = false)
public abstract class GrapplehookItemMixin extends Item {

    // Makes the compiler happy
    private GrapplehookItemMixin() {
        super(null);
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
