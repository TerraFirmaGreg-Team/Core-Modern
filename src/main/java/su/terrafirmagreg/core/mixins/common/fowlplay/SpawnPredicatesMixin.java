package su.terrafirmagreg.core.mixins.common.fowlplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;

import aqario.fowlplay.common.entity.bird.BirdEntity;
import aqario.fowlplay.common.worldgen.SpawnPredicates;

@Mixin(value = SpawnPredicates.class, remap = false)
public class SpawnPredicatesMixin {

    @Shadow
    private static boolean hasSkyAccess(LevelAccessor world, BlockPos pos) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean isMidairSpawn(LevelAccessor world, BlockPos pos) {
        throw new AssertionError();
    }

    /**
     * @author Pyritie
     * @reason Make birds spawn on more than just leaf blocks
     */
    @Overwrite
    public static boolean canSpawnPasserines(EntityType<? extends BirdEntity> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return hasSkyAccess(world, pos) && (world.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) || isMidairSpawn(world, pos));
    }
}
