package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.gregtechceu.gtceu.common.data.materials.GTFoods;
import com.gregtechceu.gtceu.common.item.AntidoteBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import su.terrafirmagreg.core.TFGCore;

public class TFGTItems {

    private static final int shortBuff = 8 * 60 * 20;
    private static final int longBuff = 30 * 60 * 20;

    public static void init() {
    }

    // Medical condition pills
    public static final ItemEntry<ComponentItem> ANTIPOISON_PILL = TFGCore.REGISTRATE.item("antipoison_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.POISON, GTMedicalConditions.WEAK_POISON, GTMedicalConditions.NAUSEA)))
            .register();
    public static final ItemEntry<ComponentItem> ANTIPOISON_TABLET = TFGCore.REGISTRATE.item("antipoison_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(50, GTMedicalConditions.POISON, GTMedicalConditions.WEAK_POISON, GTMedicalConditions.NAUSEA)))
            .register();

    public static final ItemEntry<ComponentItem> WATER_BREATHING_PILL = TFGCore.REGISTRATE.item("water_breathing_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.CARBON_MONOXIDE_POISONING, GTMedicalConditions.METHANOL_POISONING)))
            .register();

    public static final ItemEntry<ComponentItem> WATER_BREATHING_TABLET = TFGCore.REGISTRATE.item("water_breathing_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, longBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(50, GTMedicalConditions.CARBON_MONOXIDE_POISONING, GTMedicalConditions.METHANOL_POISONING)))
            .register();

    public static final ItemEntry<ComponentItem> POISON_PILL = TFGCore.REGISTRATE.item("poison_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.POISON, 15 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.ARSENICOSIS, GTMedicalConditions.BERYLLIOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> POISON_TABLET = TFGCore.REGISTRATE.item("poison_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.POISON, 8 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(50, GTMedicalConditions.ARSENICOSIS, GTMedicalConditions.BERYLLIOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> SLOWNESS_PILL = TFGCore.REGISTRATE.item("slowness_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.ASBESTOSIS, GTMedicalConditions.SILICOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> SLOWNESS_TABLET = TFGCore.REGISTRATE.item("slowness_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(50, GTMedicalConditions.ASBESTOSIS, GTMedicalConditions.SILICOSIS)))
            .register();

    public static final ItemEntry<ComponentItem> WEAKNESS_PILL = TFGCore.REGISTRATE.item("weakness_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 3 * 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(10, GTMedicalConditions.CHEMICAL_BURNS, GTMedicalConditions.IRRITANT)))
            .register();

    public static final ItemEntry<ComponentItem> WEAKNESS_TABLET = TFGCore.REGISTRATE.item("weakness_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 60 * 20, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(50, GTMedicalConditions.CHEMICAL_BURNS, GTMedicalConditions.IRRITANT)))
            .register();

    public static final ItemEntry<ComponentItem> FIRE_RESISTANCE_SALVO = TFGCore.REGISTRATE.item("fire_resistance_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(30, GTMedicalConditions.CHEMICAL_BURNS)))
            .register();

    public static final ItemEntry<ComponentItem> RESISTANCE_SALVO = TFGCore.REGISTRATE.item("resistance_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, shortBuff, 0), 1).build()))
            .onRegister(attach(new AntidoteBehavior(30, GTMedicalConditions.IRRITANT)))
            .register();

    // Normal effect pills
    public static final ItemEntry<ComponentItem> HASTE_PILL = TFGCore.REGISTRATE.item("haste_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, shortBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> HASTE_TABLET = TFGCore.REGISTRATE.item("haste_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, longBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> NIGHT_VISION_PILL = TFGCore.REGISTRATE.item("night_vision_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, shortBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> NIGHT_VISION_TABLET = TFGCore.REGISTRATE.item("night_vision_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, longBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> REGENERATION_PILL = TFGCore.REGISTRATE.item("regeneration_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.REGENERATION, shortBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> REGENERATION_TABLET = TFGCore.REGISTRATE.item("regeneration_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.REGENERATION, longBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> SPEED_PILL = TFGCore.REGISTRATE.item("speed_pill", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, shortBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> SPEED_TABLET = TFGCore.REGISTRATE.item("speed_tablet", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, longBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> ABSORPTION_SALVO = TFGCore.REGISTRATE.item("absorption_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, shortBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> INVISIBILITY_SALVO = TFGCore.REGISTRATE.item("invisibility_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.INVISIBILITY, shortBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> LUCK_SALVO = TFGCore.REGISTRATE.item("luck_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.LUCK, longBuff, 0), 1).build()))
            .register();

    public static final ItemEntry<ComponentItem> INSTANT_HEALTH_SALVO = TFGCore.REGISTRATE.item("instant_health_salvo", ComponentItem::create)
            .properties(p -> p.food(new FoodProperties.Builder().alwaysEat().fast().effect(() -> new MobEffectInstance(MobEffects.HEAL, 10, 1), 1).build()))
            .register();

    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent components) {
        return item -> item.attachComponents(components);
    }

    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }
}
