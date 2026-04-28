package su.terrafirmagreg.core.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;

import appeng.api.upgrades.Upgrades;
import de.mennomax.astikorcarts.item.CartItem;
import earth.terrarium.adastra.common.items.vehicles.RocketItem;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.tfgt.TFGCovers;
import su.terrafirmagreg.core.common.item.*;
import su.terrafirmagreg.core.common.item.wearable.FlippersItem;
import su.terrafirmagreg.core.common.item.wearable.SnorkelItem;
import su.terrafirmagreg.core.common.item.wearable.SnowshoesItem;
import su.terrafirmagreg.core.utils.ModelUtils;

@SuppressWarnings("unused")
public class TFGItems {
    public static void init() {
        TFGItems_Medicines.init();
    }

    public static final ItemEntry<PiglinDisguise> PIGLIN_DISGUISE = TFGCore.REGISTRATE.item("piglin_disguise",
            (p) -> new PiglinDisguise(TFGBlocks.PIGLIN_DISGUISE_BLOCK.get(), p))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/piglin_disguise_block")))
            .register();

    public static final ItemEntry<SnorkelItem> SNORKEL = TFGCore.REGISTRATE.item("snorkel", SnorkelItem::new)
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<FlippersItem> FLIPPERS = TFGCore.REGISTRATE.item("flippers", FlippersItem::new)
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<SnowshoesItem> SNOWSHOES = TFGCore.REGISTRATE.item("snowshoes", SnowshoesItem::new)
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<TrowelItem> TROWEL = TFGCore.REGISTRATE.item("trowel", TrowelItem::new)
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<Item> SILICON_SEED_CRYSTAL = TFGCore.REGISTRATE.item("silicon_seed_crystal", Item::new)
            .defaultModel()
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
            .model(ModelUtils.layeredItemModel(TFGCore.id("item/progenitor_cells_0"), TFGCore.id("item/progenitor_cells_1"), TFGCore.id("item/progenitor_cells_2")))
            .register();

    public static final ItemEntry<FishRoeItem> FISH_ROE = TFGCore.REGISTRATE.item("fish_roe", FishRoeItem::new)
            .model(ModelUtils.layeredItemModel(TFGCore.id("item/fish_roe_0"), TFGCore.id("item/fish_roe_0"), TFGCore.id("item/fish_roe_1"), TFGCore.id("item/fish_roe_2")))
            .register();

    public static final ItemEntry<ForgeSpawnEggItem> MOON_RABBIT_EGG = registerSpawnEgg(TFGEntities.MOON_RABBIT, 15767516, 9756658);
    public static final ItemEntry<ForgeSpawnEggItem> GLACIAN_RAM_EGG = registerSpawnEgg(TFGEntities.GLACIAN_RAM, 16772607, 3997758);
    public static final ItemEntry<ForgeSpawnEggItem> SNIFFER_SPAWN_EGG = registerSpawnEgg(TFGEntities.SNIFFER, 11285007, 4829025);
    public static final ItemEntry<ForgeSpawnEggItem> WRAPTOR_SPAWN_EGG = registerSpawnEgg(TFGEntities.WRAPTOR, 15767516, 4829025);
    public static final ItemEntry<ForgeSpawnEggItem> SURFER_SPAWN_EGG = registerSpawnEgg(TFGEntities.SURFER, 7644045, 12824430);
    public static final ItemEntry<ForgeSpawnEggItem> LEOPARD_SEAL_SPAWN_EGG = registerSpawnEgg(TFGEntities.LEOPARD_SEAL, 0x708090, 0x2F2F2F);
    public static final ItemEntry<ForgeSpawnEggItem> BISON_SPAWN_EGG = registerSpawnEgg(TFGEntities.BISON, 0x6B4C2A, 0x2C1A0E);
    public static final ItemEntry<ForgeSpawnEggItem> JERBOA_SPAWN_EGG = registerSpawnEgg(TFGEntities.JERBOA, 0xC8A96E, 0x8B6914);
    public static final ItemEntry<ForgeSpawnEggItem> LEMMING_SPAWN_EGG = registerSpawnEgg(TFGEntities.LEMMING, 0xD4A055, 0x1A1008);
    public static final ItemEntry<ForgeSpawnEggItem> MONGOOSE_SPAWN_EGG = registerSpawnEgg(TFGEntities.MONGOOSE, 0x8C7355, 0x3D2B1A);

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> MARS_WATER_BUCKET = TFGCore.REGISTRATE.item("semiheavy_ammoniacal_water_bucket",
            p -> new BucketItem(TFGFluids.MARS_WATER.getSource(), p))
            .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .register();

    @SuppressWarnings("deprecation")
    public static final ItemEntry<BucketItem> MUDDY_WATER_BUCKET = TFGCore.REGISTRATE.item("muddy_water_bucket",
            p -> new BucketItem(TFGFluids.MUDDY_WATER.getSource(), p))
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
            .defaultModel()
            .register();

    public static final ItemEntry<Item> SNIFFER_WOOL = TFGCore.REGISTRATE.item("sniffer_wool", Item::new)
            .properties(p -> p.stacksTo(32))
            .defaultModel()
            .register();

    public static final ItemEntry<Item> SNIFFER_EGG = TFGCore.REGISTRATE.item("sniffer_egg", Item::new)
            .properties(p -> p.stacksTo(32))
            .model(ModelUtils.layeredItemModel(ResourceLocation.fromNamespaceAndPath("minecraft", "item/sniffer_egg")))
            .register();

    public static final ItemEntry<Item> WRAPTOR_WOOL = TFGCore.REGISTRATE.item("wraptor_wool", Item::new)
            .properties(p -> p.stacksTo(32))
            .defaultModel()
            .register();

    public static final ItemEntry<Item> WRAPTOR_EGG = TFGCore.REGISTRATE.item("wraptor_egg", Item::new)
            .properties(p -> p.stacksTo(32))
            .model(ModelUtils.layeredItemModel(ResourceLocation.fromNamespaceAndPath("species", "item/wraptor_egg")))
            .register();

    public static final ItemEntry<Item> WIRELESS_CARD = TFGCore.REGISTRATE.item("wireless_card",
            (p) -> Upgrades.createUpgradeCardItem(p.rarity(Rarity.UNCOMMON).stacksTo(1)))
            .model(ModelUtils.layeredItemModel(TFGCore.id("item/wireless_card/wireless_card_base"),
                    TFGCore.id("item/wireless_card/wireless_card_layer1"),
                    TFGCore.id("item/wireless_card/wireless_card_layer2")))
            .register();

    public static final ItemEntry<CartItem> RNR_PLOW = TFGCore.REGISTRATE.item("rnr_plow", CartItem::new)
            .defaultModel()
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

    public static ItemEntry<ComponentItem> COVER_ROTTEN_VOIDING = TFGCore.REGISTRATE
            .item("rotten_voiding_cover", ComponentItem::create)
            .onRegister(item -> item.attachComponents(new CoverPlaceBehavior(TFGCovers.ITEM_VOIDING_ROTTEN)))
            .register();

    public static ItemEntry<Item> ALFISOL_MUD_BRICK = TFGCore.REGISTRATE.item("mud_brick/alfisol", Item::new).defaultModel().register();
    public static ItemEntry<Item> MOLLISOL_MUD_BRICK = TFGCore.REGISTRATE.item("mud_brick/mollisol", Item::new).defaultModel().register();
    public static ItemEntry<Item> OXISOL_MUD_BRICK = TFGCore.REGISTRATE.item("mud_brick/oxisol", Item::new).defaultModel().register();
    public static ItemEntry<Item> PODZOL_MUD_BRICK = TFGCore.REGISTRATE.item("mud_brick/podzol", Item::new).defaultModel().register();

    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent components) {
        return item -> item.attachComponents(components);
    }

    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    private static <T extends Mob> ItemEntry<ForgeSpawnEggItem> registerSpawnEgg(EntityEntry<T> entity, int color1, int color2) {
        return TFGCore.REGISTRATE.item("spawn_egg/" + entity.getId().getPath(),
                (p) -> new ForgeSpawnEggItem(entity, color1, color2, p))
                .setData(ProviderType.ITEM_MODEL, (ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/template_spawn_egg")))
                .register();
    }
}
