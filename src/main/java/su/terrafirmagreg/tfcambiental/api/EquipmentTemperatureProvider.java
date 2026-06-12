package su.terrafirmagreg.tfcambiental.api;

import java.util.Optional;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

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
                    return TempModifier.defined("sunlight_protection", diff * -0.2f, -0.5f);
                }
            }
        }
        return TempModifier.none();
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
