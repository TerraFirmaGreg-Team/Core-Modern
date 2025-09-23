package su.terrafirmagreg.core.common.data.entities.wraptor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.dries007.tfc.common.entities.ai.SetLookTarget;
import net.dries007.tfc.common.entities.ai.livestock.BreedBehavior;
import net.dries007.tfc.common.entities.ai.livestock.LivestockAi;
import net.dries007.tfc.common.entities.ai.prey.AvoidPredatorBehavior;
import net.minecraft.Util;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import su.terrafirmagreg.core.common.data.entities.ai.LayLargeEggBehavior;
import su.terrafirmagreg.core.common.data.entities.ai.TFGBrain;

public class TFCWraptorAi extends LivestockAi {

    public static final ImmutableList<SensorType<? extends Sensor<? super TFCWraptor>>> SENSOR_TYPES = Util.make(() -> {
        List<SensorType<? extends Sensor<? super TFCWraptor>>> list = Lists.newArrayList(LivestockAi.SENSOR_TYPES);
        list.add(TFGBrain.LARGE_NEST_BOX_SENSOR.get());
        return ImmutableList.copyOf(list);
    });

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = Util.make(() -> {
        List<MemoryModuleType<?>> list = Lists.newArrayList(LivestockAi.MEMORY_TYPES);
        list.add(TFGBrain.LARGE_NEST_MEMORY.get());
        return ImmutableList.copyOf(list);
    });

    public static Brain<?> makeWraptorBrain(Brain<? extends TFCWraptor> brain) {
        initCoreActivity(brain);
        initWraptorIdleActivity(brain);
        initRetreatActivity(brain);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE)); // core activities run all the time
        brain.setDefaultActivity(Activity.IDLE); // the default activity is a useful way to have a fallback activity
        brain.useDefaultActivity();

        return brain;
    }

    public static void initWraptorIdleActivity(Brain<? extends TFCWraptor> brain) {
        brain.addActivity(Activity.IDLE, 0, ImmutableList.of(
                SetLookTarget.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60)), // looks at player, but its only
                // try it every so often -- "Run
                // Sometimes"
                AvoidPredatorBehavior.create(true),
                new LayLargeEggBehavior(),
                new BreedBehavior<>(1.0F), // custom TFC breed behavior
                new AnimalPanic(2.0F), // if memory of being hit, runs away
                new FollowTemptation(e -> e.isBaby() ? 1.5F : 1.25F), // sets the walk and look targets to whomever it
                // has a memory of being tempted by
                BabyFollowAdult.create(UniformInt.of(5, 16), 1.25F), // babies follow any random adult around
                createIdleMovementBehaviors()));
    }
}
