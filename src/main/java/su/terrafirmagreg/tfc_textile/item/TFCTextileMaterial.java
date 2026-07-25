package su.terrafirmagreg.tfc_textile.item;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import su.terrafirmagreg.tfcambiental.item.material.TemperatureAlteringMaterial;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public final class TFCTextileMaterial implements ArmorMaterial, TemperatureAlteringMaterial {
    // Cotton materials
    public static final TFCTextileMaterial COTTON = new TFCTextileMaterial(
            "tfc_textile:cotton_cloth",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.COTTON_CLOTH.get(), 1)),
            () -> 3000,
            0,
            6f,
            -0.15f);

    // Linen materials
    public static final TFCTextileMaterial LINEN = new TFCTextileMaterial(
            "tfc_textile:linen_cloth",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.LINEN_CLOTH.get(), 1)),
            () -> 3000,
            1,
            -1f,
            -0.25f);

    // Raw hide materials
    public static final TFCTextileMaterial RAW_HIDE = new TFCTextileMaterial(
            "tfc_textile:raw_hide",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.PRIMITIVE_INSULATION.get(), 1)),
            () -> 1500,
            0,
            4f,
            -0.10f);

    // Crocodile materials
    public static final TFCTextileMaterial CROCODILE = new TFCTextileMaterial(
            "tfc_textile:crocodile_leather",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.CROCODILE_LEATHER.get(), 1)),
            () -> 4000,
            2,
            8f,
            -0.20f);

    // Caribou materials
    public static final TFCTextileMaterial CARIBOU = new TFCTextileMaterial(
            "tfc_textile:caribou_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.CARIBOU_FUR.get(), 1)),
            () -> 3500,
            1,
            12f,
            -0.30f);

    // Polar bear materials
    public static final TFCTextileMaterial POLAR_BEAR = new TFCTextileMaterial(
            "tfc_textile:polar_bear_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.POLAR_BEAR_FUR.get(), 1)),
            () -> 3500,
            1,
            15f,
            -0.35f);

    // Black bear materials
    public static final TFCTextileMaterial BLACK_BEAR = new TFCTextileMaterial(
            "tfc_textile:black_bear_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.BLACK_BEAR_FUR.get(), 1)),
            () -> 3500,
            1,
            10f,
            -0.25f);

    // Grizzly bear materials
    public static final TFCTextileMaterial GRIZZLY_BEAR = new TFCTextileMaterial(
            "tfc_textile:grizzly_bear_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.GRIZZLY_BEAR_FUR.get(), 1)),
            () -> 3500,
            1,
            10f,
            -0.25f);

    // Panther materials
    public static final TFCTextileMaterial PANTHER = new TFCTextileMaterial(
            "tfc_textile:panther_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.PANTHER_FUR.get(), 1)),
            () -> 3500,
            1,
            8f,
            -0.20f);

    // Sabertooth materials
    public static final TFCTextileMaterial SABERTOOTH = new TFCTextileMaterial(
            "tfc_textile:sabertooth_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.SABERTOOTH_FUR.get(), 1)),
            () -> 3500,
            1,
            12f,
            -0.30f);

    // Tiger materials
    public static final TFCTextileMaterial TIGER = new TFCTextileMaterial(
            "tfc_textile:tiger_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.TIGER_FUR.get(), 1)),
            () -> 3500,
            1,
            8f,
            -0.20f);

    // Cougar materials
    public static final TFCTextileMaterial COUGAR = new TFCTextileMaterial(
            "tfc_textile:cougar_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.COUGAR_FUR.get(), 1)),
            () -> 3500,
            1,
            8f,
            -0.20f);

    // Dire wolf materials
    public static final TFCTextileMaterial DIRE_WOLF = new TFCTextileMaterial(
            "tfc_textile:direwolf_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.DIREWOLF_FUR.get(), 1)),
            () -> 3500,
            1,
            6f,
            -0.15f);

    // Lion materials
    public static final TFCTextileMaterial LION = new TFCTextileMaterial(
            "tfc_textile:lion_fur",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCTextileItems.LION_FUR.get(), 1)),
            () -> 3500,
            1,
            8f,
            -0.20f);

    private final String name;
    private final SoundEvent equipSound;
    private final Supplier<Ingredient> repairIngredient;
    private final IntSupplier durabilitySupplier;
    private final int enchantmentValue;
    private final float change;
    private final float potency;

    private TFCTextileMaterial(
            String name,
            SoundEvent equipSound,
            Supplier<Ingredient> repairIngredient,
            IntSupplier durabilitySupplier,
            int enchantmentValue,
            float change,
            float potency) {
        this.name = name;
        this.equipSound = equipSound;
        this.repairIngredient = repairIngredient;
        this.durabilitySupplier = durabilitySupplier;
        this.enchantmentValue = enchantmentValue;
        this.change = change;
        this.potency = potency;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        return this.durabilitySupplier.getAsInt();
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
        return this.repairIngredient.get();
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
