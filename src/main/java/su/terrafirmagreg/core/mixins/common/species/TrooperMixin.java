package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_2.Trooper;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Trooper.class, remap = false)
public class TrooperMixin {

    // Enable spawns

    @Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$canSpawn(EntityType<Trooper> entity, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                world.getBlockState(pos.below()).is(TFCTags.Blocks.MONSTER_SPAWNS_ON)
                        && world.getBlockState(pos.below()).isValidSpawn(world, pos, entity));
    }
}
