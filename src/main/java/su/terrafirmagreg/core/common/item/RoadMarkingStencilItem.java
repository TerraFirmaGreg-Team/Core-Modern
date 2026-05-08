package su.terrafirmagreg.core.common.item;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStencilPattern;

/**
 * Optional off-hand item while using GT dye spray on asphalt road / slab.
 * <p>
 * {@link AsphaltRoadStencilPattern#LINE}: same as holding nothing — orientation follows player facing.
 * {@link AsphaltRoadStencilPattern#CROSS}: paints intersection cross ({@code decal=cross}), ignores facing.
 * {@link AsphaltRoadStencilPattern#ARROW}: paints directional arrow following player facing.
 */
public class RoadMarkingStencilItem extends Item {

    private final AsphaltRoadStencilPattern pattern;

    public RoadMarkingStencilItem(Properties properties, AsphaltRoadStencilPattern pattern) {
        super(properties);
        this.pattern = pattern;
    }

    public AsphaltRoadStencilPattern pattern() {
        return pattern;
    }

    public static Optional<AsphaltRoadStencilPattern> patternFrom(ItemStack stack) {
        if (stack.getItem() instanceof RoadMarkingStencilItem stencil) {
            return Optional.of(stencil.pattern);
        }
        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.tfg.asphalt_road_stencil.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
