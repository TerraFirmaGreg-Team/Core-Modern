package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

@Mixin(value = Slime.class)
public abstract class SlimeMixin extends Mob {
    protected SlimeMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    /**
     * @author Pyritie
     * @reason Give slimes normal mob spawning behaviour, none of this slime chunk nonsense
     */
    @Overwrite
    public static boolean checkSlimeSpawnRules(EntityType<Slime> type, LevelAccessor accessor, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return checkMobSpawnRules(type, accessor, spawnType, pos, random);
    }
}
