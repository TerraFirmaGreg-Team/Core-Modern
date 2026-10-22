package su.terrafirmagreg.core.mixins.common.opposing_force;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unusualmodding.opposing_force.entity.HangingSpider;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = HangingSpider.class, remap = false)
public class HangingSpiderMixin extends Spider {

    public HangingSpiderMixin(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 70.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ATTACK_DAMAGE, 15.0F)
                .add(Attributes.ARMOR, 6.0F));
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
