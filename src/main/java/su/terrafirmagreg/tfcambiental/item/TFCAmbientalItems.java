package su.terrafirmagreg.tfcambiental.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.item.material.ClothesMaterial;

public class TFCAmbientalItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCAmbiental.MOD_ID);

    public static final RegistryObject<Item> HOUSE_TESTER = ITEMS.register(
            "house_tester",
            () -> new HouseTester(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> LEATHER_APRON = ITEMS.register(
            "leather_apron",
            () -> new ClothesItem(ClothesMaterial.LEATHER_APRON, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> STRAW_HAT = ITEMS.register(
            "straw_hat",
            () -> new ClothesItem(ClothesMaterial.STRAW, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> WOOL_HAT = ITEMS.register(
            "wool_hat",
            () -> new ClothesItem(ClothesMaterial.WOOL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> WOOL_SWEATER = ITEMS.register(
            "wool_sweater",
            () -> new ClothesItem(ClothesMaterial.WOOL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> WOOL_PANTS = ITEMS.register(
            "wool_pants",
            () -> new ClothesItem(ClothesMaterial.WOOL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> WOOL_BOOTS = ITEMS.register(
            "wool_boots",
            () -> new ClothesItem(ClothesMaterial.WOOL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> SILK_COWL = ITEMS.register(
            "silk_cowl",
            () -> new ClothesItem(ClothesMaterial.SILK, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SILK_SHIRT = ITEMS.register(
            "silk_shirt",
            () -> new ClothesItem(ClothesMaterial.SILK, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SILK_PANTS = ITEMS.register(
            "silk_pants",
            () -> new ClothesItem(ClothesMaterial.SILK, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SILK_SHOES = ITEMS.register(
            "silk_shoes",
            () -> new ClothesItem(ClothesMaterial.SILK, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> BURLAP_COWL = ITEMS.register(
            "burlap_cowl",
            () -> new ClothesItem(ClothesMaterial.BURLAP, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BURLAP_SHIRT = ITEMS.register(
            "burlap_shirt",
            () -> new ClothesItem(ClothesMaterial.BURLAP, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BURLAP_PANTS = ITEMS.register(
            "burlap_pants",
            () -> new ClothesItem(ClothesMaterial.BURLAP, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BURLAP_SHOES = ITEMS.register(
            "burlap_shoes",
            () -> new ClothesItem(ClothesMaterial.BURLAP, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> LEATHER_HAT = ITEMS.register(
            "insulated_leather_hat",
            () -> new ClothesItem(ClothesMaterial.INSULATED_LEATHER, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LEATHER_TUNIC = ITEMS.register(
            "insulated_leather_tunic",
            () -> new ClothesItem(ClothesMaterial.INSULATED_LEATHER, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LEATHER_PANTS = ITEMS.register(
            "insulated_leather_pants",
            () -> new ClothesItem(ClothesMaterial.INSULATED_LEATHER, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LEATHER_BOOTS = ITEMS.register(
            "insulated_leather_boots",
            () -> new ClothesItem(ClothesMaterial.INSULATED_LEATHER, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));
}
