package su.terrafirmagreg.core.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.AntidoteBehavior;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;

import appeng.api.upgrades.Upgrades;
import de.mennomax.astikorcarts.item.CartItem;
import earth.terrarium.adastra.common.items.vehicles.RocketItem;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.items.*;
import su.terrafirmagreg.core.common.data.tfgt.TFGTMedicalConditions;
import su.terrafirmagreg.core.utils.ModelUtils;

/**
 * Uncomment TFGCreativeTab in TFGCore if you register anything new here
 */

@SuppressWarnings("unused")
public class TFGItems {

    private static final int shortBuff = 8 * 60 * 20;
    private static final int longBuff = 30 * 60 * 20;

    public static void init() {
    }

    public static final ItemEntry<PiglinDisguise> PIGLIN_DISGUISE = TFGCore.REGISTRATE.item("piglin_disguise",
            (p) -> new PiglinDisguise(TFGBlocks.PIGLIN_DISGUISE_BLOCK.get(), p))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/piglin_disguise_block")))
            .register();

    public static final ItemEntry<TrowelItem> TROWEL = TFGCore.REGISTRATE.item("trowel", TrowelItem::new)
            .properties(p -> p.stacksTo(1))
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

    public static final ItemEntry<ComponentItem> ANTIPOISON_PILL = TFGCore.REGISTRATE.item("antipoison_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.POISON, GTMedicalConditions.WEAK_POISON, GTMedicalConditions.NAUSEA)))
            .register();
    public static final ItemEntry<ComponentItem> ANTIPOISON_TABLET = TFGCore.REGISTRATE.item("antipoison_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(80, GTMedicalConditions.POISON, GTMedicalConditions.WEAK_POISON, GTMedicalConditions.NAUSEA)))
            .register();

    public static final ItemEntry<ComponentItem> WATER_BREATHING_PILL = TFGCore.REGISTRATE.item("water_breathing_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, shortBuff, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.UNLUCK, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.CARBON_MONOXIDE_POISONING, GTMedicalConditions.METHANOL_POISONING)))
            .register();
    public static final ItemEntry<ComponentItem> WATER_BREATHING_TABLET = TFGCore.REGISTRATE.item("water_breathing_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, longBuff, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.UNLUCK, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(80, GTMedicalConditions.CARBON_MONOXIDE_POISONING, GTMedicalConditions.METHANOL_POISONING)))
            .register();

    public static final ItemEntry<ComponentItem> POISON_PILL = TFGCore.REGISTRATE.item("poison_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.POISON, 15 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.ARSENICOSIS, GTMedicalConditions.BERYLLIOSIS)))
            .register();
    public static final ItemEntry<ComponentItem> POISON_TABLET = TFGCore.REGISTRATE.item("poison_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.POISON, 8 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(80, GTMedicalConditions.ARSENICOSIS, GTMedicalConditions.BERYLLIOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> SLOWNESS_PILL = TFGCore.REGISTRATE.item("slowness_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.ASBESTOSIS, GTMedicalConditions.SILICOSIS)))
            .register();
    public static final ItemEntry<ComponentItem> SLOWNESS_TABLET = TFGCore.REGISTRATE.item("slowness_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(80, GTMedicalConditions.ASBESTOSIS, GTMedicalConditions.SILICOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> WEAKNESS_PILL = TFGCore.REGISTRATE.item("weakness_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.CHEMICAL_BURNS, GTMedicalConditions.IRRITANT)))
            .register();
    public static final ItemEntry<ComponentItem> WEAKNESS_TABLET = TFGCore.REGISTRATE.item("weakness_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(80, GTMedicalConditions.CHEMICAL_BURNS, GTMedicalConditions.IRRITANT)))
            .register();

    public static final ItemEntry<ComponentItem> HASTE_PILL = TFGCore.REGISTRATE.item("haste_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(5, GTMedicalConditions.WEAK_POISON)))
            .register();
    public static final ItemEntry<ComponentItem> HASTE_TABLET = TFGCore.REGISTRATE.item("haste_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, longBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.WEAK_POISON)))
            .register();

    public static final ItemEntry<ComponentItem> NIGHT_VISION_PILL = TFGCore.REGISTRATE.item("night_vision_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(5, GTMedicalConditions.NAUSEA)))
            .register();
    public static final ItemEntry<ComponentItem> NIGHT_VISION_TABLET = TFGCore.REGISTRATE.item("night_vision_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, longBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.NAUSEA)))
            .register();

    public static final ItemEntry<ComponentItem> REGENERATION_PILL = TFGCore.REGISTRATE.item("regeneration_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, shortBuff, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(5, GTMedicalConditions.CHEMICAL_BURNS)))
            .register();
    public static final ItemEntry<ComponentItem> REGENERATION_TABLET = TFGCore.REGISTRATE.item("regeneration_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, longBuff, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.CHEMICAL_BURNS)))
            .register();

    public static final ItemEntry<ComponentItem> SPEED_PILL = TFGCore.REGISTRATE.item("speed_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(5, GTMedicalConditions.IRRITANT)))
            .register();
    public static final ItemEntry<ComponentItem> SPEED_TABLET = TFGCore.REGISTRATE.item("speed_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, longBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(20, GTMedicalConditions.IRRITANT)))
            .register();

    public static final ItemEntry<ComponentItem> ABSORPTION_SALVO = TFGCore.REGISTRATE.item("absorption_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(40, GTMedicalConditions.CHEMICAL_BURNS)))
            .register();

    public static final ItemEntry<ComponentItem> INVISIBILITY_SALVO = TFGCore.REGISTRATE.item("invisibility_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.INVISIBILITY, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(40, GTMedicalConditions.ARSENICOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> LUCK_SALVO = TFGCore.REGISTRATE.item("luck_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.LUCK, longBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(40, GTMedicalConditions.ASBESTOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> INSTANT_HEALTH_SALVO = TFGCore.REGISTRATE.item("instant_health_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 1), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(40, GTMedicalConditions.CHEMICAL_BURNS)))
            .register();

    public static final ItemEntry<ComponentItem> FIRE_RESISTANCE_SALVO = TFGCore.REGISTRATE.item("fire_resistance_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(40, GTMedicalConditions.CHEMICAL_BURNS)))
            .register();

    public static final ItemEntry<ComponentItem> RESISTANCE_SALVO = TFGCore.REGISTRATE.item("resistance_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(40, GTMedicalConditions.IRRITANT)))
            .register();

    public static final ItemEntry<ComponentItem> PARACETAMOL_PILL = TFGCore.REGISTRATE.item("paracetamol_pill", ComponentItem::create)
            .model(ModelUtils.layeredItemModel(GTCEu.id("item/paracetamol_pill")))
            .properties(p -> p.food(GTFoods.ANTIDOTE))
            .onRegister(attach(new AntidoteBehavior(30,
                    GTMedicalConditions.CHEMICAL_BURNS,
                    GTMedicalConditions.WEAK_POISON,
                    GTMedicalConditions.POISON,
                    GTMedicalConditions.NAUSEA,
                    GTMedicalConditions.IRRITANT,
                    GTMedicalConditions.METHANOL_POISONING,
                    GTMedicalConditions.CARBON_MONOXIDE_POISONING)))
            .onRegister(attach(new AntidoteBehavior(10,
                    GTMedicalConditions.CARCINOGEN)))
            .onRegister(attach(new AntidoteBehavior(5,
                    TFGTMedicalConditions.RADIOACTIVE)))
            .register();

    public static final ItemEntry<ComponentItem> RAD_AWAY_PILL = TFGCore.REGISTRATE.item("rad_away_pill", ComponentItem::create)
            .model(ModelUtils.layeredItemModel(GTCEu.id("item/rad_away_pill")))
            .properties(p -> p.food(GTFoods.ANTIDOTE))
            .onRegister(attach(new AntidoteBehavior(-1,
                    GTMedicalConditions.CHEMICAL_BURNS,
                    GTMedicalConditions.WEAK_POISON,
                    GTMedicalConditions.POISON,
                    GTMedicalConditions.NAUSEA,
                    GTMedicalConditions.IRRITANT,
                    GTMedicalConditions.METHANOL_POISONING,
                    GTMedicalConditions.CARBON_MONOXIDE_POISONING,
                    GTMedicalConditions.ASBESTOSIS,
                    GTMedicalConditions.ARSENICOSIS,
                    GTMedicalConditions.SILICOSIS,
                    GTMedicalConditions.BERYLLIOSIS,
                    GTMedicalConditions.CARCINOGEN,
                    TFGTMedicalConditions.RADIOACTIVE)))
            .register();

    public static ItemEntry<ComponentItem> COVER_ROTTEN_VOIDING = TFGCore.REGISTRATE
            .item("rotten_voiding_cover", ComponentItem::create)
            .register();

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
