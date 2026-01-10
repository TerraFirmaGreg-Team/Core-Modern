package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(value = SmallFireball.class)
public abstract class SmallFireballMixin extends Fireball {

    public SmallFireballMixin(EntityType<? extends Fireball> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Give Blaze fireballs some physical damage so they can't be completely ignored by the t2 space suit

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    protected void tfg$onHitEntity(EntityHitResult pResult, CallbackInfo ci) {
        if (!this.level().isClientSide) {
            Entity entity = pResult.getEntity();
            entity.hurt(this.damageSources().mobProjectile(this, this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null),
                    5);
        }
    }
}
