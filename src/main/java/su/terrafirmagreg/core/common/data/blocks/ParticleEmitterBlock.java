package su.terrafirmagreg.core.common.data.blocks;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blockentity.TickerBlockEntity;

/**
 * Basic particle emitter block.
 *
 * <p>Emits a single configured particle set using animateTick
 * or by a ticker block entity when {@code hasTicker} is true.
 *
 * <p>Features:
 * <p>- Horizontal FACING.
 * <p>- Configuration with base {@code position} and random {@code range}.
 * <p>- Optional DustParticleOptions when dust mode is enabled.
 */
@SuppressWarnings({ "deprecation", "unused" })
public class ParticleEmitterBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    private final VoxelShape shape;
    private final Supplier<SimpleParticleType> particleType;
    private final double posX, posY, posZ;
    private final double rangeX, rangeY, rangeZ;
    private final double velocityX, velocityY, velocityZ;
    private final int particleCount;
    private final boolean particleForced;
    private final boolean useDustOptions;
    private final float red, green, blue, scale;
    private final boolean hasTicker;
    private final int emitDelay;

    /**
     * Create a simple particle emitter.
     *
     * @param properties    Default properties.
     * @param shape         Collision shape. If null, a full cube is used.
     * @param itemSupplier  Supplier for the block item. Can be null.
     * @param particleType  Particle type supplier (SimpleParticleType).
     * @param posX          Base local position X.
     * @param posY          Base local position Y.
     * @param posZ          Base local position Z.
     * @param rangeX        Random range X.
     * @param rangeY        Random range Y.
     * @param rangeZ        Random range Z.
     * @param velocityX     Particle velocity X.
     * @param velocityY     Particle velocity Y.
     * @param velocityZ     Particle velocity Z.
     * @param particleCount Particles per emission (>= 1).
     * @param particleForced Always visible when true.
     * @param useDustOptions Enable DustParticleOptions (use {@code red, green, blue, scale}).
     * @param red           Dust red channel.
     * @param green         Dust green channel.
     * @param blue          Dust blue channel.
     * @param scale         Dust scale.
     * @param hasTicker     Whether a ticker entity controls emission frequency.
     * @param emitDelay     Random delay scaling (0 = every tick).
     */
    public ParticleEmitterBlock(
            Properties properties,
            VoxelShape shape,
            Supplier<Item> itemSupplier,
            Supplier<SimpleParticleType> particleType,
            double posX, double posY, double posZ,
            double rangeX, double rangeY, double rangeZ,
            double velocityX, double velocityY, double velocityZ,
            int particleCount,
            boolean particleForced,
            boolean useDustOptions,
            float red, float green, float blue, float scale,
            boolean hasTicker,
            int emitDelay) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.shape = shape != null ? shape : DEFAULT_SHAPE;
        this.particleType = particleType != null ? particleType : () -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rangeX = rangeX;
        this.rangeY = rangeY;
        this.rangeZ = rangeZ;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.particleCount = Math.max(1, particleCount);
        this.particleForced = particleForced;
        this.useDustOptions = useDustOptions;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = scale;
        this.hasTicker = hasTicker;
        this.emitDelay = Math.max(0, emitDelay);
    }

    private boolean shouldEmit(RandomSource random) {
        if (emitDelay <= 0)
            return true;
        int inner = 1 + random.nextInt(emitDelay);
        return random.nextInt(inner) == 0;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shape != null ? shape : super.getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * Client display tick. Emits particles on the client side.
     * <p>Uses random tick logic. When {@code hasTicker} is true and a block
     * entity exists at the position, animation is deferred to the ticker.
     */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (hasTicker && level.getBlockEntity(pos) != null)
            return;
        if (shouldEmit(random))
            spawnClient(level, pos, random);
    }

    /**
     * Compute a symmetric random offset in [-range, +range].
     */
    private double randRange(RandomSource r, double range) {
        if (range <= 0)
            return 0;
        return r.nextDouble() * range * (r.nextBoolean() ? 1 : -1);
    }

    /**
     * Spawn the configured particles client side around the base {@code position}
     * with the given {@code range} and velocity.
     */
    private void spawnClient(Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide)
            return;
        for (int i = 0; i < particleCount; i++) {
            double x = pos.getX() + posX + randRange(random, rangeX);
            double y = pos.getY() + posY + (rangeY > 0 ? random.nextDouble() * rangeY : 0);
            double z = pos.getZ() + posZ + randRange(random, rangeZ);
            emitClient(level, x, y, z);
        }
    }

    /**
     * Emit a single particle with configured velocity.
     */
    private void emitClient(Level level, double x, double y, double z) {
        if (useDustOptions) {
            var dust = new DustParticleOptions(new Vector3f(red, green, blue), scale);
            if (particleForced)
                level.addAlwaysVisibleParticle(dust, true, x, y, z, velocityX, velocityY, velocityZ);
            else
                level.addParticle(dust, x, y, z, velocityX, velocityY, velocityZ);
        } else {
            SimpleParticleType type = particleType.get();
            if (particleForced)
                level.addAlwaysVisibleParticle(type, true, x, y, z, velocityX, velocityY, velocityZ);
            else
                level.addParticle(type, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    // Creates ticker entity if enabled.
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return hasTicker ? new TickerBlockEntity(pos, state) : null;
    }

    // Client ticker setting emission each tick when enabled.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!hasTicker || !level.isClientSide)
            return null;
        return type == TFGBlockEntities.TICKER_ENTITY.get()
                ? (lvl, p, s, be) -> {
                    if (be instanceof TickerBlockEntity && shouldEmit(lvl.random))
                        spawnClient(lvl, p, lvl.random);
                }
                : null;
    }
}
