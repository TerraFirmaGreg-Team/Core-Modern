package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_2.Mammutilation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Mammutilation.class, remap = false)
public class MammutilationMixin {

    @Inject(method = "canSpawn", cancellable = true, at = @At("HEAD"))
    private static void tfg$canSpawn(EntityType<Mammutilation> entity, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
