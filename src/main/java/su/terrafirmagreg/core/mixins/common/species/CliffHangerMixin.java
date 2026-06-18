package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.ninni.species.server.entity.mob.update_3.CliffHanger;
import com.ninni.species.server.entity.mob.update_3.Hanger;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = CliffHanger.class, remap = false)
public abstract class CliffHangerMixin extends Hanger {

    protected CliffHangerMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract void setAttached(boolean attached);

    @Override
    public boolean fireImmune() {
        return true;
    }

    /**
     * @author Pyritie
     * @reason Change spawn conditions so cliff hangers can spawn in the beneath properly
     */
    @Overwrite
    public static boolean canSpawn(EntityType<? extends PathfinderMob> entityType, ServerLevelAccessor levelAccessor, MobSpawnType spawnType, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.NETHER_CARVER_REPLACEABLES)
                && levelAccessor.getBlockState(blockPos.below()).isValidSpawn(levelAccessor, blockPos, entityType)
                && levelAccessor.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Inject(method = "hurt", at = @At("TAIL"), remap = true)
    private void tfg$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        setTarget(null);
        deactivateTongue();
        setCantAttackTicks(20);
    }
}
