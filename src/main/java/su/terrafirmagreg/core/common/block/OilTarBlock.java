package su.terrafirmagreg.core.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OilTarBlock extends Block {

    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public OilTarBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState p_54015_, BlockGetter p_54016_, BlockPos p_54017_, CollisionContext p_54018_) {
        return SHAPE;
    }

    //idk if this is even needed
    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.defaultBlockState()), entity.getX(),
                entity.getY(), entity.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15F);
        return true;
    }
}
