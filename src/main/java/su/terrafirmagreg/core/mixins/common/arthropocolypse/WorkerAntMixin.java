package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import dev.dh.arthropocolypse.entity.Worker_Ant;
import dev.dh.arthropocolypse.entity.ai.ClimbableEntity;

@Mixin(value = Worker_Ant.class, remap = false)
public class WorkerAntMixin extends ClimbableEntity {

    protected WorkerAntMixin(EntityType type, Level level) {
        super(type, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.ARMOR, 6.0F)
                .add(Attributes.FOLLOW_RANGE, 24.0F)
                .add(Attributes.ATTACK_SPEED, 1.0F)
                .add(Attributes.ATTACK_DAMAGE, 11)
                .add(Attributes.ATTACK_KNOCKBACK, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.MOVEMENT_SPEED, 0.2));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
