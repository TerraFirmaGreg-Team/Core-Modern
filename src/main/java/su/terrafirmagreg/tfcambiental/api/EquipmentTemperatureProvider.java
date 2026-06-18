package su.terrafirmagreg.tfcambiental.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.simibubi.create.AllItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.registries.ForgeRegistries;

import earth.terrarium.adastra.common.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.item.material.TemperatureAlteringMaterial;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface EquipmentTemperatureProvider {
    List<EquipmentTemperatureProvider> PROVIDERS = List.of(
            EquipmentTemperatureProvider::handleSunlightCap,
            EquipmentTemperatureProvider::handleClothes,
            EquipmentTemperatureProvider::getEquipmentTempModifier);

    Optional<TempModifier> getModifier(Player player, ItemStack stack);

    static void evaluateAll(Player player, TempModifierStorage storage) {
        List<ItemStack> equipment = new ArrayList<>();

        CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(c -> {
            for (int i = 0; i < c.getSlots(); i++) {
                equipment.add(c.getStackInSlot(i));
            }
        });

        equipment.add(player.getItemBySlot(EquipmentSlot.HEAD));
        equipment.add(player.getItemBySlot(EquipmentSlot.CHEST));
        equipment.add(player.getItemBySlot(EquipmentSlot.LEGS));
        equipment.add(player.getItemBySlot(EquipmentSlot.FEET));

        for (ItemStack stack : equipment) {
            for (EquipmentTemperatureProvider provider : PROVIDERS) {
                storage.add(provider.getModifier(player, stack));
            }
        }
    }

    static Optional<TempModifier> handleClothes(Player player, ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem clothesItem) {
            if (clothesItem.getMaterial() instanceof TemperatureAlteringMaterial tempMaterial) {
                return Optional.of(tempMaterial.getTempModifier(stack));
            }
            if (clothesItem.getMaterial() instanceof com.lumintorious.tfcambiental.item.material.TemperatureAlteringMaterial tempMaterial) {
                return Optional.of(tempMaterial.getTempModifier(stack)); // for tfc textile
            }
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleSunlightCap(Player player, ItemStack stack) {
        float average = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        if (stack.is(TFCAmbiental.SUNBLOCKING_APPAREL)) {
            if (player.level().getBrightness(LightLayer.SKY, player.getOnPos().above()) > 14) {
                float envTemp = EnvironmentalTemperatureProvider.getEnvironmentTemperatureWithTimeOfDay(player);
                if (envTemp > average) {
                    return TempModifier.defined(Math.min(6f, envTemp - average), 0f);
                }
                return TempModifier.defined(-0.1f, 0f);
            }
        }
        return TempModifier.none();
    }

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
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId != null && "blue_steel_toe_hiking_boots".equals(itemId.getPath())) {
            return Optional.of(new TempModifier(-2f, 0.2F));
        }
        if (itemId != null && "red_steel_toe_hiking_boots".equals(itemId.getPath())) {
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

    static final java.util.Set<Item> COPPER_DIVING_SUIT = java.util.Set.of(
            AllItems.COPPER_DIVING_HELMET.get(),
            AllItems.COPPER_DIVING_BOOTS.get(),
            AllItems.COPPER_BACKTANK.get());

    static final java.util.Set<Item> BLUE_STEEL_DIVING_SUIT = java.util.Set.of(
            AllItems.NETHERITE_DIVING_HELMET.get(),
            AllItems.NETHERITE_DIVING_BOOTS.get(),
            AllItems.NETHERITE_BACKTANK.get(),
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS);

    static final java.util.Set<Item> ADVANCED_ARMOR = java.util.Set.of(
            GTItems.NANO_HELMET.get(),
            GTItems.NANO_CHESTPLATE.get(),
            GTItems.NANO_LEGGINGS.get(),
            GTItems.NANO_BOOTS.get(),
            GTItems.NANO_CHESTPLATE_ADVANCED.get(),

            GTItems.QUANTUM_HELMET.get(),
            GTItems.QUANTUM_CHESTPLATE.get(),
            GTItems.QUANTUM_LEGGINGS.get(),
            GTItems.QUANTUM_BOOTS.get(),
            GTItems.QUANTUM_CHESTPLATE_ADVANCED.get(),

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

    float FULLY_INSULATED = -0.5f;
}
