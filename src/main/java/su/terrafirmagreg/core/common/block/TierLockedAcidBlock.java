package su.terrafirmagreg.core.common.block;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.AllFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.blockentity.TierLockedAcidBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

@SuppressWarnings("deprecation")
public class TierLockedAcidBlock extends ParticleEmitterBlock {
    public TierLockedAcidBlock(
            Properties properties,
            @Nullable VoxelShape shape,
            Supplier<Item> itemSupplier,
            @Nullable Supplier<SimpleParticleType> particleType,
            double posX, double posY, double posZ,
            double rangeX, double rangeY, double rangeZ,
            double velocityX, double velocityY, double velocityZ,
            int particleCount,
            boolean particleForced,
            boolean useDustOptions,
            float red, float green, float blue, float scale,
            boolean hasTicker,
            int emitDelay) {
        super(
                properties,
                shape,
                itemSupplier,
                particleType,
                posX, posY, posZ,
                rangeX, rangeY, rangeZ,
                velocityX, velocityY, velocityZ,
                Math.max(1, particleCount),
                particleForced,
                useDustOptions,
                red, green, blue, scale,
                hasTicker,
                Math.max(0, emitDelay));
    }

    public TierLockedAcidBlock(Properties properties) {
        this(
                properties,
                DEFAULT_SHAPE,
                null,
                ParticleTypes.SMOKE::getType,
                0.5, 0.5, 0.5,
                0.4, 0, 0.4,
                0, 0.1, 0,
                5,
                false,
                false,
                1, 1, 1, 1,
                false,
                10);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TierLockedAcidBlockEntity(TFGBlockEntities.TIER_LOCKED_ACID_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isTouchingLiquid(level, pos))
            return;

        super.animateTick(state, level, pos, random);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (level.getBlockEntity(pos) instanceof TierLockedAcidBlockEntity be) {
            be.updateLiquidContact();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide)
            return null;

        return type == TFGBlockEntities.TIER_LOCKED_ACID_BLOCK_ENTITY.get()
                ? (lvl, p, s, be) -> {
                    if (be instanceof TierLockedAcidBlockEntity hornfel)
                        TierLockedAcidBlockEntity.serverTick(lvl, p, s, hornfel);
                }
                : null;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        ItemStack stack = player.getMainHandItem();

        if (isCorrectTool(stack)) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        return 0F;
    }

    private boolean isCorrectTool(ItemStack stack) {
        return stack.isCorrectToolForDrops(this.defaultBlockState());
    }

    public static boolean isTouchingLiquid(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            // TODO: Change to proper liquid
            if (level.getFluidState(pos.relative(direction)).is(AllFluids.HONEY.get())) {
                return true;
            }
        }

        return false;
    }
}
