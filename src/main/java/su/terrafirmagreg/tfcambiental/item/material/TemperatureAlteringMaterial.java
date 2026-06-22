package su.terrafirmagreg.tfcambiental.item.material;

import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public interface TemperatureAlteringMaterial {
    TempModifier getTempModifier(ItemStack stack);
}
