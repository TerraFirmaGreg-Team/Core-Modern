package su.terrafirmagreg.core.common.data.items;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class ProgenitorCellsItem extends Item {
    public ProgenitorCellsItem(Properties props) {
        super(props);
    }

    // Tooltip showing which mob's DNA is inside.
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("mob_type")) {
            String mobId = stack.getTag().getString("mob_type");
            ResourceLocation rl = ResourceLocation.parse(mobId);
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);

            if (type != null) {
                tooltip.add(Component.translatable("tfg.tooltip.proginator_cells.mob")
                        .append(Component.translatable("entity." + mobId.replace(":", ".")))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
