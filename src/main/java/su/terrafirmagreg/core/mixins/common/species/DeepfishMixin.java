package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_1.Deepfish;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

@Mixin(value = Deepfish.class, remap = false)
public class DeepfishMixin {

    // Normally this looks for a Y value below 0, but europa's crust is at 300

    @Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$canSpawn(EntityType<? extends WaterAnimal> type, LevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        // 200 instead of 300 so it spawns on the deeper side, feel free to change
        cir.setReturnValue(pos.getY() <= 200 && world.getRawBrightness(pos, 0) == 0 && world.getBlockState(pos).is(Blocks.WATER));
    }
}
