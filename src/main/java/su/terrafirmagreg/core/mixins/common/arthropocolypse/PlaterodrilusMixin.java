package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.data.TFGTags;

@Pseudo
@Mixin(targets = "dev.dh.arthropocolypse.entity.Platerodrilus")
public class PlaterodrilusMixin {

    // registerGoals
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "m_8099_", at = @At("TAIL"), remap = false)
    protected void tfg$registerGoals(CallbackInfo ci) {
        Animal mob = (Animal) (Object) this;

        mob.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
        mob.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, ServerPlayer.class, true, true));
        mob.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(mob, Animal.class, true, (entity) -> entity.getType().is(TFGTags.Entities.VenusPrey)));
    }
}
