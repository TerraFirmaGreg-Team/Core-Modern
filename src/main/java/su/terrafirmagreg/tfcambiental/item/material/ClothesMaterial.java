package su.terrafirmagreg.tfcambiental.item.material;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public final class ClothesMaterial implements ArmorMaterial, TemperatureAlteringMaterial {
    public static final float NO_WARMING = 0.0f;
    public static final float T1_WARMING = 2.0f;
    public static final float T2_WARMING = 4.0f;
    public static final float T3_WARMING = 8.0f;

    public static final float T1_COOLING = -2.0f;
    public static final float T2_COOLING = -4.0f;
    public static final float T3_COOLING = -8.0f;

    public static final float T1_INSULATION = -0.3f;
    public static final float T2_INSULATION = -0.5f;
    public static final float T3_INSULATION = -1.0f;
    public static final float T4_INSULATION = -2.0f;

    // clothes lose 1 durability every 30s
    public static final int T1_DURABILITY = 3 * 60 * 2;
    public static final int T2_DURABILITY = 12 * 60 * 2;
    public static final int T3_DURABILITY = 18 * 60 * 2;
    public static final int T4_DURABILITY = 30 * 60 * 2;

    public static final ClothesMaterial STRAW = new ClothesMaterial(
            "tfcambiental:straw",
            SoundEvents.GRASS_BREAK,
            TFCItems.STRAW,
            T1_DURABILITY,
            1,
            T1_COOLING,
            T1_INSULATION);

    public static final ClothesMaterial INSULATED_LEATHER = new ClothesMaterial(
            "tfcambiental:insulated_leather",
            SoundEvents.WOOL_PLACE,
            TFCItems.WOOL_CLOTH,
            T1_DURABILITY,
            1,
            T1_WARMING,
            T3_INSULATION);

    public static final ClothesMaterial LEATHER_APRON = new ClothesMaterial(
            "tfcambiental:leather_apron",
            SoundEvents.LEASH_KNOT_BREAK,
            TFCItems.WOOL_CLOTH,
            T2_DURABILITY,
            0,
            NO_WARMING,
            T4_INSULATION);

    public static final ClothesMaterial BURLAP = new ClothesMaterial(
            "tfcambiental:burlap_cloth",
            SoundEvents.WOOL_PLACE,
            TFCItems.BURLAP_CLOTH,
            T2_DURABILITY,
            1,
            T1_COOLING,
            T2_INSULATION);

    public static final ClothesMaterial SILK = new ClothesMaterial(
            "tfcambiental:silk_cloth",
            SoundEvents.WOOL_PLACE,
            TFCItems.SILK_CLOTH,
            T4_DURABILITY,
            1,
            T1_COOLING,
            T2_INSULATION);

    public static final ClothesMaterial WOOL = new ClothesMaterial(
            "tfcambiental:wool_cloth",
            SoundEvents.WOOL_PLACE,
            TFCItems.WOOL_CLOTH,
            T2_DURABILITY,
            0,
            T2_WARMING,
            T2_INSULATION);

    private final String name;
    private final SoundEvent equipSound;
    private final RegistryObject<Item> repairIngredient;
    private final int durability;
    private final int enchantmentValue;
    private final float change;
    private final float potency;

    private ClothesMaterial(
            String name,
            SoundEvent equipSound,
            RegistryObject<Item> repairIngredient,
            int durability,
            int enchantmentValue,
            float change,
            float potency) {
        this.name = name;
        this.equipSound = equipSound;
        this.repairIngredient = repairIngredient;
        this.durability = durability;
        this.enchantmentValue = enchantmentValue;
        this.change = change;
        this.potency = potency;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        return this.durability;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type pType) {
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public @NotNull SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(new ItemStack(this.repairIngredient.get(), 1));
    }

    @Override
    public @NotNull String getName() {
        return this.name;
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
        return new TempModifier(this.change, this.potency);
    }
}
