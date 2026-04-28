package su.terrafirmagreg.core.common.item.wearable;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Flippers: swim speed bonus while equipped in the boots ({@link EquipmentSlot#FEET}) slot.
 * Modifier is applied by {@link su.terrafirmagreg.core.common.event.TFGWearableAccessoryHandler}.
 */
public class FlippersItem extends Item {

    public static final UUID SWIM_SPEED_MODIFIER_UUID = UUID.fromString("83f4e257-cd5c-4a36-ba4b-c052422ce7cf");
    public static final String SWIM_SPEED_MODIFIER_NAME = "tfg:flippers_swim_speed_bonus";
    public static final double DEFAULT_SWIM_SPEED_ADDITION = 1.0D;

    public FlippersItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.FEET;
    }
}
