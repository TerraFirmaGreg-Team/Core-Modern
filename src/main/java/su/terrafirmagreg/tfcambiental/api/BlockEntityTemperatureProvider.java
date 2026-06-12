package su.terrafirmagreg.tfcambiental.api;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.dries007.tfc.common.blockentities.*;
import net.dries007.tfc.common.blocks.SeaIceBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.RegistryObject;

import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.core.common.data.tfgt.TFGMachines;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.api.BlockEntityTemperatureProvider.TempModifierSpec;
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

    record TempModifierSpec(String name, float change, float potency) {
        Optional<TempModifier> create() {
            return Optional.of(new TempModifier(name, change, potency));
        }
    }

    /* Blocks that affect temperature at all times */
    static final Map<Block, TempModifierSpec> SIMPLE_BLOCKS = Stream.concat(
            Stream.of(
                    Map.entry(Blocks.PACKED_ICE, new TempModifierSpec("packed_ice", -6.0F, 1.0F)),
                    Map.entry(Blocks.BLUE_ICE, new TempModifierSpec("blue_ice", -8.0F, 1.0F))),
            TFCBlocks.MAGMA_BLOCKS.values().stream()
                    .map(RegistryObject::get)
                    .map(block -> Map.entry(
                            block,
                            new TempModifierSpec("magma_rock", 5.0F, 1.0F))))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    /* Blocks that affect temperature when GTBlockStateProperties.ACTIVE */
    static final Map<Block, TempModifierSpec> ACTIVE_BLOCKS = Map.ofEntries(
            // Boilers
            Map.entry(GTBlocks.FIREBOX_BRONZE.get(), new TempModifierSpec("bronze_firebox", 6.0F, 3.0F)),
            Map.entry(GTBlocks.FIREBOX_STEEL.get(), new TempModifierSpec("steel_firebox", 8.0F, 3.0F)),
            Map.entry(GTBlocks.FIREBOX_TITANIUM.get(), new TempModifierSpec("titanium_firebox", 10.0F, 3.0F)),
            Map.entry(GTBlocks.FIREBOX_TUNGSTENSTEEL.get(), new TempModifierSpec("tungstensteel_firebox", 12.0F, 3.0F)),

            // Coils
            Map.entry(GTBlocks.COIL_CUPRONICKEL.get(), new TempModifierSpec("cupronickel_coil", 18.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_KANTHAL.get(), new TempModifierSpec("kanthal_coil", 28.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_NICHROME.get(), new TempModifierSpec("nichrome_coil", 38.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_RTMALLOY.get(), new TempModifierSpec("tungstensteel_coil", 48.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_HSSG.get(), new TempModifierSpec("hssg_coil", 58.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_NAQUADAH.get(), new TempModifierSpec("naquadah_coil", 78.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_TRINIUM.get(), new TempModifierSpec("trinium_coil", 88.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_TRITANIUM.get(), new TempModifierSpec("tritanium_coil", 98.0F, 3.0F)));

    /* Blocks that affect temperature when cap.isActive() */
    static final Map<Block, TempModifierSpec> CAPABILITY_BLOCKS = Stream.of(
            Stream.of(
                    Map.entry(
                            GTMachines.STEAM_SOLID_BOILER.right().getBlock(),
                            new TempModifierSpec("steam_solid_boiler", 5.0F, 2.0F)),
                    Map.entry(
                            GTMachines.STEAM_LIQUID_BOILER.right().getBlock(),
                            new TempModifierSpec("steam_liquid_boiler", 5.0F, 2.0F)),
                    Map.entry(
                            GTMachines.STEAM_FURNACE.right().getBlock(),
                            new TempModifierSpec("steam_furnace", 8.0F, 2.0F)),
                    Map.entry(
                            GTMachines.STEAM_ALLOY_SMELTER.right().getBlock(),
                            new TempModifierSpec("steam_alloy_smelter", 6.0F, 2.0F))),

            Arrays.stream(GTMachines.ELECTRIC_FURNACE)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec("electric_furnace", 10.0F, 3.0F))),

            Arrays.stream(GTMachines.ARC_FURNACE)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec("arc_furnace", 12.0F, 3.0F))),

            Arrays.stream(GTMachines.ALLOY_SMELTER)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec("alloy_smelter", 9.0F, 3.0F))),

            Arrays.stream(GTMachines.FLUID_HEATER)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec("fluid_heater", 11.0F, 3.0F))),

            Arrays.stream(TFGMachines.FOOD_OVEN)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec("food_oven", 7.0F, 2.0F))),

            Arrays.stream(TFGMachines.FOOD_REFRIGERATOR)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec("refrigerator", 5.0F, 1.0F))))
            .flatMap(Function.identity())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * Determines if the given block should modify player temperature
     * @return Optional TempModifier
     */
    static Optional<TempModifier> getBlockTempModifier(Player player, BlockPos blockPos, BlockState state) {
        Block block = state.getBlock();

        TempModifierSpec spec = SIMPLE_BLOCKS.get(block);
        if (spec != null)
            return spec.create();

        spec = ACTIVE_BLOCKS.get(block);
        if (spec != null && state.getValue(GTBlockStateProperties.ACTIVE)) {
            return spec.create();
        }

        spec = CAPABILITY_BLOCKS.get(block);
        if (spec != null) {
            var cap = GTCapabilityHelper.getRecipeLogic(player.level(), blockPos, null);
            if (cap != null && cap.isActive()) {
                return spec.create();
            }
        }

        // Special cases
        if (block instanceof AqueductBlock aqueduct) {
            var fluid = state.getValue(aqueduct.getFluidProperty()).getFluid();
            if (fluid == Fluids.LAVA) {
                return Optional.of(new TempModifier("aqueduct_lava", 5.0F, 1.0F));
            } else if (fluid == TFCFluids.SPRING_WATER.getFlowing()) {
                return Optional.of(new TempModifier("aqueduct_spring_water", 2.0F, 1.0F));
            }
        }

        if (block instanceof IceBlock)
            return Optional.of(new TempModifier("ice_block", -4.0F, 1.0F));

        if (block instanceof SeaIceBlock)
            return Optional.of(new TempModifier("sea_ice", -6.0F, 1.0F));

        if (block instanceof OvenBottomBlock && state.getValue(OvenBottomBlock.LIT))
            return Optional.of(new TempModifier("firmalife_oven", 6.0F, 1.0F));

        return Optional.empty();
    }
}
