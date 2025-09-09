package su.terrafirmagreg.core.common.data.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class DirtyDnaSyringeItem extends Item {
    public DirtyDnaSyringeItem(Properties props) {
        super(props);
    }

    // Dirty tooltip.
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.dirty1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.dirty2").withStyle(ChatFormatting.WHITE));
    }
}
