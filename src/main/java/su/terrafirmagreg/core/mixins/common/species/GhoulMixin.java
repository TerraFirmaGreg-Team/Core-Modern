package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_3.Ghoul;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Ghoul.class, remap = false)
public class GhoulMixin extends Monster {

    protected GhoulMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Make Ghouls ignore light levels and spawn above Y=0
    @Inject(method = "checkMonsterSpawnRules", at = @At("HEAD"), remap = true, cancellable = true)
    private static void tfg$checkMonsterSpawnRules(EntityType<? extends Monster> entityType, ServerLevelAccessor accessor, MobSpawnType spawnType, BlockPos pos, RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(accessor.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, accessor, spawnType, pos, random));
    }

    // Change the bloodlust effect to only last 30 seconds and not forever
    @ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;IIZZ)V"), index = 1, remap = true)
    private int tfg$doHurtTarget(int duration) {
        return 20 * 30;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
