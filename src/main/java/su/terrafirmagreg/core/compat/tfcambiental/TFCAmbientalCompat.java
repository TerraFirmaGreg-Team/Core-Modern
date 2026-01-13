package su.terrafirmagreg.core.compat.tfcambiental;

import java.util.*;

import com.eerussianguy.beneath.common.blocks.LavaAqueductBlock;
import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.lumintorious.tfcambiental.api.AmbientalRegistry;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import com.simibubi.create.AllItems;

import net.dries007.tfc.common.blocks.SeaIceBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.RegistryObject;

import earth.terrarium.adastra.common.registry.ModItems;

import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;

/**
 * Compatibility for TFC Ambiental
 *
 * <p> Registers a block and equipment temperature provider with TFC Ambiental.
 * Uses static maps/sets for performance as this runs >3000x per player, every 20 ticks.
 */
public final class TFCAmbientalCompat {

    // ==================== BLOCKS ====================

    /**
     * Immutable spec for creating Optional {@link TempModifier} s
     */
    private record TempModifierSpec(String name, float change, float potency) {
        Optional<TempModifier> create() {
            return Optional.of(new TempModifier(name, change, potency));
        }
    }

    /* Blocks that affect temperature at all times */
    private static final Map<Block, TempModifierSpec> SIMPLE_BLOCKS;
    static {
        Map<Block, TempModifierSpec> simpleBlocks = new HashMap<>();

        // Ice blocks
        simpleBlocks.put(Blocks.PACKED_ICE, new TempModifierSpec("packed_ice", -6.0F, 1.0F));
        simpleBlocks.put(Blocks.BLUE_ICE, new TempModifierSpec("blue_ice", -8.0F, 1.0F));

        // Magma blocks
        TempModifierSpec magmaRockSpec = new TempModifierSpec("magma_rock", 5.0F, 1.0F);
        TFCBlocks.MAGMA_BLOCKS.values().stream()
                .map(RegistryObject::get)
                .forEach(block -> simpleBlocks.put(block, magmaRockSpec));

        SIMPLE_BLOCKS = Map.copyOf(simpleBlocks); // Makes it immutable
    }

    /* Blocks that affect temperature when GTBlockStateProperties.ACTIVE */
    private static final Map<Block, TempModifierSpec> ACTIVE_BLOCKS = Map.ofEntries(
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
    private static final Map<Block, TempModifierSpec> CAPABILITY_BLOCKS;
    static {
        Map<Block, TempModifierSpec> capabilityBlocks = new HashMap<>();

        // Steam machines
        capabilityBlocks.put(GTMachines.STEAM_SOLID_BOILER.right().getBlock(), new TempModifierSpec("steam_solid_boiler", 5.0F, 2.0F));
        capabilityBlocks.put(GTMachines.STEAM_LIQUID_BOILER.right().getBlock(), new TempModifierSpec("steam_liquid_boiler", 5.0F, 2.0F));

        capabilityBlocks.put(GTMachines.STEAM_FURNACE.right().getBlock(), new TempModifierSpec("steam_furnace", 8.0F, 2.0F));
        capabilityBlocks.put(GTMachines.STEAM_ALLOY_SMELTER.right().getBlock(), new TempModifierSpec("steam_alloy_smelter", 6.0F, 2.0F));

        // Electric furnaces
        TempModifierSpec electricFurnaceSpec = new TempModifierSpec("electric_furnace", 10.0F, 3.0F);
        Arrays.stream(GTMachines.ELECTRIC_FURNACE)
                .filter(Objects::nonNull)
                .forEach(m -> capabilityBlocks.put(m.getBlock(), electricFurnaceSpec));

        // Arc furnaces
        TempModifierSpec arcSpec = new TempModifierSpec("arc_furnace", 12.0F, 3.0F);
        Arrays.stream(GTMachines.ARC_FURNACE)
                .filter(Objects::nonNull)
                .forEach(m -> capabilityBlocks.put(m.getBlock(), arcSpec));

        // Alloy smelters
        TempModifierSpec alloySpec = new TempModifierSpec("alloy_smelter", 9.0F, 3.0F);
        Arrays.stream(GTMachines.ALLOY_SMELTER)
                .filter(Objects::nonNull)
                .forEach(m -> capabilityBlocks.put(m.getBlock(), alloySpec));

        // Fluid Heaters
        TempModifierSpec fluidHeaterSpec = new TempModifierSpec("fluid_heater", 11.0F, 3.0F);
        Arrays.stream(GTMachines.FLUID_HEATER)
                .filter(Objects::nonNull)
                .forEach(m -> capabilityBlocks.put(m.getBlock(), fluidHeaterSpec));

        // Food ovens
        TempModifierSpec foodOvenSpec = new TempModifierSpec("food_oven", 7.0F, 2.0F);
        Arrays.stream(TFGMachines.FOOD_OVEN)
                .filter(Objects::nonNull)
                .forEach(m -> capabilityBlocks.put(m.getBlock(), foodOvenSpec));

        // Refrigerators
        TempModifierSpec refrigeratorSpec = new TempModifierSpec("refrigerator", 5.0F, 1.0F);
        Arrays.stream(TFGMachines.FOOD_REFRIGERATOR)
                .filter(Objects::nonNull)
                .forEach(m -> capabilityBlocks.put(m.getBlock(), refrigeratorSpec));

        CAPABILITY_BLOCKS = Map.copyOf(capabilityBlocks);
    }

    /**
     * Determines if the given block should modify player temperature
     * @return Optional TempModifier
     */
    private static Optional<TempModifier> getBlockTempModifier(Player player, BlockPos blockPos, BlockState state) {
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
        if (block instanceof LavaAqueductBlock && state.getValue(LavaAqueductBlock.FLUID).getFluid() == Fluids.LAVA)
            return Optional.of(new TempModifier("aqueduct_lava", 5.0F, 1.0F));

        if (block instanceof AqueductBlock && state.getValue(AqueductBlock.FLUID).getFluid() == TFCFluids.SPRING_WATER.getFlowing())
            return Optional.of(new TempModifier("aqueduct_spring_water", 2.0F, 1.0F));

        if (block instanceof IceBlock)
            return Optional.of(new TempModifier("ice_block", -4.0F, 1.0F));

        if (block instanceof SeaIceBlock)
            return Optional.of(new TempModifier("sea_ice", -6.0F, 1.0F));

        if (block instanceof OvenBottomBlock && state.getValue(OvenBottomBlock.LIT))
            return Optional.of(new TempModifier("firmalife_oven", 6.0F, 1.0F));

        return Optional.empty();
    }

    // ==================== EQUIPMENT ====================

    public static final float HEATPROOF = -9F;
    public static final float FULLY_INSULATED = -10F;

    private static final Set<Item> COPPER_DIVING_SUIT = Set.of(
            AllItems.COPPER_DIVING_HELMET.get(),
            AllItems.COPPER_DIVING_BOOTS.get(),
            AllItems.COPPER_BACKTANK.get());

    private static final Set<Item> BLUE_STEEL_DIVING_SUIT = Set.of(
            AllItems.NETHERITE_DIVING_HELMET.get(),
            AllItems.NETHERITE_DIVING_BOOTS.get(),
            AllItems.NETHERITE_BACKTANK.get(),
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS);

    private static final Set<Item> ADVANCED_ARMOR = Set.of(
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

    /**
     * Determines if the given block should modify player temperature
     * @return Optional TempModifier
     */
    private static Optional<TempModifier> getEquipmentTempModifier(Player player, ItemStack stack) {
        Item item = stack.getItem();

        if (COPPER_DIVING_SUIT.contains(item)) {
            return Optional.of(new TempModifier("copper_diving_suit", -1F, 0.1F));
        }
        if (BLUE_STEEL_DIVING_SUIT.contains(item)) {
            return Optional.of(new TempModifier("blue_steel_diving_suit", -3F, HEATPROOF));
        }
        if (ADVANCED_ARMOR.contains(item)) {
            return Optional.of(new TempModifier("advanced_armor", 0F, FULLY_INSULATED));
        }

        return Optional.empty();
    }

    // ==================== REGISTER ====================

    /**
     * Registers the providers with AmbientalRegistry
     */
    public static void register() {
        AmbientalRegistry.BLOCKS.register(TFCAmbientalCompat::getBlockTempModifier);
        AmbientalRegistry.EQUIPMENT.register(TFCAmbientalCompat::getEquipmentTempModifier);
    }
}
