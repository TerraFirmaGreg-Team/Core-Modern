package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import dev.dh.arthropocolypse.entity.Soldier_Ant;
import dev.dh.arthropocolypse.entity.ai.ClimbableEntity;

@Mixin(value = Soldier_Ant.class, remap = false)
public class SoldierAntMixin extends ClimbableEntity {

    protected SoldierAntMixin(EntityType type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    protected void tfg$registerGoals(CallbackInfo ci) {
        Animal mob = (Animal) (Object) this;

        mob.goalSelector.addGoal(8, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0F)
                .add(Attributes.ARMOR, 10.0F)
                .add(Attributes.FOLLOW_RANGE, 24.0F)
                .add(Attributes.ATTACK_SPEED, 0.5F)
                .add(Attributes.ATTACK_DAMAGE, 14)
                .add(Attributes.ATTACK_KNOCKBACK, 0.4)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.3));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
