package su.terrafirmagreg.core.common.data.items;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.compat.starcatcher.StarcatcherFishVariants;

/**
 * Item for fish roe which can store a mob id in its NBT data.
 */
public class FishRoeItem extends Item {

    private static final Map<Integer, Component> DISPLAY_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Component> ENTITY_DISPLAY_CACHE = new ConcurrentHashMap<>();

    public FishRoeItem(Properties props) {
        super(props);
    }

    // Get display name with mob type via language placeholder.
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains("mob_type")) {
            return Component.translatable("item.tfg.fish_roe", Component.empty());
        }

        int nbtHash = stack.getTag().hashCode();
        Component cachedName = DISPLAY_NAME_CACHE.get(nbtHash);
        if (cachedName != null) {
            return cachedName;
        }

        Component placeholder = computeDisplayPlaceholder(stack);
        Component result = Component.translatable("item.tfg.fish_roe", placeholder);

        DISPLAY_NAME_CACHE.put(nbtHash, result);
        return result;
    }

    private Component computeDisplayPlaceholder(ItemStack stack) {
        if (stack.getTag() == null) {
            return Component.empty();
        }

        String mobId = stack.getTag().getString("mob_type");

        // Check if this is a Starcatcher fish and use the fish item translation.
        String fishName = StarcatcherFishVariants.getFishName(stack);
        if (fishName != null) {
            return Component.translatable("item.starcatcher." + fishName);
        }

        // Ue cached entity name translation.
        if (!mobId.isEmpty()) {
            return ENTITY_DISPLAY_CACHE.computeIfAbsent(mobId, id -> {
                try {
                    ResourceLocation entityId = ResourceLocation.parse(id);
                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
                    if (type != null) {
                        return type.getDescription();
                    }
                } catch (Exception ignored) {
                }
                return Component.empty();
            });
        }

        return Component.empty();
    }

    // Display name with mob ID placeholder.
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
            @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    }
}
