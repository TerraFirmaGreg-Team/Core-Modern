package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.upgrades.Upgrades;
import de.mennomax.astikorcarts.item.CartItem;
import earth.terrarium.adastra.common.items.vehicles.RocketItem;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.items.*;

/**
 * Uncomment TFGCreativeTab in TFGCore if you register anything new here
 */

@SuppressWarnings("unused")
public class TFGItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFGCore.MOD_ID);

    public static final ItemEntry<PiglinDisguise> PIGLIN_DISGUISE = TFGCore.REGISTRATE.item("piglin_disguise",
            (p) -> new PiglinDisguise(TFGBlocks.PIGLIN_DISGUISE_BLOCK.get(), p))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<TrowelItem> TROWEL = TFGCore.REGISTRATE.item("trowel", TrowelItem::new)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<EmptyDnaSyringeItem> EMPTY_DNA_SYRINGE = TFGCore.REGISTRATE.item("empty_dna_syringe", EmptyDnaSyringeItem::new)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<DirtyDnaSyringeItem> DIRTY_DNA_SYRINGE = TFGCore.REGISTRATE.item("dirty_dna_syringe", DirtyDnaSyringeItem::new)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<FilledDnaSyringeItem> FILLED_DNA_SYRINGE = TFGCore.REGISTRATE.item("filled_dna_syringe", FilledDnaSyringeItem::new)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();
    public static final ItemEntry<ProgenitorCellsItem> PROGENITOR_CELLS = TFGCore.REGISTRATE.item("progenitor_cells", ProgenitorCellsItem::new)
            .model((ctx, prov) -> prov.generated(ctx::getEntry,
                    TFGCore.id("item/fish_roe_0"), TFGCore.id("item/fish_roe_0"), TFGCore.id("item/fish_roe_1"), TFGCore.id("item/fish_roe_2")))
            .register();

    public static final ItemEntry<FishRoeItem> FISH_ROE = TFGCore.REGISTRATE.item("fish_roe", FishRoeItem::new)
            .model((ctx, prov) -> prov.generated(ctx::getEntry,
                    TFGCore.id("item/progenitor_cells_0"), TFGCore.id("item/progenitor_cells_1"), TFGCore.id("item/progenitor_cells_2")))
            .register();

    public static final ItemEntry<ForgeSpawnEggItem> MOON_RABBIT_EGG = registerSpawnEgg(TFGEntities.MOON_RABBIT, 15767516, 9756658);
    public static final ItemEntry<ForgeSpawnEggItem> GLACIAN_RAM_EGG = registerSpawnEgg(TFGEntities.GLACIAN_RAM, 16772607, 3997758);
    public static final ItemEntry<ForgeSpawnEggItem> SNIFFER_SPAWN_EGG = registerSpawnEgg(TFGEntities.SNIFFER, 11285007, 4829025);
    public static final ItemEntry<ForgeSpawnEggItem> WRAPTOR_SPAWN_EGG = registerSpawnEgg(TFGEntities.WRAPTOR, 15767516, 4829025);
    public static final ItemEntry<ForgeSpawnEggItem> SURFER_SPAWN_EGG = registerSpawnEgg(TFGEntities.SURFER, 7644045, 12824430);

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> MARS_WATER_BUCKET = TFGCore.REGISTRATE.item("semiheavy_ammoniacal_water_bucket",
            p -> new BucketItem(TFGFluids.MARS_WATER.getSource(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> SULFUR_FUMES_BUCKET = TFGCore.REGISTRATE.item("sulfur_fumes_bucket",
                    p -> new BucketItem(TFGFluids.SULFUR_FUMES.getSource(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> GEYSER_SLURRY_BUCKET = TFGCore.REGISTRATE.item("geyser_slurry_bucket",
                    p -> new BucketItem(TFGFluids.GEYSER_SLURRY.getSource(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<Item> RAILGUN_AMMO_SHELL = TFGCore.REGISTRATE.item("railgun_ammo_shell", Item::new)
            .properties(p -> p.stacksTo(16))
            .register();

    public static final ItemEntry<Item> GLACIAN_WOOL = TFGCore.REGISTRATE.item("glacian_wool", Item::new)
            .properties(p -> p.stacksTo(32))
            .register();

    public static final ItemEntry<Item> SNIFFER_WOOL = TFGCore.REGISTRATE.item("sniffer_wool", Item::new)
            .properties(p -> p.stacksTo(32))
            .register();

    public static final ItemEntry<Item> SNIFFER_EGG = TFGCore.REGISTRATE.item("sniffer_egg", Item::new)
            .properties(p -> p.stacksTo(32))
            .register();

    public static final ItemEntry<Item> WRAPTOR_WOOL = TFGCore.REGISTRATE.item("wraptor_wool", Item::new)
            .properties(p -> p.stacksTo(32))
            .register();

    public static final ItemEntry<Item> WRAPTOR_EGG = TFGCore.REGISTRATE.item("wraptor_egg", Item::new)
            .properties(p -> p.stacksTo(32))
            .register();

    public static final ItemEntry<Item> WIRELESS_CARD = TFGCore.REGISTRATE.item("wireless_card",
            (p) -> Upgrades.createUpgradeCardItem(p.rarity(Rarity.UNCOMMON).stacksTo(1)))
            .model((ctx, prov) -> prov.generated(ctx::getEntry,
                    TFGCore.id("item/wireless_card/wireless_card_base"), TFGCore.id("item/wireless_card/wireless_card_layer1"), TFGCore.id("item/wireless_card/wireless_card_layer2")))
            .register();

    public static final ItemEntry<CartItem> RNR_PLOW = TFGCore.REGISTRATE.item("rnr_plow", CartItem::new)
            .properties(p -> p.stacksTo(1))
            .register();

    public static final ItemEntry<RocketItem> TIER_1_DOUBLE_ROCKET = TFGCore.REGISTRATE.item("tier_1_double_rocket",
            p -> new RocketItem(TFGEntities.TIER_1_DOUBLE_ROCKET::get, p))
            .properties(p -> p.stacksTo(1).fireResistant())
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<RocketItem> TIER_2_DOUBLE_ROCKET = TFGCore.REGISTRATE.item("tier_2_double_rocket",
                    p -> new RocketItem(TFGEntities.TIER_2_DOUBLE_ROCKET::get, p))
            .properties(p -> p.stacksTo(1).fireResistant())
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<RocketItem> TIER_3_DOUBLE_ROCKET = TFGCore.REGISTRATE.item("tier_3_double_rocket",
                    p -> new RocketItem(TFGEntities.TIER_3_DOUBLE_ROCKET::get, p))
            .properties(p -> p.stacksTo(1).fireResistant())
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<RocketItem> TIER_4_DOUBLE_ROCKET = TFGCore.REGISTRATE.item("tier_4_double_rocket",
                    p -> new RocketItem(TFGEntities.TIER_4_DOUBLE_ROCKET::get, p))
            .properties(p -> p.stacksTo(1).fireResistant())
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    private static <T extends Mob> ItemEntry<ForgeSpawnEggItem> registerSpawnEgg(EntityEntry<T> entity, int color1, int color2) {
        return TFGCore.REGISTRATE.item("spawn_egg/" + entity.getId().getPath(),
                (p) -> new ForgeSpawnEggItem(entity, color1, color2, p)).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).register();
    }
}
