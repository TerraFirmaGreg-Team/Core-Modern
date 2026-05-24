package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_3.Bewereager;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Bewereager.class, remap = false)
public class BewereagerMixin extends Monster {

    protected BewereagerMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Make Ghouls ignore light levels and spawn above Y=0
    @Inject(method = "checkMonsterSpawnRules", at = @At("HEAD"), remap = true, cancellable = true)
    private static void tfg$checkMonsterSpawnRules(EntityType<? extends Monster> entityType, ServerLevelAccessor accessor, MobSpawnType spawnType, BlockPos pos, RandomSource random,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(accessor.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(entityType, accessor, spawnType, pos, random));
    }

    // Remove transforming
    @Inject(method = "transform", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$transform(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
