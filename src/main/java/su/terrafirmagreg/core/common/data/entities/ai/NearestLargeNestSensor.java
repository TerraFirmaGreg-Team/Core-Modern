package su.terrafirmagreg.core.common.data.entities.ai;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.pathfinder.Path;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import su.terrafirmagreg.core.common.data.entities.TFGWoolEggProducingAnimal;

public class NearestLargeNestSensor extends Sensor<TFGWoolEggProducingAnimal> {
    private static final int CACHE_TIMEOUT = 40;
    private static final int BATCH_SIZE = 5;

    private int triedCount;
    private long lastUpdate;
    private final Long2LongMap batchCache = new Long2LongOpenHashMap(); // position to time

    @Override
    protected void doTick(ServerLevel level, TFGWoolEggProducingAnimal animal) {
        // we only need to do this if we are going to make an egg and not sitting already
        if (animal.isReadyForAnimalProduct() && !animal.isPassenger()) {
            triedCount = 0;
            lastUpdate = level.getGameTime() + level.getRandom().nextInt(20);
            PoiManager manager = level.getPoiManager();
            Predicate<BlockPos> predicate = pos -> {
                final long packed = pos.asLong();
                if (batchCache.containsKey(packed)) {
                    return false;
                } else if (++triedCount >= BATCH_SIZE) {
                    return false;
                } else {
                    batchCache.putIfAbsent(packed, lastUpdate + CACHE_TIMEOUT);
                    return true;
                }
            };
            // Deals with pathing to the nest
            Stream<BlockPos> found = manager.findAll(
                    p -> p.value() == TFGBrain.LARGE_NEST_POI.get()
                            || p.value() == TFGBrain.WARPED_LARGE_NEST_POI.get(),
                    predicate, animal.blockPosition(), 48, PoiManager.Occupancy.ANY);
            Path path = animal.getNavigation().createPath(found, TFGBrain.LARGE_NEST_POI.get().validRange());
            if (path != null && path.canReach()) {
                BlockPos target = path.getTarget();
                manager.getType(target)
                        .ifPresent(poi -> animal.getBrain().setMemory(TFGBrain.LARGE_NEST_MEMORY.get(), target));
            } else if (triedCount < BATCH_SIZE) {
                batchCache.long2LongEntrySet().removeIf(set -> set.getLongValue() < lastUpdate);
            }
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(TFGBrain.LARGE_NEST_MEMORY.get());
    }

}
