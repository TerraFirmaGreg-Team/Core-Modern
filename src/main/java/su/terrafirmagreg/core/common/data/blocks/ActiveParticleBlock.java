package su.terrafirmagreg.core.common.data.blocks;

import java.util.function.Supplier;

import dev.latvian.mods.kubejs.typings.Info;
import org.joml.Vector3f;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

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
 * Particle emitter block with active/inactive states.
 * Adds the ability to have different particle effects based on the active state.
 */
public class ActiveParticleBlock extends ActiveBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    private final VoxelShape shape;
    private final ParticleConfig inactiveConfig;
    private final ParticleConfig activeConfig;
    private final boolean hasTicker;
    private final int emitDelay;

    public ActiveParticleBlock(
            Properties properties,
            VoxelShape shape,
            Supplier<Item> itemSupplier,
            ParticleConfig inactiveConfig,
            ParticleConfig activeConfig,
            boolean hasTicker,
            int emitDelay) {
        super(properties);
        this.shape = shape != null ? shape : DEFAULT_SHAPE;
        this.inactiveConfig = inactiveConfig;
        this.activeConfig = activeConfig;
        this.hasTicker = hasTicker;
        this.emitDelay = Math.max(0, emitDelay);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(GTBlockStateProperties.ACTIVE, false));
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

    @Info("Client display tick. Cannot be every tick. Use ticker for adjustable frequency.")
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (hasTicker && level.getBlockEntity(pos) != null)
            return;
        if (!shouldEmit(random))
            return;
        ParticleConfig cfg = state.getValue(GTBlockStateProperties.ACTIVE) ? activeConfig : inactiveConfig;
        if (cfg != null)
            cfg.spawnClient(level, pos, random);
    }

    // Creates ticker entity if enabled.
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return hasTicker ? new TickerBlockEntity(pos, state) : null;
    }

    // Client ticker setting emission each tick when enabled.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!hasTicker || !level.isClientSide)
            return null;
        if (type != TFGBlockEntities.TICKER_ENTITY.get())
            return null;
        return (lvl, p, s, be) -> {
            if (be instanceof TickerBlockEntity && shouldEmit(lvl.random)) {
                ParticleConfig cfg = s.getValue(GTBlockStateProperties.ACTIVE) ? activeConfig : inactiveConfig;
                if (cfg != null)
                    cfg.spawnClient(lvl, p, lvl.random);
            }
        };
    }

    // Immutable particle emission configuration.
    public static class ParticleConfig {
        private final Supplier<SimpleParticleType> type;
        private final double baseX, baseY, baseZ;
        private final double offsetX, offsetY, offsetZ;
        private final double velocityX, velocityY, velocityZ;
        private final int count;
        private final boolean forced;
        private final boolean useDust;
        private final float r, g, b, scale;

        public ParticleConfig(
                Supplier<SimpleParticleType> type,
                double baseX, double baseY, double baseZ,
                double offsetX, double offsetY, double offsetZ,
                double velocityX, double velocityY, double velocityZ,
                int count,
                boolean forced,
                boolean useDust,
                float r, float g, float b, float scale) {
            this.type = type != null ? type : () -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
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

        private double randOffset(RandomSource rdn, double range) {
            if (range <= 0)
                return 0;
            return rdn.nextDouble() * range * (rdn.nextBoolean() ? 1 : -1);
        }

        private void emitClient(Level level, double x, double y, double z) {
            if (useDust) {
                var dust = new DustParticleOptions(new Vector3f(r, g, b), scale);
                if (forced)
                    level.addAlwaysVisibleParticle(dust, true, x, y, z, velocityX, velocityY, velocityZ);
                else
                    level.addParticle(dust, x, y, z, velocityX, velocityY, velocityZ);
            } else {
                var p = type.get();
                if (forced)
                    level.addAlwaysVisibleParticle(p, true, x, y, z, velocityX, velocityY, velocityZ);
                else
                    level.addParticle(p, x, y, z, velocityX, velocityY, velocityZ);
            }
        }

        // Spawns configured particle batch.
        public void spawnClient(Level level, BlockPos pos, RandomSource random) {
            if (!level.isClientSide)
                return;
            for (int i = 0; i < count; i++) {
                double x = pos.getX() + baseX + randOffset(random, offsetX);
                double y = pos.getY() + baseY + (offsetY > 0 ? random.nextDouble() * offsetY : 0);
                double z = pos.getZ() + baseZ + randOffset(random, offsetZ);
                emitClient(level, x, y, z);
            }
        }
    }
}
