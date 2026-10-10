package su.terrafirmagreg.core.mixins.common.opposing_force;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unusualmodding.opposing_force.entity.Trembler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Trembler.class, remap = false)
public class TremblerMixin extends Monster {

    protected TremblerMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.15F)
                .add(Attributes.ATTACK_DAMAGE, 10.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5F)
                .add(Attributes.ARMOR, 20.0F));
    }

    @Inject(method = "canTremblerSpawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$canTremblerSpawn(EntityType<Trembler> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(level.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, level, spawnType, pos, random));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
