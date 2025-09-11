package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.registry.SpeciesTags;
import com.ninni.species.server.entity.mob.update_1.Limpet;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Limpet.class, remap = false)
public abstract class LimpetMixin {

    // Let it spawn anywhere instead of only underground
    @Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$canSpawn(EntityType<? extends PathfinderMob> entityType, ServerLevelAccessor levelAccessor,
            MobSpawnType spawnType, BlockPos blockPos, RandomSource randomSource, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                levelAccessor.getBrightness(LightLayer.BLOCK, blockPos) == 0
                        && levelAccessor.getBlockState(blockPos.below()).is(SpeciesTags.LIMPET_SPAWNABLE_ON)
                        && levelAccessor.getBlockState(blockPos.below()).isValidSpawn(levelAccessor, blockPos,
                                entityType));
    }
}
