package su.terrafirmagreg.core.mixins.common.tfc.entities;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.dries007.tfc.common.entities.ai.FastGateBehavior;
import net.dries007.tfc.common.entities.ai.livestock.LivestockAi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;

@Mixin(value = LivestockAi.class, remap = false)
public class LivestockAiMixin {

    /**
     * Make it much more likely for livestock to stand still.
     * Without this mixin livestock has a 1 in 3 chance to go on a random walk, 1 in 3 to walk to what it's looking at if anything,
     * and 1 in 3 to do nothing for 30 to 60 ticks.
     * This bumps the DoNothing up to 3 in 5 chance.
     * This doesn't affect high priority behaviors like following, breeding, escaping, etc.
     */
    @WrapOperation(method = "createIdleMovementBehaviors", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/common/entities/ai/FastGateBehavior;runOne(Ljava/util/List;)Lnet/dries007/tfc/common/entities/ai/FastGateBehavior;"))
    private static FastGateBehavior<LivingEntity> tfg$addIdleDoNothings(List<BehaviorControl<? super LivingEntity>> behaviors, Operation<FastGateBehavior<LivingEntity>> original) {
        List<BehaviorControl<? super LivingEntity>> expanded = new ArrayList<>(behaviors);
        expanded.add(new DoNothing(30, 60));
        expanded.add(new DoNothing(30, 60));
        return original.call(ImmutableList.copyOf(expanded));
    }
}
