package su.terrafirmagreg.core.common.data.items;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FilledDnaSyringeItem extends Item {
    public FilledDnaSyringeItem(Properties props) {
        super(props);
    }

    // Tooltip showing which mob's DNA is inside.
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("mob_type")) {
            String mobId = stack.getTag().getString("mob_type");
            ResourceLocation rl = new ResourceLocation(mobId);
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);

            if (type != null) {
                tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.full")
                        .append(Component.translatable("entity." + mobId.replace(":", ".")))
                        .withStyle(ChatFormatting.GOLD));
            }
        } else {
            tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.empty")
                    .withStyle(ChatFormatting.GRAY));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tfg.tooltip.dna_syringe.explain")
                    .withStyle(ChatFormatting.WHITE));
        } else {
            tooltip.add(Component.translatable("tfg.tooltip.shift_hint")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        }
    }
}
