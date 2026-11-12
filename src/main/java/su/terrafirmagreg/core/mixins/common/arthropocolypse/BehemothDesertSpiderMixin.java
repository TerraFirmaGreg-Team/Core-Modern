package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import dev.dh.arthropocolypse.entity.Behemoth_Desert_Spider;

@Mixin(value = Behemoth_Desert_Spider.class, remap = false)
public class BehemothDesertSpiderMixin extends PathfinderMob {
    protected BehemothDesertSpiderMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1300.0F)
                .add(Attributes.ARMOR, 20.0F)
                .add(Attributes.ARMOR_TOUGHNESS, 8)
                .add(Attributes.FOLLOW_RANGE, 48.0F)
                .add(Attributes.ATTACK_SPEED, 1.0F)
                .add(Attributes.ATTACK_DAMAGE, 40)
                .add(Attributes.ATTACK_KNOCKBACK, 0.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.MOVEMENT_SPEED, 0.3));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
