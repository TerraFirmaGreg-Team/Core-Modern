package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

@Mixin(value = Bat.class, remap = true)
public abstract class BatMixin extends AmbientCreature {
    protected BatMixin(EntityType<? extends AmbientCreature> entityType, Level level) {
        super(entityType, level);
    }

    // Bats are hardcoded to only spawn below sea level until 1.21.2

    @Inject(method = "checkBatSpawnRules", at = @At("HEAD"), remap = true, cancellable = true)
    private static void tfg$checkBatSpawnRules(EntityType<Bat> bat, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (level.dimensionType().hasCeiling()) {
            if (random.nextBoolean()) {
                cir.setReturnValue(false);
                return;
            }
            cir.setReturnValue(level.getMaxLocalRawBrightness(pos) <= random.nextInt(4) && checkMobSpawnRules(bat, level, spawnType, pos, random));
        }
    }
}
