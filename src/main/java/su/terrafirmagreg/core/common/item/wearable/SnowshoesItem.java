package su.terrafirmagreg.core.common.item.wearable;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Snowshoes: powder-snow walking when worn in boots + reduced block slowdown on
 * {@link su.terrafirmagreg.core.common.data.TFGTags.Blocks#NOT_SLOWED_WITH_SNOWSHOES}.
 */
public class SnowshoesItem extends Item {

    public static final UUID STEP_HEIGHT_MODIFIER_UUID = UUID.fromString("8f3c2b1a-6d5e-4c3b-9f2a-1e0d8c7b6a50");
    public static final String STEP_HEIGHT_MODIFIER_NAME = "tfg:snowshoes_step_height";
    public static final double STEP_HEIGHT_ADDITION_ON_SNOW = 0.5D;

    public SnowshoesItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.FEET;
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return wearer.getItemBySlot(EquipmentSlot.FEET).is(stack.getItem());
    }
}
