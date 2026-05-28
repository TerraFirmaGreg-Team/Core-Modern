package su.terrafirmagreg.core.mixins.common.tfc.entities;

import static net.dries007.tfc.common.entities.ai.prey.RammingPreyAi.*;

import org.spongepowered.asm.mixin.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.ai.prey.PrepareRamNearestTargetTFC;
import net.dries007.tfc.common.entities.ai.prey.RamTargetTFC;
import net.dries007.tfc.common.entities.ai.prey.RammingPreyAi;
import net.dries007.tfc.common.entities.prey.RammingPrey;
import net.dries007.tfc.util.Helpers;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.schedule.Activity;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = RammingPreyAi.class, remap = false)
public class RammingPreyAiMixin {

    // Mixin to stop ramming prey (bison, wildebeest, boars, moose) from ramming other ramming prey, resulting in
    // herds killing themselves

    @Shadow
    @Final
    private static UniformInt TIME_BETWEEN_RAMS_MALE;
    @Shadow
    @Final
    private static UniformInt TIME_BETWEEN_RAMS_FEMALE;
    @Shadow
    @Final
    private static float SPEED_MULTIPLIER_WHEN_PREPARING_TO_RAM;

    @Unique
    private static final TargetingConditions TFG_RAM_TARGET_CONDITIONS = TargetingConditions.forCombat().selector(
            (target) -> target.level().getWorldBorder().isWithinBounds(target.getBoundingBox())
                    && !(target instanceof RammingPrey)
                    && !Helpers.isEntity(target, TFGTags.Entities.NotRammedByRammers));

    /**
     * @author Pyritie
     * @reason Since RAM_TARGET_CONDITIONS is a private static field, it can't be modified.
     * Instead, overwrite initRamActivity with our own version
     */
    @Overwrite
    private static void initRamActivity(Brain<? extends RammingPrey> brain) {
        brain.addActivityWithConditions(Activity.RAM, ImmutableList.of(
                Pair.of(0, new RamTargetTFC(
                        (rammingPrey) -> rammingPrey.isMale() ? TIME_BETWEEN_RAMS_MALE : TIME_BETWEEN_RAMS_FEMALE,
                        TFG_RAM_TARGET_CONDITIONS, 3.0F,
                        (rammingPrey) -> rammingPrey.isBaby() ? BABY_RAM_KNOCKBACK_FORCE : ADULT_RAM_KNOCKBACK_FORCE,
                        (rammingPrey) -> TFCSounds.RAMMING_IMPACT.get())),
                Pair.of(1, new PrepareRamNearestTargetTFC<>(
                        (rammingPrey) -> rammingPrey.isMale() ? TIME_BETWEEN_RAMS_MALE.getMinValue() : TIME_BETWEEN_RAMS_FEMALE.getMinValue(),
                        RAM_MIN_DISTANCE, RAM_MAX_DISTANCE, SPEED_MULTIPLIER_WHEN_PREPARING_TO_RAM, TFG_RAM_TARGET_CONDITIONS, RAM_PREPARE_TIME,
                        (rammingPrey) -> rammingPrey.getAttackSound().get()))),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }
}
