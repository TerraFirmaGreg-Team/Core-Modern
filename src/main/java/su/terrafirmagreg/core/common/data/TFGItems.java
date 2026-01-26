package su.terrafirmagreg.core.common.data;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import appeng.api.upgrades.Upgrades;
import de.mennomax.astikorcarts.item.CartItem;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.items.vehicles.RocketItem;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.items.*;

/**
 * Uncomment TFGCreativeTab in TFGCore if you register anything new here
 */

@SuppressWarnings("unused")
public class TFGItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFGCore.MOD_ID);

    public static final RegistryObject<Item> PIGLIN_DISGUISE = ITEMS.register("piglin_disguise",
            () -> new PiglinDisguise(TFGBlocks.PIGLIN_DISGUISE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> TROWEL = ITEMS.register("trowel",
            () -> new TrowelItem(new Item.Properties()));

    public static final RegistryObject<Item> EMPTY_DNA_SYRINGE = ITEMS.register("empty_dna_syringe",
            () -> new EmptyDnaSyringeItem(new Item.Properties()));
    public static final RegistryObject<Item> DIRTY_DNA_SYRINGE = ITEMS.register("dirty_dna_syringe",
            () -> new DirtyDnaSyringeItem(new Item.Properties()));
    public static final RegistryObject<Item> FILLED_DNA_SYRINGE = ITEMS.register("filled_dna_syringe",
            () -> new FilledDnaSyringeItem(new Item.Properties()));
    public static final RegistryObject<Item> PROGENITOR_CELLS = ITEMS.register("progenitor_cells",
            () -> new ProgenitorCellsItem(new Item.Properties()));
    public static final RegistryObject<Item> FISH_ROE = ITEMS.register("fish_roe",
            () -> new FishRoeItem(new Item.Properties()));

    public static final RegistryObject<Item> MOON_RABBIT_EGG = registerSpawnEgg(TFGEntities.MOON_RABBIT, 15767516, 9756658);
    public static final RegistryObject<Item> GLACIAN_RAM_EGG = registerSpawnEgg(TFGEntities.GLACIAN_RAM, 16772607, 3997758);
    public static final RegistryObject<Item> SNIFFER_SPAWN_EGG = registerSpawnEgg(TFGEntities.SNIFFER, 11285007, 4829025);
    public static final RegistryObject<Item> WRAPTOR_SPAWN_EGG = registerSpawnEgg(TFGEntities.WRAPTOR, 15767516, 4829025);
    public static final RegistryObject<Item> SURFER_SPAWN_EGG = registerSpawnEgg(TFGEntities.SURFER, 7644045, 12824430);

    @SuppressWarnings("deprecation")
    public static final RegistryObject<BucketItem> MARS_WATER_BUCKET = register("semiheavy_ammoniacal_water_bucket",
            () -> new BucketItem(TFGFluids.MARS_WATER.getSource(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    @SuppressWarnings("deprecation")
    public static final RegistryObject<BucketItem> SULFUR_FUMES_BUCKET = register("sulfur_fumes_bucket",
            () -> new BucketItem(TFGFluids.SULFUR_FUMES.getSource(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    @SuppressWarnings("deprecation")
    public static final RegistryObject<BucketItem> GEYSER_SLURRY_BUCKET = register("geyser_slurry_bucket",
            () -> new BucketItem(TFGFluids.GEYSER_SLURRY.getSource(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final RegistryObject<Item> RAILGUN_AMMO_SHELL = ITEMS.register("railgun_ammo_shell",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> GLACIAN_WOOL = ITEMS.register("glacian_wool",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final RegistryObject<Item> SNIFFER_WOOL = ITEMS.register("sniffer_wool",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final RegistryObject<Item> SNIFFER_EGG = ITEMS.register("sniffer_egg",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final RegistryObject<Item> WRAPTOR_WOOL = ITEMS.register("wraptor_wool",
            () -> new Item(new Item.Properties().stacksTo(32)));
    public static final RegistryObject<Item> WRAPTOR_EGG = ITEMS.register("wraptor_egg",
            () -> new Item(new Item.Properties().stacksTo(32)));

    public static final RegistryObject<ComponentItem> ELECTRIC_EXTENDO_GRIP = ITEMS.register("electric_extendo_grip",
            () -> new ElectricExtendoGrip(new Item.Properties()));

    public static final RegistryObject<Item> WIRELESS_CARD = ITEMS.register("wireless_card",
            () -> Upgrades.createUpgradeCardItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));

    public static final RegistryObject<Item> RNR_PLOW = ITEMS.register("rnr_plow",
            () -> new CartItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TIER_1_DOUBLE_ROCKET = ITEMS.register("tier_1_double_rocket",
            () -> {
                RegistryObject<EntityType<Rocket>> var10002 = TFGEntities.TIER_1_DOUBLE_ROCKET;
                Objects.requireNonNull(var10002);
                return new RocketItem(var10002::get, (new Item.Properties()).stacksTo(1).fireResistant());
            });

    private static RegistryObject<Item> register(String name) {
        return register(name, () -> new Item(new Item.Properties()));
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item) {
        return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
    }

    private static <T extends EntityType<? extends Mob>> RegistryObject<Item> registerSpawnEgg(RegistryObject<T> entity, int color1, int color2) {
        return register("spawn_egg/" + entity.getId().getPath(),
                () -> new ForgeSpawnEggItem(entity, color1, color2, new Item.Properties()));
    }
}
