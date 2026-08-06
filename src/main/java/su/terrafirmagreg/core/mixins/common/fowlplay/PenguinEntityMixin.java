package su.terrafirmagreg.core.mixins.common.fowlplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.animal.Animal;

import aqario.fowlplay.common.entity.bird.penguin.PenguinEntity;

@Mixin(value = PenguinEntity.class, remap = false)
public class PenguinEntityMixin {
    //    @Inject(method = "canSpawnPenguins", at = @At("HEAD"), remap = false, cancellable = true)
    //    private static void tfg$canSpawnPenguins(EntityType<? extends BirdEntity> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random,
    //            CallbackInfoReturnable<Boolean> cir) {
    //
    //        var level = world.getServer().getLevel(Level.OVERWORLD);
    //        float rainfall = Climate.getRainfall(level, pos);
    //        float temp = Climate.getAverageTemperature(level, pos);
    //
    //        if (rainfall < 75 || temp > -14) {
    //            cir.cancel();
    //        }
    //    }

    /**
     * @author Pyritie
     * @reason Prevent vanilla-like breeding
     */
    @Inject(method = "canMate", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$canMate(Animal other, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
