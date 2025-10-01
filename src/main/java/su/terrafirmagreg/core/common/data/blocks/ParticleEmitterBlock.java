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

public class ParticleEmitterBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(0, 0, 0, 16, 16, 16);

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

    public ParticleEmitterBlock(
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
            boolean hasTicker) {
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
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Info("Client visual tick (skipped if ticker entity present).")
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (hasTicker && level.getBlockEntity(pos) != null)
            return;
        spawnParticlesClient(level, pos, random);
    }

    private double randOffset(RandomSource r, double range) {
        if (range <= 0)
            return 0;
        return r.nextDouble() * range * (r.nextBoolean() ? 1 : -1);
    }

    private void spawnParticlesClient(Level level, BlockPos pos, RandomSource random) {
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

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return hasTicker ? new TickerBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!hasTicker || !level.isClientSide) return null;
        return type == TFGBlockEntities.TICKER_ENTITY.get()
                ? (lvl, p, s, be) -> {
            if (be instanceof TickerBlockEntity t && t.shouldEmit(lvl)) {
                spawnParticlesClient(lvl, p, lvl.random);
            }
        }
        : null;
    }
}
