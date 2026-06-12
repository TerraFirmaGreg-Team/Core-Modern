package su.terrafirmagreg.tfcambiental.api;

import java.util.Optional;
import java.util.Set;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.simibubi.create.AllItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;

import earth.terrarium.adastra.common.registry.ModItems;
import mod.traister101.sns.common.items.SNSItems;
import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.item.material.TemperatureAlteringMaterial;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface EquipmentTemperatureProvider {
    Optional<TempModifier> getModifier(Player player, ItemStack stack);

    static Optional<TempModifier> handleClothes(Player player, ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem clothesItem) {
            if (clothesItem.getMaterial() instanceof TemperatureAlteringMaterial tempMaterial) {
                return Optional.of(tempMaterial.getTempModifier(stack));
            }
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleSunlightCap(Player player, ItemStack stack) {
        float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if (stack.is(TFCAmbiental.SUNBLOCKING_APPAREL)) {
            if (player.level().getBrightness(LightLayer.SKY, player.getOnPos().above()) > 14) {
                float envTemp = EnvironmentalTemperatureProvider.getEnvironmentTemperatureWithTimeOfDay(player);
                if (envTemp > AVERAGE) {
                    float diff = envTemp - AVERAGE;
                    Optional<TempModifier> helmetMod = handleClothes(player, stack);
                    if (helmetMod.isPresent()) {
                        diff -= helmetMod.get().getChange();
                    } else {
                        diff -= 1;
                    }
                    return TempModifier.defined(diff * -0.2f, -0.5f);
                }
            }
        }
        return TempModifier.none();
    }

    static final float FULLY_INSULATED = -10F;

    static final Set<Item> COPPER_DIVING_SUIT = Set.of(
            AllItems.COPPER_DIVING_HELMET.get(),
            AllItems.COPPER_DIVING_BOOTS.get(),
            AllItems.COPPER_BACKTANK.get());

    static final Set<Item> BLUE_STEEL_DIVING_SUIT = Set.of(
            AllItems.NETHERITE_DIVING_HELMET.get(),
            AllItems.NETHERITE_DIVING_BOOTS.get(),
            AllItems.NETHERITE_BACKTANK.get(),
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS);

    static final Set<Item> ADVANCED_ARMOR = Set.of(
            // Nano armor
            GTItems.NANO_HELMET.get(),
            GTItems.NANO_CHESTPLATE.get(),
            GTItems.NANO_LEGGINGS.get(),
            GTItems.NANO_BOOTS.get(),
            GTItems.NANO_CHESTPLATE_ADVANCED.get(),

            // Quantum armor
            GTItems.QUANTUM_HELMET.get(),
            GTItems.QUANTUM_CHESTPLATE.get(),
            GTItems.QUANTUM_LEGGINGS.get(),
            GTItems.QUANTUM_BOOTS.get(),
            GTItems.QUANTUM_CHESTPLATE_ADVANCED.get(),

            // Space suits
            ModItems.SPACE_HELMET.get(),
            ModItems.SPACE_SUIT.get(),
            ModItems.SPACE_PANTS.get(),
            ModItems.SPACE_BOOTS.get(),
            ModItems.NETHERITE_SPACE_HELMET.get(),
            ModItems.NETHERITE_SPACE_SUIT.get(),
            ModItems.NETHERITE_SPACE_PANTS.get(),
            ModItems.NETHERITE_SPACE_BOOTS.get(),
            ModItems.JET_SUIT_HELMET.get(),
            ModItems.JET_SUIT.get(),
            ModItems.JET_SUIT_PANTS.get(),
            ModItems.JET_SUIT_BOOTS.get());

    static Optional<TempModifier> getEquipmentTempModifier(Player player, ItemStack stack) {
        Item item = stack.getItem();

        if (COPPER_DIVING_SUIT.contains(item)) {
            return Optional.of(new TempModifier(-1F, 0.1F));
        }
        if (BLUE_STEEL_DIVING_SUIT.contains(item)) {
            return Optional.of(new TempModifier(-3F, 0.9F));
        }
        if (ADVANCED_ARMOR.contains(item)) {
            return Optional.of(new TempModifier(0F, FULLY_INSULATED));
        }
        if (item == SNSItems.BLUE_STEEL_TOE_HIKING_BOOTS.get()) {
            return Optional.of(new TempModifier(-2f, 0.2F));
        }
        if (item == SNSItems.RED_STEEL_TOE_HIKING_BOOTS.get()) {
            return Optional.of(new TempModifier(2f, 0.2F));
        }

        return Optional.empty();
    }

    /** Checks if the player is wearing a full suit in their 4 armor slots. */
    public static boolean isFullyInsulated(Player player) {
        var head = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
        var chest = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        var legs = player.getItemBySlot(EquipmentSlot.LEGS).getItem();
        var feet = player.getItemBySlot(EquipmentSlot.FEET).getItem();

        return ADVANCED_ARMOR.contains(head) && ADVANCED_ARMOR.contains(chest)
                && ADVANCED_ARMOR.contains(legs) && ADVANCED_ARMOR.contains(feet);
    }

    static void evaluateAll(Player player, TempModifierStorage storage) {
        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); i++) {
                ItemStack stack = c.getStackInSlot(i);
                for (var fn : AmbientalRegistry.EQUIPMENT) {
                    storage.add(fn.getModifier(player, stack));
                }
            }
        });
        for (var fn : AmbientalRegistry.EQUIPMENT) {
            storage.add(fn.getModifier(player, player.getItemBySlot(EquipmentSlot.HEAD)));
            storage.add(fn.getModifier(player, player.getItemBySlot(EquipmentSlot.CHEST)));
            storage.add(fn.getModifier(player, player.getItemBySlot(EquipmentSlot.LEGS)));
            storage.add(fn.getModifier(player, player.getItemBySlot(EquipmentSlot.FEET)));
        }
    }

    static ItemStack getEquipmentByType(Player player, ArmorItem.Type type) {
        var feetArmor = player.getItemBySlot(EquipmentSlot.FEET);
        if (!feetArmor.isEmpty() && feetArmor.getItem() instanceof ArmorItem armorItem && armorItem.getType().equals(type)) {
            return feetArmor;
        }
        return CuriosApi.getCuriosHelper().getEquippedCurios(player).map(c -> {
            for (int i = 0; i < c.getSlots(); i++) {
                ItemStack stack = c.getStackInSlot(i);
                if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getType().equals(type)) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }
}
