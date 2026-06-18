package su.terrafirmagreg.tfcambiental.item.material;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public final class ClothesMaterial implements ArmorMaterial, TemperatureAlteringMaterial {
    public static final ClothesMaterial BURLAP = new ClothesMaterial(
            "tfcambiental:burlap_cloth",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCItems.BURLAP_CLOTH.get(), 1)),
            () -> TFCAmbientalConfig.SERVER.durabilityBurlapClothes.get(),
            1,
            -0.5f,
            -0.25f);

    public static final ClothesMaterial INSULATED_LEATHER = new ClothesMaterial(
            "tfcambiental:insulated_leather",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCItems.WOOL_CLOTH.get(), 1)),
            () -> TFCAmbientalConfig.SERVER.durabilityInsulatedLeatherClothes.get(),
            1,
            2f,
            -0.15f);

    public static final ClothesMaterial LEATHER_APRON = new ClothesMaterial(
            "tfcambiental:leather_apron",
            SoundEvents.LEASH_KNOT_BREAK,
            () -> Ingredient.of(new ItemStack(TFCItems.WOOL_CLOTH.get(), 1)),
            () -> TFCAmbientalConfig.SERVER.durabilityLeatherApronClothes.get(),
            0,
            0f,
            -0.35f);

    public static final ClothesMaterial SILK = new ClothesMaterial(
            "tfcambiental:silk_cloth",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCItems.SILK_CLOTH.get(), 1)),
            () -> TFCAmbientalConfig.SERVER.durabilitySilkClothes.get(),
            1,
            -1f,
            -0.1f);

    public static final ClothesMaterial STRAW = new ClothesMaterial(
            "tfcambiental:straw",
            SoundEvents.GRASS_BREAK,
            () -> Ingredient.of(new ItemStack(TFCItems.STRAW.get(), 1)),
            () -> TFCAmbientalConfig.SERVER.durabilityStrawClothes.get(),
            1,
            0f,
            -0.1f);

    public static final ClothesMaterial WOOL = new ClothesMaterial(
            "tfcambiental:wool_cloth",
            SoundEvents.WOOL_PLACE,
            () -> Ingredient.of(new ItemStack(TFCItems.WOOL_CLOTH.get(), 1)),
            () -> TFCAmbientalConfig.SERVER.durabilityWoolClothes.get(),
            0,
            4f,
            -0.15f);

    private final String name;
    private final SoundEvent equipSound;
    private final Supplier<Ingredient> repairIngredient;
    private final IntSupplier durabilitySupplier;
    private final int enchantmentValue;
    private final float change;
    private final float potency;

    private ClothesMaterial(
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
