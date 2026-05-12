package su.terrafirmagreg.core.common.block.asphalt;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import tfchotornot.common.HNTags;

import su.terrafirmagreg.core.common.data.TFGTags;

public class HotAsphaltRoadBlockItem extends BlockItem {

    public HotAsphaltRoadBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide() || !(entity instanceof Player player) ||
                player.tickCount % AsphaltRoadHelper.HEAT_DAMAGE_INTERVAL_TICKS != 0L) {
            return;
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.is(HNTags.Items.INSULATING)) {
            ToolHelper.damageItem(offhand, player);
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(TFGTags.Items.HotProtectionEquipment)) {
            return;
        }

        player.hurt(GTDamageTypes.HEAT.source(level), AsphaltRoadHelper.HOT_ITEM_DAMAGE);
    }
}
