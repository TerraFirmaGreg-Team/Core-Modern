package su.terrafirmagreg.core.common.data.blocks;

import java.util.function.Supplier;

import org.joml.Vector3f;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Particle emitter block similar to `su.terrafirmagreg.core.common.data.blocks.ParticleEmitterBlock`
 * But extends GT ActiveBlock so it can generate particles when active/inactive.
 */
public class ActiveParticleBlock extends ActiveBlock {
    /**
     * The constant FACING.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /**
     * The constant DEFAULT_SHAPE.
     */
    public static final VoxelShape DEFAULT_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    private final VoxelShape shape;
    private final ParticleConfig inactiveConfig;
    private final ParticleConfig activeConfig;

    /**
     * Instantiates a new Active particle block.
     *
     * @param properties     the properties
     * @param shape          the shape
     * @param itemSupplier   the item supplier
     * @param inactiveConfig the inactive config
     * @param activeConfig   the active config
     */
    public ActiveParticleBlock(
            Properties properties,
            VoxelShape shape,
            Supplier<Item> itemSupplier,
            ParticleConfig inactiveConfig,
            ParticleConfig activeConfig) {
        super(properties);
        this.shape = shape != null ? shape : DEFAULT_SHAPE;
        this.inactiveConfig = inactiveConfig;
        this.activeConfig = activeConfig;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(GTBlockStateProperties.ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, GTBlockStateProperties.ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        ParticleConfig cfg = state.getValue(GTBlockStateProperties.ACTIVE) ? activeConfig : inactiveConfig;
        if (cfg != null) {
            cfg.spawn(level, pos, random);
        }
    }

    /**
     * The type Particle config.
     */
    public static class ParticleConfig {
        private final Supplier<SimpleParticleType> type;
        private final double offsetX, offsetY, offsetZ;
        private final double velocityX, velocityY, velocityZ;
        private final int count;
        private final boolean forced;
        private final boolean useDust;
        private final float r, g, b, scale;

        /**
         * Instantiates a new Particle config.
         *
         * @param type      the type
         * @param offsetX   the offset x
         * @param offsetY   the offset y
         * @param offsetZ   the offset z
         * @param velocityX the velocity x
         * @param velocityY the velocity y
         * @param velocityZ the velocity z
         * @param count     the count
         * @param forced    the forced
         * @param useDust   the use dust
         * @param r         the r
         * @param g         the g
         * @param b         the b
         * @param scale     the scale
         */
        public ParticleConfig(
                Supplier<SimpleParticleType> type,
                double offsetX, double offsetY, double offsetZ,
                double velocityX, double velocityY, double velocityZ,
                int count,
                boolean forced,
                boolean useDust,
                float r, float g, float b, float scale) {
            this.type = type != null ? type : () -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.velocityZ = velocityZ;
            this.count = Math.max(1, count);
            this.forced = forced;
            this.useDust = useDust;
            this.r = r;
            this.g = g;
            this.b = b;
            this.scale = scale;
        }

        /**
         * Spawn.
         *
         * @param level  the level
         * @param pos    the pos
         * @param random the random
         */
        public void spawn(Level level, BlockPos pos, RandomSource random) {
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + 0.5 + random.nextDouble() * offsetX * (random.nextBoolean() ? 1 : -1);
                double y = pos.getY() + random.nextDouble() * offsetY;
                double z = pos.getZ() + 0.5 + random.nextDouble() * offsetZ * (random.nextBoolean() ? 1 : -1);

                if (useDust) {
                    var dust = new net.minecraft.core.particles.DustParticleOptions(
                            new Vector3f(r, g, b), scale);
                    if (forced) {
                        level.addAlwaysVisibleParticle(dust, true, x, y, z, velocityX, velocityY, velocityZ);
                    } else {
                        level.addParticle(dust, x, y, z, velocityX, velocityY, velocityZ);
                    }
                } else {
                    var p = type.get();
                    if (forced) {
                        level.addAlwaysVisibleParticle(p, true, x, y, z, velocityX, velocityY, velocityZ);
                    } else {
                        level.addParticle(p, x, y, z, velocityX, velocityY, velocityZ);
                    }
                }
            }
        }
    }
}
