package su.terrafirmagreg.core.common.data.blocks;

import java.util.function.Supplier;

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
import net.minecraft.world.level.LevelReader;
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

import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blockentity.TickerBlockEntity;

// Decoration variant particle emitter.
public class ParticleEmitterDecorationBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(2.0F, 0.0F, 2.0F, 14.0F, 16.0F, 14.0F);

    private final VoxelShape shape;
    private final Supplier<SimpleParticleType> particleType;
    private final double baseX, baseY, baseZ;
    private final double offsetX, offsetY, offsetZ;
    private final double velocityX, velocityY, velocityZ;
    private final int particleCount;
    private final boolean particleForced;
    private final boolean useDustOptions;
    private final float red, green, blue, scale;
    private final boolean hasTicker;
    private final int emitDelay;

    public ParticleEmitterDecorationBlock(
            Properties properties,
            VoxelShape shape,
            Supplier<Item> itemSupplier,
            Supplier<SimpleParticleType> particleType,
            double baseX, double baseY, double baseZ,
            double offsetX, double offsetY, double offsetZ,
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
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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

    // Needs sturdy block below.
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Info("Client display tick. Cannot be every tick. Use ticker for adjustable frequency.")
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (hasTicker && level.getBlockEntity(pos) != null)
            return;
        if (shouldEmit(random))
            spawnClient(level, pos, random);
    }

    private double randOffset(RandomSource r, double range) {
        if (range <= 0)
            return 0;
        return r.nextDouble() * range * (r.nextBoolean() ? 1 : -1);
    }

    private void spawnClient(Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide)
            return;
        for (int i = 0; i < particleCount; i++) {
            double x = pos.getX() + baseX + randOffset(random, offsetX);
            double y = pos.getY() + baseY + (offsetY > 0 ? random.nextDouble() * offsetY : 0);
            double z = pos.getZ() + baseZ + randOffset(random, offsetZ);
            emitClient(level, x, y, z);
        }
    }

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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return hasTicker ? new TickerBlockEntity(pos, state) : null;
    }

    // Client ticker setting emission each tick when enabled.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!hasTicker || !level.isClientSide)
            return null;
        return type == TFGBlockEntities.TICKER_ENTITY.get()
                ? (lvl, p, s, be) -> {
            if (be instanceof TickerBlockEntity && shouldEmit(lvl.random))
                spawnClient(lvl, p, lvl.random);
        }
                : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!canSurvive(state, level, pos)) {
            Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
        }
    }
}
