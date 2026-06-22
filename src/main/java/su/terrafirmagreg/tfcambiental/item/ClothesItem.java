package su.terrafirmagreg.tfcambiental.item;

import net.minecraft.world.item.*;

public class ClothesItem extends ArmorItem {
    public ClothesItem(ArmorMaterial material, ArmorItem.Type type, Properties pProperties) {
        super(material, type, pProperties);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getDurabilityForType(getType());
    }
}
