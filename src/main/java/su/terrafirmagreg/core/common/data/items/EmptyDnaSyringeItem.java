package su.terrafirmagreg.core.common.data.items;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class EmptyDnaSyringeItem extends Item {
    public EmptyDnaSyringeItem(Properties props) {
        super(props);
    }

    // Empty tooltip
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip,
            @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.empty").withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.explain").withStyle(ChatFormatting.WHITE));
        } else {
            tooltip.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.YELLOW,
                    ChatFormatting.ITALIC));
        }
    }
}
