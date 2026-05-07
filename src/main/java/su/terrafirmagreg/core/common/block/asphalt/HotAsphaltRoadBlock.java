package su.terrafirmagreg.core.common.block.asphalt;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

@SuppressWarnings("deprecation")
public class HotAsphaltRoadBlock extends Block {

    public static final int TICKS_UNTIL_SET = 100;

    public HotAsphaltRoadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        TickCounterBlockEntity.reset(level, pos);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, TICKS_UNTIL_SET);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        level.setBlock(pos, TFGBlocks.ASPHALT_ROAD.getDefaultState(), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos, TFGBlocks.ASPHALT_ROAD.get());
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide() || !(entity instanceof LivingEntity living) || living.isSteppingCarefully()) {
            return;
        }
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        DamageSource src = level.damageSources().hotFloor();
        if (!living.isInvulnerableTo(src)) {
            living.hurt(src, 0.5F);
        }
    }
}
