package com.lumintorious.tfcambiental.item.material;

import net.minecraft.world.item.ItemStack;
import com.lumintorious.tfcambiental.modifier.TempModifier;

public interface TemperatureAlteringMaterial {
    TempModifier getTempModifier(ItemStack stack);
}