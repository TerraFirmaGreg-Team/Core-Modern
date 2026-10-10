package su.terrafirmagreg.core.mixins.common.opposing_force;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unusualmodding.opposing_force.entity.Slug;
import com.unusualmodding.opposing_force.entity.base.SummonableMonster;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(value = Slug.class, remap = false)
public class SlugMixin extends SummonableMonster {

    protected SlugMixin(EntityType<? extends SummonableMonster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.14F)
                .add(Attributes.ATTACK_DAMAGE, 12.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2));
    }

    @Inject(method = "tame", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$tame(Player player, CallbackInfo ci) {
        ci.cancel();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Inject(method = "dropFromLootTable", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$dropFromLootTable(DamageSource source, boolean drops, CallbackInfo ci) {
        ci.cancel();
    }
}
