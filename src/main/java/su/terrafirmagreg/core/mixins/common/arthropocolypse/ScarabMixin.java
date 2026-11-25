package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import dev.dh.arthropocolypse.entity.Scarab;
import dev.dh.arthropocolypse.entity.ai.ClimbableEntity;

@Mixin(value = Scarab.class, remap = false)
public class ScarabMixin extends ClimbableEntity {

    protected ScarabMixin(EntityType type, Level level) {
        super(type, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ATTACK_DAMAGE, 2 + 4)
                .add(Attributes.ATTACK_SPEED, 1.0F)
                .add(Attributes.ARMOR, 1.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 0.1F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4F)
                .add(Attributes.FOLLOW_RANGE, 24.0F));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
