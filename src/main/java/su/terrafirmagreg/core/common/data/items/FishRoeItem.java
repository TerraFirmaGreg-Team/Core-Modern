package su.terrafirmagreg.core.common.data.items;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.StarcatcherFishVariants;

/**
 * Item for fish roe which can store a mob id in its NBT data.
 */
public class FishRoeItem extends Item {
    public FishRoeItem(Properties props) {
        super(props);
    }

    // Get display name with mob type via language placeholder.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Component placeholder = Component.empty();

        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("mob_type")) {
            String mobId = stack.getTag().getString("mob_type");

            // Check if this is a Starcatcher fish and use the fish item translation.
            String fishName = StarcatcherFishVariants.getFishName(stack);
            if (fishName != null) {
                placeholder = Component.translatable("item.starcatcher." + fishName);
            } else {
                if (!mobId.isEmpty()) {
                    ResourceLocation itemId = ResourceLocation.parse(mobId);
                    Item item = ForgeRegistries.ITEMS.getValue(itemId);

                    if (item != null && item != Items.AIR) {
                        ItemStack itemStack = new ItemStack(item);
                        placeholder = itemStack.getDisplayName();
                    } else {
                        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(itemId);
                        if (type != null) {
                            placeholder = type.getDescription();
                        }
                    }
                }
            }
        }

        return Component.translatable("item.tfg.fish_roe", placeholder);
    }

    // Display name with mob ID placeholder.
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
            @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    }
}
