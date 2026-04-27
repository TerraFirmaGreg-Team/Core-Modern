package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Husk.class)
public abstract class HuskMixin extends Zombie {

    public HuskMixin(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /**
     * @author Pyritie
     * @reason So husks don't have to see the sky in order to spawn
     */
    @Overwrite
    public static boolean checkHuskSpawnRules(EntityType<Husk> entity, ServerLevelAccessor accessor, MobSpawnType spawnType,
            BlockPos pos, RandomSource random) {
        return checkMonsterSpawnRules(entity, accessor, spawnType, pos, random);
    }
}
