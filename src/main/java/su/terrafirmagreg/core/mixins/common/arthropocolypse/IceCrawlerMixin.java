package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

@Pseudo
@Mixin(targets = "dev.dh.arthropocolypse.entity.Ice_Crawler")
public class IceCrawlerMixin {

    // registerGoals
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "m_8099_", at = @At("TAIL"))
    protected void tfg$registerGoals(CallbackInfo ci) {
        PathfinderMob mob = (PathfinderMob) (Object) this;

        mob.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
    }
}
