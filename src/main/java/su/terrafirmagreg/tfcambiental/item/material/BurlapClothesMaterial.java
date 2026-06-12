package su.terrafirmagreg.tfcambiental.item.material;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public class BurlapClothesMaterial implements ArmorMaterial, TemperatureAlteringMaterial {
    public static final BurlapClothesMaterial MATERIAL = new BurlapClothesMaterial();

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        if (!TFCAmbientalConfig.LOADED)
            return 1;
        return TFCAmbientalConfig.SERVER.durabilityBurlapClothes.get();
    }

    @Override
    public int getDefenseForType(ArmorItem.Type pType) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return SoundEvents.WOOL_PLACE;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(new ItemStack(TFCItems.BURLAP_CLOTH.get(), 1));
    }

    @Override
    public @NotNull String getName() {
        return "tfcambiental:burlap_cloth";
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }

    @Override
    public TempModifier getTempModifier(ItemStack stack) {
        return new TempModifier(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString(), -0.5f, -0.25f);
    }
}
