package su.terrafirmagreg.tfcambiental.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.item.material.*;

public class TFCAmbientalItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCAmbiental.MOD_ID);

    public static final RegistryObject<Item> HOUSE_TESTER = ITEMS.register(
            "house_tester",
            () -> new HouseTester(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SNOWSHOES = ITEMS.register(
            "snowshoes",
            () -> new Snowshoes(new Item.Properties().stacksTo(1).durability(27000)));

    public static final RegistryObject<Item> LEATHER_APRON = ITEMS.register(
            "leather_apron",
            () -> new ClothesItem(LeatherApronMaterial.MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> STRAW_HAT = ITEMS.register(
            "straw_hat",
            () -> new ClothesItem(StrawClothesMaterial.MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> WOOL_HAT = ITEMS.register(
            "wool_hat",
            () -> new ClothesItem(WoolClothesMaterial.MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> WOOL_SWEATER = ITEMS.register(
            "wool_sweater",
            () -> new ClothesItem(WoolClothesMaterial.MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> WOOL_PANTS = ITEMS.register(
            "wool_pants",
            () -> new ClothesItem(WoolClothesMaterial.MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> WOOL_BOOTS = ITEMS.register(
            "wool_boots",
            () -> new ClothesItem(WoolClothesMaterial.MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> SILK_COWL = ITEMS.register(
            "silk_cowl",
            () -> new ClothesItem(SilkClothesMaterial.MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SILK_SHIRT = ITEMS.register(
            "silk_shirt",
            () -> new ClothesItem(SilkClothesMaterial.MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SILK_PANTS = ITEMS.register(
            "silk_pants",
            () -> new ClothesItem(SilkClothesMaterial.MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SILK_SHOES = ITEMS.register(
            "silk_shoes",
            () -> new ClothesItem(SilkClothesMaterial.MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> BURLAP_COWL = ITEMS.register(
            "burlap_cowl",
            () -> new ClothesItem(BurlapClothesMaterial.MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BURLAP_SHIRT = ITEMS.register(
            "burlap_shirt",
            () -> new ClothesItem(BurlapClothesMaterial.MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BURLAP_PANTS = ITEMS.register(
            "burlap_pants",
            () -> new ClothesItem(BurlapClothesMaterial.MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BURLAP_SHOES = ITEMS.register(
            "burlap_shoes",
            () -> new ClothesItem(BurlapClothesMaterial.MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static final RegistryObject<Item> LEATHER_HAT = ITEMS.register(
            "insulated_leather_hat",
            () -> new ClothesItem(InsulatedLeatherClothesMaterial.MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LEATHER_TUNIC = ITEMS.register(
            "insulated_leather_tunic",
            () -> new ClothesItem(InsulatedLeatherClothesMaterial.MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LEATHER_PANTS = ITEMS.register(
            "insulated_leather_pants",
            () -> new ClothesItem(InsulatedLeatherClothesMaterial.MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LEATHER_BOOTS = ITEMS.register(
            "insulated_leather_boots",
            () -> new ClothesItem(InsulatedLeatherClothesMaterial.MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));
}
