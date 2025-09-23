package su.terrafirmagreg.core.common.data.items;

import org.jetbrains.annotations.Nullable;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class PiglinDisguise extends BlockItem {

    public PiglinDisguise(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, @Nullable LivingEntity wearer) {
        return stack.is(ItemTags.PIGLIN_LOVED);
    }
}
