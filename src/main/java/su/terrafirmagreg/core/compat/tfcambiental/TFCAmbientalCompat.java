package su.terrafirmagreg.core.compat.tfcambiental;

import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.lumintorious.tfcambiental.api.AmbientalRegistry;
import com.lumintorious.tfcambiental.modifier.TempModifier;
import net.dries007.tfc.common.blocks.SeaIceBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.material.Fluids;

import java.util.Arrays;
import java.util.Optional;

public final class TFCAmbientalCompat {

    public static void register() {
        /* Блоки магмы */
        final Rock[] magmaRocks = new Rock[] { Rock.ANDESITE, Rock.DIORITE, Rock.DACITE, Rock.RHYOLITE, Rock.GABBRO, Rock.GRANITE };

        for (final Rock rock : magmaRocks) {
            AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("magma_" + rock.getSerializedName(), 3.0F, 1.0F)).filter((mod) ->
                    state.getBlock() == TFCBlocks.MAGMA_BLOCKS.get(rock).get())
            );
        }


        /* Паровые машины */
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("hp_steam_solid_boiler", 5.0F, 2.0F)).filter((mod) -> {
            var isTargetBlock = state.getBlock() == GTMachines.STEAM_SOLID_BOILER.right().getBlock();
            var cap = GTCapabilityHelper.getWorkable(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("hp_steam_liquid_boiler", 5.0F, 2.0F)).filter((mod) -> {
            var isTargetBlock = state.getBlock() == GTMachines.STEAM_LIQUID_BOILER.right().getBlock();
            var cap = GTCapabilityHelper.getWorkable(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("hp_steam_furnace", 8.0F, 2.0F)).filter((mod) -> {
            var isTargetBlock = state.getBlock() == GTMachines.STEAM_FURNACE.right().getBlock();
            var cap = GTCapabilityHelper.getWorkable(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("hp_steam_alloy_smelter", 6.0F, 2.0F)).filter((mod) -> {
            var isTargetBlock = state.getBlock() == GTMachines.STEAM_ALLOY_SMELTER.right().getBlock();
            var cap = GTCapabilityHelper.getWorkable(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        /* Бойлеры */
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("bronze_firebox", 6.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.FIREBOX_BRONZE.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("steel_firebox", 8.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.FIREBOX_STEEL.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("titanium_firebox", 10.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.FIREBOX_TITANIUM.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("tungstensteel_firebox", 12.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.FIREBOX_TUNGSTENSTEEL.get() && state.getValue(ActiveBlock.ACTIVE)));

        /* Электро-печки */
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("electric_furnace", 10.0F, 3.0F)).filter((mod) -> {
            var isTargetBlock = Arrays.stream(GTMachines.ELECTRIC_FURNACE).anyMatch(element -> {
                if (element != null) return element.getBlock() == state.getBlock();
                return false;
            });
            var cap = GTCapabilityHelper.getRecipeLogic(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("arc_furnace", 12.0F, 3.0F)).filter((mod) -> {
            var isTargetBlock = Arrays.stream(GTMachines.ARC_FURNACE).anyMatch(element -> {
                if (element != null) return element.getBlock() == state.getBlock();
                return false;
            });
            var cap = GTCapabilityHelper.getRecipeLogic(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("alloy_smelter", 9.0F, 3.0F)).filter((mod) -> {
            var isTargetBlock = Arrays.stream(GTMachines.ALLOY_SMELTER).anyMatch(element -> {
                if (element != null) return element.getBlock() == state.getBlock();
                return false;
            });
            var cap = GTCapabilityHelper.getRecipeLogic(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("fluid_heater", 11.0F, 3.0F)).filter((mod) -> {
            var isTargetBlock = Arrays.stream(GTMachines.FLUID_HEATER).anyMatch(element -> {
                if (element != null) return element.getBlock() == state.getBlock();
                return false;
            });
            var cap = GTCapabilityHelper.getRecipeLogic(player.level(), blockPos, null);
            return cap != null && cap.isActive() && cap.isWorkingEnabled() && isTargetBlock;
        }));

        /* Койлы доменной печи */
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("cupronickel_coil", 18.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_CUPRONICKEL.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("kanthal_coil", 28.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_KANTHAL.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("nichrome_coil", 38.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_NICHROME.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("tungstensteel_coil", 48.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_RTMALLOY.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("hssg_coil", 58.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_HSSG.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("naquadah_coil", 78.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_NAQUADAH.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("trinium_coil", 88.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_TRINIUM.get() && state.getValue(ActiveBlock.ACTIVE)));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("tritanium_coil", 98.0F, 3.0F)).filter((mod) -> state.getBlock() == GTBlocks.COIL_TRITANIUM.get() && state.getValue(ActiveBlock.ACTIVE)));

        /* Другое */
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("aqueduct_lava", 4.0F, 1.0F)).filter((mod) -> state.getBlock() instanceof AqueductBlock && state.getValue(AqueductBlock.FLUID).getFluid() == Fluids.LAVA));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("aqueduct_water", -3.0F, 1.0F)).filter((mod) -> state.getBlock() instanceof AqueductBlock && state.getValue(AqueductBlock.FLUID).getFluid() == Fluids.WATER));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("packed_block", -9.0F, 1.0F)).filter((mod) -> state.getBlock() == Blocks.PACKED_ICE));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("blue_ice", -8.0F, 1.0F)).filter((mod) -> state.getBlock() == Blocks.BLUE_ICE));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("ice_block", -6.0F, 1.0F)).filter((mod) -> state.getBlock() instanceof IceBlock));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("sea_ice", -6.0F, 1.0F)).filter((mod) -> state.getBlock() instanceof SeaIceBlock));
        AmbientalRegistry.BLOCKS.register((player, blockPos, state) -> Optional.of(new TempModifier("firmalife_oven", 6.0F, 1.0F)).filter((mod) -> state.getBlock() instanceof OvenBottomBlock && state.getValue(OvenBottomBlock.LIT)));
    }

}
