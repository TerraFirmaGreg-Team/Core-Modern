package su.terrafirmagreg.tfc_textile.item;

import org.jetbrains.annotations.NotNull;

import com.eerussianguy.beneath.common.items.BeneathItems;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.tfcambiental.item.material.ClothesMaterial;
import su.terrafirmagreg.tfcambiental.item.material.TemperatureAlteringMaterial;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public final class TFCTextileMaterial implements ArmorMaterial, TemperatureAlteringMaterial {
    public static final TFCTextileMaterial COTTON = new TFCTextileMaterial(
            "tfc_textile:cotton_cloth",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.COTTON_CLOTH,
            ClothesMaterial.T2_DURABILITY,
            0,
            ClothesMaterial.T2_WARMING,
            ClothesMaterial.T2_INSULATION);

    public static final TFCTextileMaterial LINEN = new TFCTextileMaterial(
            "tfc_textile:linen_cloth",
            SoundEvents.WOOL_PLACE,
            ForgeRegistries.ITEMS.getValue(TFGCore.id("linen_cloth")),
            ClothesMaterial.T2_DURABILITY,
            1,
            ClothesMaterial.T1_COOLING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial RAW_HIDE = new TFCTextileMaterial(
            "tfc_textile:raw_hide",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.PRIMITIVE_INSULATION,
            ClothesMaterial.T1_DURABILITY,
            0,
            ClothesMaterial.T1_WARMING,
            ClothesMaterial.T2_INSULATION);

    public static final TFCTextileMaterial DIRE_WOLF = new TFCTextileMaterial(
            "tfc_textile:direwolf_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.DIREWOLF_FUR,
            ClothesMaterial.T2_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T2_INSULATION);

    public static final TFCTextileMaterial PANTHER = new TFCTextileMaterial(
            "tfc_textile:panther_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.PANTHER_FUR,
            ClothesMaterial.T2_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T2_INSULATION);

    public static final TFCTextileMaterial COUGAR = new TFCTextileMaterial(
            "tfc_textile:cougar_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.COUGAR_FUR,
            ClothesMaterial.T3_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial TIGER = new TFCTextileMaterial(
            "tfc_textile:tiger_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.TIGER_FUR,
            ClothesMaterial.T3_DURABILITY,
            1,
            ClothesMaterial.T2_COOLING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial CROCODILE = new TFCTextileMaterial(
            "tfc_textile:crocodile_leather",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.CROCODILE_LEATHER,
            ClothesMaterial.T3_DURABILITY,
            2,
            ClothesMaterial.T2_COOLING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial LION = new TFCTextileMaterial(
            "tfc_textile:lion_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.LION_FUR,
            ClothesMaterial.T3_DURABILITY,
            1,
            ClothesMaterial.T2_COOLING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial BLACK_BEAR = new TFCTextileMaterial(
            "tfc_textile:black_bear_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.BLACK_BEAR_FUR,
            ClothesMaterial.T3_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial GRIZZLY_BEAR = new TFCTextileMaterial(
            "tfc_textile:grizzly_bear_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.GRIZZLY_BEAR_FUR,
            ClothesMaterial.T3_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T3_INSULATION);

    public static final TFCTextileMaterial CARIBOU = new TFCTextileMaterial(
            "tfc_textile:caribou_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.CARIBOU_FUR,
            ClothesMaterial.T3_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T4_INSULATION);

    public static final TFCTextileMaterial SABERTOOTH = new TFCTextileMaterial(
            "tfc_textile:sabertooth_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.SABERTOOTH_FUR,
            ClothesMaterial.T4_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T4_INSULATION);

    public static final TFCTextileMaterial POLAR_BEAR = new TFCTextileMaterial(
            "tfc_textile:polar_bear_fur",
            SoundEvents.WOOL_PLACE,
            TFCTextileItems.POLAR_BEAR_FUR,
            ClothesMaterial.T4_DURABILITY,
            1,
            ClothesMaterial.T3_WARMING,
            ClothesMaterial.T4_INSULATION);

    public static final TFCTextileMaterial RED_ELK = new TFCTextileMaterial(
            "tfc_textile:red_elk",
            SoundEvents.WOOL_PLACE,
            BeneathItems.CURSED_HIDE,
            ClothesMaterial.T4_DURABILITY,
            2,
            ClothesMaterial.T3_COOLING,
            ClothesMaterial.T4_INSULATION);

    public static final TFCTextileMaterial PHANTOM_SILK = new TFCTextileMaterial(
            "tfc_textile:phantom_silk",
            SoundEvents.WOOL_PLACE,
            ForgeRegistries.ITEMS.getValue(TFGCore.id("phantom_silk")),
            ClothesMaterial.T2_DURABILITY,
            2,
            ClothesMaterial.T3_COOLING,
            ClothesMaterial.T2_INSULATION);

    private final String name;
    private final SoundEvent equipSound;
    private final Item repairIngredient;
    private final int durability;
    private final int enchantmentValue;
    private final float change;
    private final float potency;

    private TFCTextileMaterial(
            String name,
            SoundEvent equipSound,
            Item repairIngredient,
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

    private TFCTextileMaterial(
            String name,
            SoundEvent equipSound,
            RegistryObject<Item> repairIngredient,
            int durability,
            int enchantmentValue,
            float change,
            float potency) {
        this.name = name;
        this.equipSound = equipSound;
        this.repairIngredient = repairIngredient.get();
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
        return Ingredient.of(new ItemStack(this.repairIngredient, 1));
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
