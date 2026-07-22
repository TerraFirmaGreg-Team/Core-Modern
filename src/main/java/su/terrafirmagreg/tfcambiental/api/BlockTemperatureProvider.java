package su.terrafirmagreg.tfcambiental.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

import net.createmod.catnip.math.VecHelper;
import net.dries007.tfc.common.blockentities.BloomeryBlockEntity;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;
import net.dries007.tfc.common.blockentities.FirepitBlockEntity;
import net.dries007.tfc.common.blockentities.GrillBlockEntity;
import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.blocks.SeaIceBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.RegistryObject;

import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Mars;
import su.terrafirmagreg.core.common.data.tfgt.TFGMachines;
import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.api.BlockTemperatureProvider.TempModifierSpec;
import su.terrafirmagreg.tfcambiental.item.TFCAmbientalItems;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface BlockTemperatureProvider {
    List<BlockTemperatureProvider> PROVIDERS = List.of(
            BlockTemperatureProvider::handleHotStuff,
            BlockTemperatureProvider::handleColdStuff,
            BlockTemperatureProvider::handleSnow,
            BlockTemperatureProvider::handleWarmStuff,
            BlockTemperatureProvider::getBlockTempModifier);

    Optional<TempModifier> getModifier(Player player, BlockPos pos, BlockState state);

    static void evaluateAll(Player player, TempModifierStorage storage) {
        BlockPos onPos = player.getOnPos();
        BlockPos pos1 = new BlockPos(onPos.getX() - 9, onPos.getY() - 3, onPos.getZ() - 9);
        BlockPos pos2 = new BlockPos(onPos.getX() + 9, onPos.getY() + 5, onPos.getZ() + 9);
        Iterable<BlockPos> allPositions = BlockPos.betweenClosed(pos1, pos2);

        TempModifierStorage potentialStorage = new TempModifierStorage();

        for (BlockPos pos : allPositions) {
            if (!player.level().isLoaded(pos)) {
                continue;
            }

            BlockState state = player.level().getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            double distance = Math.sqrt(onPos.distSqr(pos));
            float distanceMultiplier = (float) distance / 9f;
            distanceMultiplier = Math.min(1f, Math.max(0f, distanceMultiplier));
            distanceMultiplier = 1f - distanceMultiplier;
            boolean isInside = EnvironmentalTemperatureProvider.getSkylight(player) < 14 && EnvironmentalTemperatureProvider.getBlockLight(player) > 3;
            if (isInside) {
                distanceMultiplier *= 1.3f;
            }

            for (BlockTemperatureProvider provider : PROVIDERS) {
                addScaled(potentialStorage, provider.getModifier(player, pos, state), distanceMultiplier);
            }

            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (blockEntity != null) {
                addScaled(storage, handleCharcoalForge(player, blockEntity), distanceMultiplier);
                addScaled(storage, handleFirePit(player, blockEntity), distanceMultiplier);
                addScaled(storage, handlePot(player, blockEntity), distanceMultiplier);
                addScaled(storage, handleGrill(player, blockEntity), distanceMultiplier);
                addScaled(storage, handleBloomery(player, blockEntity), distanceMultiplier);
                addScaled(storage, handleLitBlock(player, blockEntity), distanceMultiplier);
                addScaled(storage, handleIHeatBlock(player, blockEntity), distanceMultiplier);
                storage.add(handleMultiblockInside(player, blockEntity));
                storage.add(handleEncasedFan(player, blockEntity));
            }
        }

        List<TempModifier> mods = new ArrayList<>();
        for (TempModifier mod : potentialStorage) {
            mods.add(mod);
        }
        mods.sort(Comparator.comparingDouble((TempModifier m) -> Math.abs(m.getChange())).reversed());
        for (int i = 0; i < Math.min(5, mods.size()); i++) {
            storage.add(mods.get(i));
        }
    }

    private static void addScaled(TempModifierStorage storage, Optional<TempModifier> modifier, float multiplier) {
        modifier.ifPresent(mod -> {
            mod.setChange(mod.getChange() * multiplier);
            mod.setPotency(mod.getPotency() * multiplier);
            mod.setWetness(mod.getWetness() * multiplier);
            storage.add(mod);
        });
    }

    static Optional<TempModifier> handleHotStuff(Player player, BlockPos pos, BlockState state) {
        return state.is(TFCAmbiental.HOT_STUFF) ? Optional.of(new TempModifier(3f, 0.2f, -15f, true)) : TempModifier.none();
    }

    static Optional<TempModifier> handleColdStuff(Player player, BlockPos pos, BlockState state) {
        return state.is(TFCAmbiental.COLD_STUFF) ? Optional.of(new TempModifier(-0.5f, 0.2f, true)) : TempModifier.none();
    }

    static Optional<TempModifier> handleSnow(Player player, BlockPos pos, BlockState state) {
        return state.is(Blocks.SNOW) && EquipmentTemperatureProvider.getEquipmentByType(player, ArmorItem.Type.BOOTS).isEmpty()
                ? Optional.of(new TempModifier(-0.5f, 0.5f))
                : TempModifier.none();
    }

    static Optional<TempModifier> handleWarmStuff(Player player, BlockPos pos, BlockState state) {
        return state.is(TFCAmbiental.WARM_STUFF) ? Optional.of(new TempModifier(1f, 0f, -5f, true)) : TempModifier.none();
    }

    private static boolean hasProtection(Player player) {
        var item = CuriosApi.getCuriosHelper().findCurios(player, TFCAmbientalItems.LEATHER_APRON.get());
        if (item.isEmpty()) {
            return false;
        }
        float environmentTemperature = EnvironmentalTemperatureProvider.getEnvironmentTemperatureWithTimeOfDay(player);
        float average = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
        return environmentTemperature > average;
    }

    static Optional<TempModifier> handleCharcoalForge(Player player, BlockEntity entity) {
        if (entity instanceof CharcoalForgeBlockEntity forge) {
            float temp = forge.getTemperature();
            float change = temp / 140f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined(change, 0);
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
            return TempModifier.defined(Math.min(6f, change), 0);
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
            return TempModifier.defined(Math.min(6f, change), 0);
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
            return TempModifier.defined(Math.min(6f, change), 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleBloomery(Player player, BlockEntity entity) {
        if (entity instanceof BloomeryBlockEntity bloomery) {
            float change = bloomery.getRemainingTicks() > 0 ? 4f : 0f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined(change, 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleLitBlock(Player player, BlockEntity entity) {
        if (entity.getBlockState().hasProperty(BlockStateProperties.LIT)
                && entity.getBlockState().getValue(BlockStateProperties.LIT)) {
            float change = 3f;
            if (hasProtection(player)) {
                change = change * 0.3f;
            }
            return TempModifier.defined(change, 0);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleIHeatBlock(Player player, BlockEntity entity) {
        return entity.getCapability(HeatCapability.BLOCK_CAPABILITY)
                .map(cap -> new TempModifier(cap.getTemperature() / 140f, 0));
    }

    record TempModifierSpec(float change, float potency) {
        Optional<TempModifier> create() {
            return Optional.of(new TempModifier(change, potency, false));
        }
    }

    static final Map<Block, TempModifierSpec> SIMPLE_BLOCKS = Stream.concat(
            Stream.of(
                    Map.entry(Blocks.PACKED_ICE, new TempModifierSpec(-1.0F, 1.0F)),
                    Map.entry(Blocks.BLUE_ICE, new TempModifierSpec(-4.0F, 1.0F)),
                    Map.entry(TFCBlocks.SEA_ICE.get(), new TempModifierSpec(-2.0F, 1.0F)),
                    Map.entry(TFGBlocks.DRY_ICE.get(), new TempModifierSpec(-2.0F, 1.0F)),
                    Map.entry(TFGBlocks_Mars.MARS_ICE.get(), new TempModifierSpec(-3.0F, 1.0F))),
            TFCBlocks.MAGMA_BLOCKS.values().stream()
                    .map(RegistryObject::get)
                    .map(block -> Map.entry(block, new TempModifierSpec(3.0F, 1.0F))))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    static final Map<Block, TempModifierSpec> ACTIVE_BLOCKS = Map.ofEntries(
            Map.entry(GTBlocks.FIREBOX_BRONZE.get(), new TempModifierSpec(3.0F, 1.0F)),
            Map.entry(GTBlocks.FIREBOX_STEEL.get(), new TempModifierSpec(4.0F, 2.0F)),
            Map.entry(GTBlocks.FIREBOX_TITANIUM.get(), new TempModifierSpec(5.0F, 3.0F)),
            Map.entry(GTBlocks.FIREBOX_TUNGSTENSTEEL.get(), new TempModifierSpec(6.0F, 3.0F)),

            Map.entry(GTBlocks.COIL_CUPRONICKEL.get(), new TempModifierSpec(5.0F, 1.0F)),
            Map.entry(GTBlocks.COIL_KANTHAL.get(), new TempModifierSpec(10.0F, 1.0F)),
            Map.entry(GTBlocks.COIL_NICHROME.get(), new TempModifierSpec(20.0F, 2.0F)),
            Map.entry(GTBlocks.COIL_RTMALLOY.get(), new TempModifierSpec(25.0F, 2.0F)),
            Map.entry(GTBlocks.COIL_HSSG.get(), new TempModifierSpec(30.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_NAQUADAH.get(), new TempModifierSpec(40.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_TRINIUM.get(), new TempModifierSpec(45.0F, 3.0F)),
            Map.entry(GTBlocks.COIL_TRITANIUM.get(), new TempModifierSpec(50.0F, 3.0F)));

    static final Map<Block, TempModifierSpec> CAPABILITY_BLOCKS = Stream.of(
            Stream.of(
                    Map.entry(
                            GTMachines.STEAM_SOLID_BOILER.right().getBlock(),
                            new TempModifierSpec(5.0F, 1.0F)),
                    Map.entry(
                            GTMachines.STEAM_LIQUID_BOILER.right().getBlock(),
                            new TempModifierSpec(5.0F, 1.0F)),
                    Map.entry(
                            GTMachines.STEAM_FURNACE.right().getBlock(),
                            new TempModifierSpec(8.0F, 1.0F)),
                    Map.entry(
                            GTMachines.STEAM_ALLOY_SMELTER.right().getBlock(),
                            new TempModifierSpec(6.0F, 1.0F))),

            Arrays.stream(GTMachines.ELECTRIC_FURNACE)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec(5.0F, 2.0F))),
            Arrays.stream(GTMachines.ARC_FURNACE)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec(6.0F, 2.0F))),
            Arrays.stream(GTMachines.ALLOY_SMELTER)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec(5.0F, 2.0F))),
            Arrays.stream(GTMachines.FLUID_HEATER)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec(6.0F, 2.0F))),
            Arrays.stream(TFGMachines.FOOD_OVEN)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec(3.0F, 2.0F))),
            Arrays.stream(TFGMachines.FOOD_REFRIGERATOR)
                    .filter(Objects::nonNull)
                    .map(m -> Map.entry(
                            m.getBlock(),
                            new TempModifierSpec(2.0F, 1.0F))))
            .flatMap(Function.identity())
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    static Optional<TempModifier> getBlockTempModifier(Player player, BlockPos blockPos, BlockState state) {
        Block block = state.getBlock();

        TempModifierSpec spec = SIMPLE_BLOCKS.get(block);
        if (spec != null) {
            return spec.create();
        }

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

        if (block instanceof AqueductBlock aqueduct) {
            var fluid = state.getValue(aqueduct.getFluidProperty()).getFluid();
            if (fluid == Fluids.LAVA) {
                return Optional.of(new TempModifier(5.0F, 1.0F));
            } else if (fluid == TFCFluids.SPRING_WATER.getFlowing()) {
                return Optional.of(new TempModifier(2.0F, 1.0F));
            }
        }

        if (block instanceof IceBlock) {
            return Optional.of(new TempModifier(-4.0F, 1.0F, true));
        }

        if (block instanceof SeaIceBlock) {
            return Optional.of(new TempModifier(-3.0F, 1.0F));
        }

        if (block instanceof OvenBottomBlock && state.getValue(OvenBottomBlock.LIT)) {
            return Optional.of(new TempModifier(6.0F, 1.0F));
        }

        return Optional.empty();
    }

    static Optional<TempModifier> handleMultiblockInside(Player player, BlockEntity blockEntity) {
        if (blockEntity instanceof IMachineBlockEntity machineBE &&
                machineBE.getMetaMachine() instanceof WorkableElectricMultiblockMachine machine &&
                machine.isFormed()
                && isPosInCacheSafe(machine, player.blockPosition())
                && machine.getRecipeLogic().isWorking()) {

            float temp;

            if (machine.getDefinition() == GCYMMachines.MEGA_VACUUM_FREEZER) {
                temp = -273.0f;
            } else if (machine.getDefinition() == GTMultiMachines.VACUUM_FREEZER) {
                temp = -150.0f;
            } else if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
                temp = coilMachine.getCoilType().getCoilTemperature();
            } else {
                return TempModifier.none();
            }

            return Optional.of(new TempModifier(temp, 100f));
        }
        return TempModifier.none();
    }

    private static boolean isPosInCacheSafe(WorkableElectricMultiblockMachine machine, BlockPos pos) {
        var lock = machine.getPatternLock();
        if (!lock.tryLock()) {
            return false;
        }
        try {
            return machine.getMultiblockState().isPosInCache(pos);
        } catch (NullPointerException ignored) {
            return false;
        } finally {
            lock.unlock();
        }
    }

    static Optional<TempModifier> handleEncasedFan(Player player, BlockEntity blockEntity) {
        if (blockEntity instanceof EncasedFanBlockEntity fanBE) {
            AirCurrent airCurrent = fanBE.getAirCurrent();
            if (airCurrent != null && airCurrent.maxDistance > 0 && airCurrent.bounds.intersects(player.getBoundingBox())) {
                double distance = VecHelper.alignedDistanceToFace(player.position(), fanBE.getBlockPos(), airCurrent.direction);
                FanProcessingType type = airCurrent.getTypeAt((float) distance);
                if (type instanceof AllFanProcessingTypes.BlastingType) {
                    return Optional.of(new TempModifier(4.0F, 1.0F, true));
                } else if (type instanceof AllFanProcessingTypes.SmokingType) {
                    return Optional.of(new TempModifier(2.0F, 1.0F, true));
                } else if (type instanceof AllFanProcessingTypes.SplashingType) {
                    return Optional.of(new TempModifier(-2.0F, 1.0F, 2.0F, true));
                } else {
                    return Optional.of(new TempModifier(-2.0F, 1.0F, true));
                }
            }
        }
        return TempModifier.none();
    }
}
