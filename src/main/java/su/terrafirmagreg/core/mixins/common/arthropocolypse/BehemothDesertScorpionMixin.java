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

import dev.dh.arthropocolypse.entity.Behemoth_Desert_Scorpion;

@Mixin(value = Behemoth_Desert_Scorpion.class, remap = false)
public class BehemothDesertScorpionMixin extends PathfinderMob {
    protected BehemothDesertScorpionMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0F)
                .add(Attributes.ARMOR, 10.0F)
                .add(Attributes.ARMOR_TOUGHNESS, 4)
                .add(Attributes.FOLLOW_RANGE, 24.0F)
                .add(Attributes.ATTACK_SPEED, 0.5F)
                .add(Attributes.ATTACK_DAMAGE, 3 + 10)
                .add(Attributes.ATTACK_KNOCKBACK, 0.1)
                .add(Attributes.MOVEMENT_SPEED, 0.3));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
