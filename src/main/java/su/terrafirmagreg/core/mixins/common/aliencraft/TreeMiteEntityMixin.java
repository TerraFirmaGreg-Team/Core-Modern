package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import en.predator97427fr.aliencraft.entity.TreeMiteEntity;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = TreeMiteEntity.class, remap = false)
public class TreeMiteEntityMixin extends PathfinderMob {

    protected TreeMiteEntityMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 90F)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 12.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 10.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10.0F)
                .add(Attributes.FOLLOW_RANGE, 16.0F)
                .add(Attributes.ARMOR, 6F));
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    private void tfg$registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true, (entity) -> entity.getType().is(TFGTags.Entities.VenusPrey)));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
