package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class TFGTRepairHelper {
    private static final float DEFAULT_REPAIR_PERCENT = 0.25f;

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getInventory() instanceof CraftingContainer grid))
            return;

        boolean hasRepairKit = false;
        float repairPercent = DEFAULT_REPAIR_PERCENT;
        ItemStack toolStack = ItemStack.EMPTY;

        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack stack = grid.getItem(i);
            if (stack.isEmpty())
                continue;

            MaterialEntry entry = ChemicalHelper.getMaterialEntry(stack.getItem());
            if (entry != null && !entry.isEmpty() && entry.tagPrefix() == TFGTagPrefix.repairKit) {
                hasRepairKit = true;
                repairPercent = stack.hasTag() && stack.getTag().contains("RepairPercent")
                        ? sanitizeRepairPercent(stack.getTag().getFloat("RepairPercent"))
                        : DEFAULT_REPAIR_PERCENT;
            } else if (stack.getItem() instanceof IGTTool) {
                toolStack = stack;
            }
        }

        if (hasRepairKit && !toolStack.isEmpty()) {
            int repairAmount = (int) (toolStack.getMaxDamage() * repairPercent);
            int newDamage = Math.max(0, toolStack.getDamageValue() - repairAmount);

            if (toolStack.hasTag()) {
                event.getCrafting().setTag(toolStack.getTag().copy());
            }
            event.getCrafting().setDamageValue(newDamage);
        }
    }

    private static float sanitizeRepairPercent(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return DEFAULT_REPAIR_PERCENT;
        }
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
