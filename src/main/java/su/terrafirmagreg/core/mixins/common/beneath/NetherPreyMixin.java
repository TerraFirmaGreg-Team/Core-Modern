package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.eerussianguy.beneath.common.entities.prey.NetherPrey;

import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = NetherPrey.class, remap = false)
public class NetherPreyMixin {

    /**
     * @author Pyritie
     * @reason Make red elk not spawn in the air
     */
    @Overwrite
    public static boolean spawnRules(EntityType<? extends NetherPrey> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand) {
        final BlockPos.MutableBlockPos cursor = pos.mutable();
        do {
            cursor.move(0, 1, 0);
        } while (Helpers.isFluid(level.getFluidState(cursor), FluidTags.LAVA));

        return level.getBlockState(cursor).isAir() && !level.getBlockState(cursor.below()).isAir(); // this bit is new
    }

    /**
     * @author Pyritie
     * @reason Make red elk not take damage from water
     */
    @Overwrite(remap = true)
    public boolean isSensitiveToWater() {
        return false;
    }

    /**
     * @author Pyritie
     * @reason Red elk are stupid
     */
    @Overwrite(remap = true)
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        ((NetherPrey) (Object) this).resetFallDistance();
    }
}
