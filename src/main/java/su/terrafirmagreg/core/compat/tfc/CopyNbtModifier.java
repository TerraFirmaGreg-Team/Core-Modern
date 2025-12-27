package su.terrafirmagreg.core.compat.tfc.modifier;

import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.minecraft.world.item.ItemStack;

public enum CopyNbtModifier implements ItemStackModifier.SingleInstance<CopyNbtModifier> {
    INSTANCE;

    // Copy all NBT data from input to stack
    @Override
    public ItemStack apply(ItemStack stack, ItemStack input) {
        if (input.hasTag()) {
            stack.setTag(input.getTag().copy());
        }
        return stack;
    }

    @Override
    public CopyNbtModifier instance() {
        return INSTANCE;
    }

    @Override
    public boolean dependsOnInput() {
        return true;
    }
}
