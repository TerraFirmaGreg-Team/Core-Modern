package su.terrafirmagreg.core.common.data.entities.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.LargeNestBoxBlock;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TFGBrain {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, TFGCore.MOD_ID);
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, TFGCore.MOD_ID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, TFGCore.MOD_ID);

    public static final RegistryObject<MemoryModuleType<BlockPos>> LARGE_NEST_MEMORY = MEMORY_TYPES.register("nest", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<SensorType<NearestLargeNestSensor>> LARGE_NEST_BOX_SENSOR = SENSOR_TYPES.register("nearest_nest_box", () -> new SensorType<>(NearestLargeNestSensor::new));

    public static final RegistryObject<PoiType> LARGE_NEST_POI = POI_TYPES.register("large_nest_poi", () -> new PoiType(ImmutableSet.copyOf(TFGBlocks.LARGE_NEST_BOX.get().getStateDefinition().getPossibleStates()), 1, 2));
    public static final RegistryObject<PoiType> WARPED_LARGE_NEST_POI = POI_TYPES.register("warped_large_nest_poi", () -> new PoiType(ImmutableSet.copyOf(TFGBlocks.LARGE_NEST_BOX_WARPED.get().getStateDefinition().getPossibleStates()), 1, 2));


}
