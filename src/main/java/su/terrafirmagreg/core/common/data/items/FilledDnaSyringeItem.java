package su.terrafirmagreg.core.common.data.items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

import su.terrafirmagreg.core.compat.starcatcher.StarcatcherFishVariants;

public class FilledDnaSyringeItem extends Item {

    private static final Map<Integer, Component> TOOLTIP_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Component> ENTITY_DISPLAY_CACHE = new ConcurrentHashMap<>();

    public FilledDnaSyringeItem(Properties props) {
        super(props);
    }

    // Tooltip showing which mob's DNA is inside.
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("mob_type")) {
            int nbtHash = stack.getTag().hashCode();
            Component cachedTooltip = TOOLTIP_CACHE.get(nbtHash);

            if (cachedTooltip != null) {
                tooltip.add(cachedTooltip);
            } else {
                Component mobTooltip = computeMobTooltip(stack);
                TOOLTIP_CACHE.put(nbtHash, mobTooltip);
                tooltip.add(mobTooltip);
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

    private Component computeMobTooltip(ItemStack stack) {
        if (stack.getTag() == null) {
            return Component.translatable("tfg.tooltip.dna_syringe.full")
                    .append(Component.empty())
                    .withStyle(ChatFormatting.GOLD);
        }

        String mobId = stack.getTag().getString("mob_type");

        // Check if this is a Starcatcher fish and use the fish item translation.
        String fishName = StarcatcherFishVariants.getFishName(stack);
        if (fishName != null) {
            Component fishDisplayName = Component.translatable("item.starcatcher." + fishName);
            return Component.translatable("tfg.tooltip.dna_syringe.full")
                    .append(fishDisplayName)
                    .withStyle(ChatFormatting.GOLD);
        } else {
            // Use entity name translation.
            Component entityName = ENTITY_DISPLAY_CACHE.computeIfAbsent(mobId, id -> {
                try {
                    ResourceLocation entityId = ResourceLocation.parse(id);
                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
                    if (type != null) {
                        return Component.translatable("entity." + id.replace(":", "."));
                    }
                } catch (Exception ignored) {
                }
                return Component.empty();
            });

            return Component.translatable("tfg.tooltip.dna_syringe.full")
                    .append(entityName)
                    .withStyle(ChatFormatting.GOLD);
        }
    }
}
