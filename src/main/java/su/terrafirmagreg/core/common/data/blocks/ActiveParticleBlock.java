package su.terrafirmagreg.core.common.data.blocks;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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

import dev.latvian.mods.kubejs.typings.Info;
import lombok.AllArgsConstructor;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blockentity.TickerBlockEntity;

/**
 * Particle emitter block with active/inactive states.
 *
 * <p>This block can emit particles according to two separate
 * configuration lists. One for the inactive state and one for the active
 * state. Emission can be performed either from the block's client-side
 * animateTick or via a ticker block entity when {@code hasTicker} is true.
 *
 * <p>Extra Features:
 * <p>- AXIS orientable.
 * <p>- Activity is controlled by GTBlockStateProperties.ACTIVE.
 * <p>- Light level is configurable independently for active and inactive.
 */
@SuppressWarnings({ "deprecation", "unused" })
public class ActiveParticleBlock extends ActiveBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    private final VoxelShape shape;
    private final List<ParticleConfig> inactiveConfigs;
    private final List<ParticleConfig> activeConfigs;
    private final boolean hasTicker;
    private final int emitDelay;
    private final int inactiveLight;
    private final int activeLight;

    /**
     * Create a new ActiveParticleBlock.
     *
     * @param properties      Default properties.
     * @param shape           Collision shape. If null, a full cube is used.
     * @param itemSupplier    Supplier for the block item. Can be null.
     * @param inactiveConfigs List of particle configs used when the block is inactive.
     * @param activeConfigs   List of particle configs used when the block is active.
     * @param hasTicker       Whether the block uses a block entity ticker for controlled emission.
     * @param emitDelay       Average delay for emission (0 = every tick).
     * @param inactiveLight   Light level when inactive (0-15).
     * @param activeLight     Light level when active (0-15).
     */
    public ActiveParticleBlock(
            Properties properties,
            @Nullable VoxelShape shape,
            @Nullable Supplier<Item> itemSupplier,
            @Nullable List<ParticleConfig> inactiveConfigs,
            @Nullable List<ParticleConfig> activeConfigs,
            boolean hasTicker,
            int emitDelay,
            int inactiveLight,
            int activeLight) {
        super(properties);
        this.shape = shape != null ? shape : DEFAULT_SHAPE;
        this.inactiveConfigs = inactiveConfigs != null ? inactiveConfigs : List.of();
        this.activeConfigs = activeConfigs != null ? activeConfigs : List.of();
        this.hasTicker = hasTicker;
        this.emitDelay = Math.max(0, emitDelay);
        this.inactiveLight = clampLight(inactiveLight);
        this.activeLight = clampLight(activeLight);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(GTBlockStateProperties.ACTIVE, false));
    }

    public ActiveParticleBlock(Properties properties, @Nullable VoxelShape shape, @Nullable Supplier<Item> itemSupplier, @Nullable List<ParticleConfig> inactiveConfigs,
            @Nullable List<ParticleConfig> activeConfigs) {
        this(properties, shape, itemSupplier, inactiveConfigs, activeConfigs, false, 0, 0, 0);
    }

    private static int clampLight(int v) {
        if (v < 0)
            return 0;
        return Math.min(v, 15);
    }

    private boolean shouldEmit(RandomSource random) {
        if (emitDelay <= 0)
            return true;
        int inner = 1 + random.nextInt(emitDelay);
        return random.nextInt(inner) == 0;
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

    /**
     * Client display tick. Emits particles on the client side.
     *
     * <p>This method uses random tick logic for emission. When {@code hasTicker} is true and a block
     * entity exists at the position, animation is deferred to the ticker.
     *
     * @param state  Current block state (used to determine active/inactive).
     * @param level  Client instance.
     * @param pos    Block position.
     * @param random Random source.
     */
    @Info("Client display tick. Cannot be every tick. Use ticker for adjustable frequency.")
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (hasTicker && level.getBlockEntity(pos) != null)
            return;
        if (!shouldEmit(random))
            return;
        var list = state.getValue(GTBlockStateProperties.ACTIVE) ? activeConfigs : inactiveConfigs;
        if (!list.isEmpty()) {
            for (var cfg : list) {
                cfg.spawnClient(level, pos, random);
            }
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(GTBlockStateProperties.ACTIVE) ? activeLight : inactiveLight;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return hasTicker ? new TickerBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!hasTicker || !level.isClientSide)
            return null;
        if (type != TFGBlockEntities.TICKER_ENTITY.get())
            return null;
        return (lvl, p, s, be) -> {
            if (be instanceof TickerBlockEntity && shouldEmit(lvl.random)) {
                var list = s.getValue(GTBlockStateProperties.ACTIVE) ? activeConfigs : inactiveConfigs;
                if (!list.isEmpty()) {
                    for (var cfg : list) {
                        cfg.spawnClient(lvl, p, lvl.random);
                    }
                }
            }
        };
    }

    /**
     * Immutable configuration.
     *
     * <p>Each instance defines:
     * <p>- particle type (or useDust + RGB for DustParticleOptions).
     * <p>- base local offset position (posX/posY/posZ).
     * <p>- randomization ranges on each axis.
     * <p>- particle velocity.
     * <p>- particle count.
     * <p>- whether the particle is forced (always visible) or standard.
     */
    @AllArgsConstructor
    public static class ParticleConfig {
        private final Supplier<SimpleParticleType> type;
        private final double posX, posY, posZ;
        private final double rangeX, rangeY, rangeZ;
        private final double velocityX, velocityY, velocityZ;
        private final int count;
        private final boolean forced;
        private final boolean useDust;
        private final float r, g, b, scale;

        private double randRange(RandomSource rdn, double range) {
            if (range <= 0)
                return 0;
            return rdn.nextDouble() * range * (rdn.nextBoolean() ? 1 : -1);
        }

        private void emitClient(Level level, double x, double y, double z) {
            if (useDust) {
                var dust = new DustParticleOptions(new Vector3f(r, g, b), scale);
                if (forced)
                    level.addAlwaysVisibleParticle(dust, x, y, z, velocityX, velocityY, velocityZ);
                else
                    level.addParticle(dust, x, y, z, velocityX, velocityY, velocityZ);
            } else {
                var p = type.get();
                if (forced)
                    level.addAlwaysVisibleParticle(p, x, y, z, velocityX, velocityY, velocityZ);
                else
                    level.addParticle(p, x, y, z, velocityX, velocityY, velocityZ);
            }
        }

        /**
         * Spawn configured particles on the client.
         *
         * <p>Only executes when {@code level.isClientSide} is true. Particles are
         * spawned around the block position + configured offsets and ranges.
         *
         * @param level  The client Level where particles should be spawned.
         * @param pos    Block position used as the base.
         * @param random Random source used for position jitter.
         */
        public void spawnClient(Level level, BlockPos pos, RandomSource random) {
            if (!level.isClientSide)
                return;
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + posX + randRange(random, rangeX);
                double y = pos.getY() + posY + (rangeY > 0 ? random.nextDouble() * rangeY : 0);
                double z = pos.getZ() + posZ + randRange(random, rangeZ);
                emitClient(level, x, y, z);
            }
        }
    }
}
