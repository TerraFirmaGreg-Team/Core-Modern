package su.terrafirmagreg.tfcambiental.api;

import java.util.Locale;
import java.util.Optional;

import net.dries007.tfc.common.blockentities.*;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.item.TFCAmbientalItems;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface BlockEntityTemperatureProvider {
    Optional<TempModifier> getModifier(Player player, BlockEntity entity);

    static void evaluateAll(Player player, TempModifierStorage storage) {
        BlockTemperatureProvider.evaluateAll(player, storage);
    }

    private static boolean hasProtection(Player player) {
        var item = CuriosApi.getCuriosHelper().findCurios(player, TFCAmbientalItems.LEATHER_APRON.get());
        if (item.isEmpty()) {
            return false;
        }
        float environmentTemperature = EnvironmentalTemperatureProvider.getEnvironmentTemperatureWithTimeOfDay(player);
        float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        return environmentTemperature > AVERAGE;
    }

    static Optional<TempModifier> handleCharcoalForge(Player player, BlockEntity entity) {
        if (entity instanceof CharcoalForgeBlockEntity forge) {

            float temp = forge.getTemperature();
            float change = temp / 140f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("charcoal_forge", change, 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleFirePit(Player player, BlockEntity entity) {
        if (entity instanceof FirepitBlockEntity pit) {
            float temp = pit.getTemperature();
            float change = temp / 100f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("fire_pit", Math.min(6f, change), 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handlePot(Player player, BlockEntity entity) {
        if (entity instanceof PotBlockEntity pit) {
            float temp = pit.getTemperature();
            float change = temp / 100f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("pot", Math.min(6f, change), 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleGrill(Player player, BlockEntity entity) {
        if (entity instanceof GrillBlockEntity pit) {
            float temp = pit.getTemperature();
            float change = temp / 100f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("grill", Math.min(6f, change), 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleBloomery(Player player, BlockEntity entity) {
        if (entity instanceof BloomeryBlockEntity bloomery) {
            float change = bloomery.getRemainingTicks() > 0 ? 4f : 0f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("bloomery", change, 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleLitBlock(Player player, BlockEntity entity) {
        if (entity.getBlockState().hasProperty(BlockStateProperties.LIT) && entity.getBlockState().getValue(BlockStateProperties.LIT)) {
            float change = 3f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined("lit_block", change, 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleIHeatBlock(Player player, BlockEntity entity) {
        return entity.getCapability(HeatCapability.BLOCK_CAPABILITY).map(cap -> new TempModifier(entity.getClass().getName().toLowerCase(Locale.ROOT), cap.getTemperature() / 140f, 0));
    }
}
