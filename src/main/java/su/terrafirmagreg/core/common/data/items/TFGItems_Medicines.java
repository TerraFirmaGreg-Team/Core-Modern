package su.terrafirmagreg.core.common.data.items;

import static su.terrafirmagreg.core.common.data.items.TFGItems.attach;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.AntidoteBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.dries007.tfc.common.TFCEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGEffects;
import su.terrafirmagreg.core.common.data.tfgt.TFGMedicalConditions;
import su.terrafirmagreg.core.utils.ModelUtils;

public class TFGItems_Medicines {

    private static final int shortBuff = 8 * 60 * 20;
    private static final int longBuff = 30 * 60 * 20;

    public static void init() {
    }

    // Powders
    public static final ItemEntry<ComponentItem> COMBAT_POWDER = TFGCore.REGISTRATE.item("combat_powder", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60 * 20, 1), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60 * 20, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 3), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 10 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.CHEMICAL_BURNS, GTMedicalConditions.IRRITANT)))
            .register();
    public static final ItemEntry<ComponentItem> RECOVERY_POWDER = TFGCore.REGISTRATE.item("recovery_powder", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.HEAL, 1, 1), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 60 * 20, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 20, 1), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.CHEMICAL_BURNS, GTMedicalConditions.IRRITANT)))
            .register();
    public static final ItemEntry<ComponentItem> ACROBAT_POWDER = TFGCore.REGISTRATE.item("acrobat_powder", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 2), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, 30 * 20, 1), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 * 20, 1), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 60 * 20, 1), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.SILICOSIS, GTMedicalConditions.ARSENICOSIS, GTMedicalConditions.ASBESTOSIS)))
            .register();
    public static final ItemEntry<ComponentItem> MINING_POWDER = TFGCore.REGISTRATE.item("mining_powder", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 3 * 60 * 20, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 3 * 60 * 20, 0), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 3 * 60 * 20, 2), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.SILICOSIS, GTMedicalConditions.ARSENICOSIS, GTMedicalConditions.ASBESTOSIS)))
            .register();

    // Pills & Tablets
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

    // Contaminant Capsules
    public static final ItemEntry<ComponentItem> PARASITICIDE_CAPSULE = TFGCore.REGISTRATE.item("parasiticide_capsule", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(TFGEffects.CURE_PARASITES.get(), 20 * 150, 1, false, false), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20 * 20, 4), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20 * 60 * 5, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(30, GTMedicalConditions.IRRITANT)))
            .register();

    public static final ItemEntry<ComponentItem> DETOX_CAPSULE = TFGCore.REGISTRATE.item("detox_capsule", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(TFGEffects.CURE_TOXINS.get(), 20 * 150, 1, false, false), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 20 * 60 * 5, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(-1, GTMedicalConditions.POISON, GTMedicalConditions.WEAK_POISON)))
            .register();

    public static final ItemEntry<ComponentItem> NANOFILTRATION_CAPSULE = TFGCore.REGISTRATE.item("nanofiltration_capsule", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast()
                    .effect(() -> new MobEffectInstance(TFGEffects.CURE_MICROPLASTICS.get(), 20 * 300, 0, false, false), 1)
                    .effect(() -> new MobEffectInstance(TFCEffects.THIRST.get(), 20 * 60 * 2, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(60,
                    GTMedicalConditions.POISON,
                    GTMedicalConditions.WEAK_POISON,
                    GTMedicalConditions.ARSENICOSIS,
                    GTMedicalConditions.ASBESTOSIS,
                    GTMedicalConditions.BERYLLIOSIS,
                    GTMedicalConditions.SILICOSIS)))
            .register();

    // Salvos
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

    // Antidotes
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
                    TFGMedicalConditions.RADIOACTIVE)))
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
                    TFGMedicalConditions.RADIOACTIVE)))
            .register();
}
