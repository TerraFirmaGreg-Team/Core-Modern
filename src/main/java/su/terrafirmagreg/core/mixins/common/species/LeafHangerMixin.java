package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.ninni.species.server.entity.mob.update_3.Hanger;
import com.ninni.species.server.entity.mob.update_3.LeafHanger;

import net.dries007.tfc.common.entities.aquatic.AquaticMob;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;

@Mixin(value = LeafHanger.class, remap = false)
public abstract class LeafHangerMixin implements AquaticMob {

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSpawnIn(Fluid fluid) {
        return fluid.is(FluidTags.WATER);
    }

    /**
     * @author Pyritie
     * @reason Lets leaf hangers spawn in salt water, and above the sea level
     */
    @Overwrite
    public static boolean canSpawn(EntityType<? extends Hanger> type, LevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return world.getFluidState(pos).is(FluidTags.WATER)
                && world.getFluidState(pos.above()).is(FluidTags.WATER)
                && world.getDifficulty() != Difficulty.PEACEFUL;
    }
}
