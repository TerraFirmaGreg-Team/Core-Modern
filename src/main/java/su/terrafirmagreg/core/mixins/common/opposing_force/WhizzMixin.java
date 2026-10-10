package su.terrafirmagreg.core.mixins.common.opposing_force;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unusualmodding.opposing_force.entity.Whizz;
import com.unusualmodding.opposing_force.entity.base.SummonableMonster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

@Mixin(value = Whizz.class, remap = false)
public class WhizzMixin extends SummonableMonster {

    protected WhizzMixin(EntityType<? extends SummonableMonster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0f)
                .add(Attributes.FLYING_SPEED, 0.7F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ATTACK_DAMAGE, 8.0f));
    }

    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$skipAttackInteraction(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
