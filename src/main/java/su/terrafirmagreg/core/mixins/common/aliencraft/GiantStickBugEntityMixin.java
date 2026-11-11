package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import en.predator97427fr.aliencraft.entity.GiantStickBugEntity;

@Mixin(value = GiantStickBugEntity.class, remap = false)
public class GiantStickBugEntityMixin extends PathfinderMob {

    protected GiantStickBugEntityMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 150.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 12.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 3.0F)
                .add(Attributes.FOLLOW_RANGE, 32.0F)
                .add(Attributes.ARMOR, 10F));
    }

    // Remove the mob's immunity to everything but potion effects
    @Inject(method = "hurt", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$hurt(DamageSource damagesource, float amount, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.hurt(damagesource, amount));
    }

    // Remove it trying to check for nearby logs/leaves to destroy
    @Inject(method = "clearNearbyFoliage", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$clearNearbyFoliage(BlockPos center, CallbackInfo ci) {
        ci.cancel();
    }
}
