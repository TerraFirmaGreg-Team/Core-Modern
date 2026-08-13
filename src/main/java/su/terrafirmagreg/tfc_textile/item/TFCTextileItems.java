package su.terrafirmagreg.tfc_textile.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.tfc_textile.TFCtextile;
import su.terrafirmagreg.tfcambiental.item.ClothesItem;

public class TFCTextileItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCtextile.MODID);

    //ingredients

    public static final RegistryObject<Item> COTTON_CLOTH = ITEMS.register(
            "cotton_cloth",
            () -> new Item(new Item.Properties().stacksTo(32))

    );

    public static final RegistryObject<Item> COTTON_STRING = ITEMS.register(
            "cotton_string",
            () -> new Item(new Item.Properties().stacksTo(32))

    );

    public static final RegistryObject<Item> CROCODILE_LEATHER = ITEMS.register(
            "crocodile_leather",
            () -> new Item(new Item.Properties().stacksTo(32))

    );

    public static final RegistryObject<Item> PRIMITIVE_INSULATION = ITEMS.register(
            "primitive_insulation",
            () -> new Item(new Item.Properties().stacksTo(32))

    );

    public static final RegistryObject<Item> LINEN_CLOTH = ITEMS.register(
            "linen_cloth",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> CARIBOU_FUR = ITEMS.register(
            "caribou_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> POLAR_BEAR_FUR = ITEMS.register(
            "polar_bear_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> BLACK_BEAR_FUR = ITEMS.register(
            "black_bear_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> GRIZZLY_BEAR_FUR = ITEMS.register(
            "grizzly_bear_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> PANTHER_FUR = ITEMS.register(
            "panther_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> SABERTOOTH_FUR = ITEMS.register(
            "sabertooth_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> TIGER_FUR = ITEMS.register(
            "tiger_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> COUGAR_FUR = ITEMS.register(
            "cougar_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> DIREWOLF_FUR = ITEMS.register(
            "direwolf_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );
    public static final RegistryObject<Item> LION_FUR = ITEMS.register(
            "lion_fur",
            () -> new Item(new Item.Properties().stacksTo(32))

    );

    //cotton clothes

    public static final RegistryObject<Item> COTTON_HAT = ITEMS.register(
            "cotton_hat",
            () -> new ClothesItem(TFCTextileMaterial.COTTON, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> COTTON_SHIRT = ITEMS.register(
            "cotton_shirt",
            () -> new ClothesItem(TFCTextileMaterial.COTTON, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> COTTON_PANTS = ITEMS.register(
            "cotton_pants",
            () -> new ClothesItem(TFCTextileMaterial.COTTON, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> COTTON_SOCKS = ITEMS.register(
            "cotton_socks",
            () -> new ClothesItem(TFCTextileMaterial.COTTON, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //crocodile clothes

    public static final RegistryObject<Item> CROCODILE_HAT = ITEMS.register(
            "crocodile_hat",
            () -> new ClothesItem(TFCTextileMaterial.CROCODILE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> CROCODILE_SHIRT = ITEMS.register(
            "crocodile_shirt",
            () -> new ClothesItem(TFCTextileMaterial.CROCODILE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> CROCODILE_PANTS = ITEMS.register(
            "crocodile_pants",
            () -> new ClothesItem(TFCTextileMaterial.CROCODILE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> CROCODILE_BOOTS = ITEMS.register(
            "crocodile_boots",
            () -> new ClothesItem(TFCTextileMaterial.CROCODILE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //linen clothes

    public static final RegistryObject<Item> LINEN_HAT = ITEMS.register(
            "linen_hat",
            () -> new ClothesItem(TFCTextileMaterial.LINEN, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LINEN_SHIRT = ITEMS.register(
            "linen_shirt",
            () -> new ClothesItem(TFCTextileMaterial.LINEN, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LINEN_PANTS = ITEMS.register(
            "linen_pants",
            () -> new ClothesItem(TFCTextileMaterial.LINEN, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LINEN_SOCKS = ITEMS.register(
            "linen_socks",
            () -> new ClothesItem(TFCTextileMaterial.LINEN, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //caveman clothes

    public static final RegistryObject<Item> RAW_HAT = ITEMS.register(
            "raw_hat",
            () -> new ClothesItem(TFCTextileMaterial.RAW_HIDE, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> RAW_SHIRT = ITEMS.register(
            "raw_shirt",
            () -> new ClothesItem(TFCTextileMaterial.RAW_HIDE, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> RAW_PANTS = ITEMS.register(
            "raw_pants",
            () -> new ClothesItem(TFCTextileMaterial.RAW_HIDE, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> RAW_SOCKS = ITEMS.register(
            "raw_socks",
            () -> new ClothesItem(TFCTextileMaterial.RAW_HIDE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //Artic-caribou

    public static final RegistryObject<Item> CARIBOU_HAT = ITEMS.register(
            "caribou_hat",
            () -> new ClothesItem(TFCTextileMaterial.CARIBOU, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> CARIBOU_SHIRT = ITEMS.register(
            "caribou_shirt",
            () -> new ClothesItem(TFCTextileMaterial.CARIBOU, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> CARIBOU_PANTS = ITEMS.register(
            "caribou_pants",
            () -> new ClothesItem(TFCTextileMaterial.CARIBOU, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> CARIBOU_SOCKS = ITEMS.register(
            "caribou_boots",
            () -> new ClothesItem(TFCTextileMaterial.CARIBOU, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //Artic-polar bear

    public static final RegistryObject<Item> POLAR_HAT = ITEMS.register(
            "polar_bear_hat",
            () -> new ClothesItem(TFCTextileMaterial.POLAR_BEAR, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> POLAR_SHIRT = ITEMS.register(
            "polar_bear_shirt",
            () -> new ClothesItem(TFCTextileMaterial.POLAR_BEAR, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> POLAR_PANTS = ITEMS.register(
            "polar_bear_pants",
            () -> new ClothesItem(TFCTextileMaterial.POLAR_BEAR, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> POLAR_SOCKS = ITEMS.register(
            "polar_bear_boots",
            () -> new ClothesItem(TFCTextileMaterial.POLAR_BEAR, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //Tiger

    public static final RegistryObject<Item> TIGER_HAT = ITEMS.register(
            "tiger_hat",
            () -> new ClothesItem(TFCTextileMaterial.TIGER, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> TIGER_SHIRT = ITEMS.register(
            "tiger_shirt",
            () -> new ClothesItem(TFCTextileMaterial.TIGER, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> TIGER_PANTS = ITEMS.register(
            "tiger_pants",
            () -> new ClothesItem(TFCTextileMaterial.TIGER, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> TIGER_SOCKS = ITEMS.register(
            "tiger_boots",
            () -> new ClothesItem(TFCTextileMaterial.TIGER, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //Cougar

    public static final RegistryObject<Item> COUGAR_HAT = ITEMS.register(
            "cougar_hat",
            () -> new ClothesItem(TFCTextileMaterial.COUGAR, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> COUGAR_SHIRT = ITEMS.register(
            "cougar_shirt",
            () -> new ClothesItem(TFCTextileMaterial.COUGAR, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> COUGAR_PANTS = ITEMS.register(
            "cougar_pants",
            () -> new ClothesItem(TFCTextileMaterial.COUGAR, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> COUGAR_SOCKS = ITEMS.register(
            "cougar_boots",
            () -> new ClothesItem(TFCTextileMaterial.COUGAR, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    //Panther

    public static final RegistryObject<Item> PANTHER_HAT = ITEMS.register(
            "panther_hat",
            () -> new ClothesItem(TFCTextileMaterial.PANTHER, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> PANTHER_SHIRT = ITEMS.register(
            "panther_shirt",
            () -> new ClothesItem(TFCTextileMaterial.PANTHER, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> PANTHER_PANTS = ITEMS.register(
            "panther_pants",
            () -> new ClothesItem(TFCTextileMaterial.PANTHER, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> PANTHER_SOCKS = ITEMS.register(
            "panther_boots",
            () -> new ClothesItem(TFCTextileMaterial.PANTHER, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Sabertooth

    public static final RegistryObject<Item> SABERTOOTH_HAT = ITEMS.register(
            "sabertooth_hat",
            () -> new ClothesItem(TFCTextileMaterial.SABERTOOTH, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SABERTOOTH_SHIRT = ITEMS.register(
            "sabertooth_shirt",
            () -> new ClothesItem(TFCTextileMaterial.SABERTOOTH, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SABERTOOTH_PANTS = ITEMS.register(
            "sabertooth_pants",
            () -> new ClothesItem(TFCTextileMaterial.SABERTOOTH, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> SABERTOOTH_SOCKS = ITEMS.register(
            "sabertooth_boots",
            () -> new ClothesItem(TFCTextileMaterial.SABERTOOTH, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Black Bear

    public static final RegistryObject<Item> BLACK_BEAR_HAT = ITEMS.register(
            "black_bear_hat",
            () -> new ClothesItem(TFCTextileMaterial.BLACK_BEAR, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BLACK_BEAR_SHIRT = ITEMS.register(
            "black_bear_shirt",
            () -> new ClothesItem(TFCTextileMaterial.BLACK_BEAR, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BLACK_BEAR_PANTS = ITEMS.register(
            "black_bear_pants",
            () -> new ClothesItem(TFCTextileMaterial.BLACK_BEAR, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> BLACK_BEAR_SOCKS = ITEMS.register(
            "black_bear_boots",
            () -> new ClothesItem(TFCTextileMaterial.BLACK_BEAR, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Grizzly Bear

    public static final RegistryObject<Item> GRIZZLY_BEAR_HAT = ITEMS.register(
            "grizzly_bear_hat",
            () -> new ClothesItem(TFCTextileMaterial.GRIZZLY_BEAR, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> GRIZZLY_BEAR_SHIRT = ITEMS.register(
            "grizzly_bear_shirt",
            () -> new ClothesItem(TFCTextileMaterial.GRIZZLY_BEAR, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> GRIZZLY_BEAR_PANTS = ITEMS.register(
            "grizzly_bear_pants",
            () -> new ClothesItem(TFCTextileMaterial.GRIZZLY_BEAR, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> GRIZZLY_BEAR_SOCKS = ITEMS.register(
            "grizzly_bear_boots",
            () -> new ClothesItem(TFCTextileMaterial.GRIZZLY_BEAR, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Dire Wolf

    public static final RegistryObject<Item> DIREWOLF_HAT = ITEMS.register(
            "direwolf_hat",
            () -> new ClothesItem(TFCTextileMaterial.DIRE_WOLF, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> DIREWOLF_SHIRT = ITEMS.register(
            "direwolf_shirt",
            () -> new ClothesItem(TFCTextileMaterial.DIRE_WOLF, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> DIREWOLF_PANTS = ITEMS.register(
            "direwolf_pants",
            () -> new ClothesItem(TFCTextileMaterial.DIRE_WOLF, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> DIREWOLF_SOCKS = ITEMS.register(
            "direwolf_boots",
            () -> new ClothesItem(TFCTextileMaterial.DIRE_WOLF, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Lion

    public static final RegistryObject<Item> LION_HAT = ITEMS.register(
            "lion_hat",
            () -> new ClothesItem(TFCTextileMaterial.LION, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LION_SHIRT = ITEMS.register(
            "lion_shirt",
            () -> new ClothesItem(TFCTextileMaterial.LION, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LION_PANTS = ITEMS.register(
            "lion_pants",
            () -> new ClothesItem(TFCTextileMaterial.LION, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> LION_SOCKS = ITEMS.register(
            "lion_boots",
            () -> new ClothesItem(TFCTextileMaterial.LION, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Red Elk

    public static final RegistryObject<Item> RED_ELK_HAT = ITEMS.register(
            "red_elk_hat",
            () -> new ClothesItem(TFCTextileMaterial.RED_ELK, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> RED_ELK_SHIRT = ITEMS.register(
            "red_elk_shirt",
            () -> new ClothesItem(TFCTextileMaterial.RED_ELK, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> RED_ELK_PANTS = ITEMS.register(
            "red_elk_pants",
            () -> new ClothesItem(TFCTextileMaterial.RED_ELK, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> RED_ELK_SOCKS = ITEMS.register(
            "red_elk_boots",
            () -> new ClothesItem(TFCTextileMaterial.RED_ELK, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    // Phantom Silk

    public static final RegistryObject<Item> PHANTOM_SILK_HAT = ITEMS.register(
            "phantom_silk_hat",
            () -> new ClothesItem(TFCTextileMaterial.PHANTOM_SILK, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> PHANTOM_SILK_SHIRT = ITEMS.register(
            "phantom_silk_shirt",
            () -> new ClothesItem(TFCTextileMaterial.PHANTOM_SILK, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> PHANTOM_SILK_PANTS = ITEMS.register(
            "phantom_silk_pants",
            () -> new ClothesItem(TFCTextileMaterial.PHANTOM_SILK, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1).durability(1)));
    public static final RegistryObject<Item> PHANTOM_SILK_SOCKS = ITEMS.register(
            "phantom_silk_boots",
            () -> new ClothesItem(TFCTextileMaterial.PHANTOM_SILK, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).durability(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
