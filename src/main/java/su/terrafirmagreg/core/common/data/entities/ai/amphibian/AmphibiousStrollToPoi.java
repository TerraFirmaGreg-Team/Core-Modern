/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.data.entities.ai.amphibian;

import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableLong;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

// Adapted from StrollToPoi to allow for variable speed modifiers between land and water
public class AmphibiousStrollToPoi {
    public AmphibiousStrollToPoi() {
    }

    public static BehaviorControl<PathfinderMob> create(MemoryModuleType<GlobalPos> poiPosMemory, Function<LivingEntity, Float> speedModifier, int closeEnoughDist, int maxDistFromPoi) {
        MutableLong mutablelong = new MutableLong(0L);
        return BehaviorBuilder.create(
                (m) -> m.group(m.registered(MemoryModuleType.WALK_TARGET),
                        m.present(poiPosMemory)).apply(
                                m, (walkTarget, pos) -> (level, mob, p_258853_) -> {

                                    GlobalPos globalpos = (GlobalPos) m.get(pos);
                                    if (level.dimension() == globalpos.dimension() && globalpos.pos().closerToCenterThan(mob.position(), (double) maxDistFromPoi)) {
                                        if (p_258853_ <= mutablelong.getValue()) {
                                            return true;
                                        } else {
                                            walkTarget.set(new WalkTarget(globalpos.pos(), speedModifier.apply(mob), closeEnoughDist));
                                            mutablelong.setValue(p_258853_ + 80L);
                                            return true;
                                        }
                                    } else {
                                        return false;
                                    }
                                }));
    }
}
