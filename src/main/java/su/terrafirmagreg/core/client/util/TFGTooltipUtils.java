package su.terrafirmagreg.core.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

/**
 * Utility class for handling multi-line tooltips.
 */
public class TFGTooltipUtils {

    /**
     * Splits a component into multiple components based on line breaks (\r).
     * @param tooltip The component to normalize.
     * @return A list of components, each representing a line.
     */
    @NotNull
    public static List<Component> normalize(@Nullable Component tooltip) {
        if (tooltip == null) {
            return Collections.emptyList();
        }

        final String[] lines = tooltip.getString().split("\\R", -1);
        if (lines.length <= 1) {
            return List.of(tooltip);
        }

        final List<Component> components = new ArrayList<>(lines.length);
        for (String line : lines) {
            components.add(Component.literal(line).withStyle(tooltip.getStyle()));
        }
        return components;
    }

    /**
     * Normalizes a list of components, splitting any that contain line breaks.
     * @param tooltip The list of components to normalize.
     * @return A normalized list of components.
     */
    @NotNull
    public static List<Component> normalize(@Nullable List<Component> tooltip) {
        if (tooltip == null || tooltip.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Component> components = new ArrayList<>();
        for (Component component : tooltip) {
            components.addAll(normalize(component));
        }
        return components;
    }
}
