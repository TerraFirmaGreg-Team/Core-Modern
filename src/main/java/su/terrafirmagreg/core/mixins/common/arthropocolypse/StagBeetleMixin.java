package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import dev.dh.arthropocolypse.entity.Stag_Beetle;
import dev.dh.arthropocolypse.entity.ai.ClimbableEntity;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = Stag_Beetle.class, remap = false)
public class StagBeetleMixin extends ClimbableEntity {

    protected StagBeetleMixin(EntityType type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    protected void tfg$registerGoals(CallbackInfo ci) {
        Animal mob = (Animal) (Object) this;

        mob.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
        mob.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, ServerPlayer.class, true, true));
        mob.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(mob, Animal.class, true, (entity) -> entity.getType().is(TFGTags.Entities.VenusPrey)));
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0F)
                .add(Attributes.ARMOR, 10.0F)
                .add(Attributes.FOLLOW_RANGE, 24.0F)
                .add(Attributes.ATTACK_SPEED, 1.0F)
                .add(Attributes.ATTACK_DAMAGE, 20)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.2));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
