package su.terrafirmagreg.tfcambiental.api;

import java.util.List;
import java.util.Optional;

import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface ItemTemperatureProvider {
    List<ItemTemperatureProvider> PROVIDERS = List.of(
            ItemTemperatureProvider::handleTemperatureCapability,
            ItemTemperatureProvider::handleHotIngots);

    Optional<TempModifier> getModifier(Player player, ItemStack stack);

    static void evaluateAll(Player player, TempModifierStorage modifiers) {
        for (ItemStack stack : player.getInventory().items) {
            for (ItemTemperatureProvider provider : PROVIDERS) {
                modifiers.add(provider.getModifier(player, stack));
            }
        }
    }

    static Optional<TempModifier> handleTemperatureCapability(Player player, ItemStack stack) {
        return stack.getCapability(HeatCapability.CAPABILITY).map(cap -> {
            float temp = cap.getTemperature() / 800;
            return new TempModifier(temp, 0.1f * stack.getCount());
        });
    }

    static Optional<TempModifier> handleHotIngots(Player player, ItemStack stack) {
        return stack.is(TFCAmbiental.HOT_INGOTS) ? Optional.of(new TempModifier(TFCAmbientalConfig.COMMON.hotIngotTemperature.get().floatValue(), 0.1f * stack.getCount()))
                : TempModifier.none();
    }
}
