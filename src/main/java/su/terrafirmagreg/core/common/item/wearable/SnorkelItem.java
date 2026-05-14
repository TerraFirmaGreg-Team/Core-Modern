package su.terrafirmagreg.core.common.item.wearable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Snorkel: water breathing while equipped in the helmet ({@link EquipmentSlot#HEAD}) slot.
 */
public class SnorkelItem extends Item {

    public static final int DEFAULT_BREATHING_DURATION_TICKS = 15 * 20;

    public SnorkelItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public static boolean shouldApplyWaterBreathing(Player player) {
        if (isInfinite()) {
            return true;
        }
        return !player.isEyeInFluid(FluidTags.WATER);
    }

    public static boolean isInfinite() {
        return false;
    }

    public static int getEffectDurationTicks(Player player) {
        int duration = DEFAULT_BREATHING_DURATION_TICKS;
        if (!isInfinite()
                && player.getItemBySlot(EquipmentSlot.HEAD).is(Items.TURTLE_HELMET)
                && !player.isEyeInFluid(FluidTags.WATER)) {
            duration += 200;
        }
        return duration + 19;
    }

    public static boolean shouldShowIcon() {
        return !isInfinite();
    }
}
