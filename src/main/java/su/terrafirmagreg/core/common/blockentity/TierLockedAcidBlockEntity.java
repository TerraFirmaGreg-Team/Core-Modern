package su.terrafirmagreg.core.common.blockentity;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.TierLockedAcidBlock;

public class TierLockedAcidBlockEntity extends TickerBlockEntity {
    private static final int NEUTRALIZATION_DURATION = 9600;
    private long neutralizationStartTime = -1L;
    private int tickCount = 0;

    public TierLockedAcidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TierLockedAcidBlockEntity be) {
        if (level.isClientSide || be.neutralizationStartTime < 0 || ++be.tickCount < 10) {
            return;
        } else {
            be.tickCount = 0;
        }

        if (Calendars.SERVER.getTicks() - be.neutralizationStartTime >= NEUTRALIZATION_DURATION) {
            // TODO: Change to proper block
            level.setBlockAndUpdate(pos, TFCBlocks.ROCK_BLOCKS.get(Rock.ANDESITE).get(Rock.BlockType.RAW).get().defaultBlockState());
        }
    }

    public void updateLiquidContact() {
        if (level == null || level.isClientSide)
            return;

        boolean touchingLiquid = TierLockedAcidBlock.isTouchingLiquid(level, worldPosition);

        if (touchingLiquid && neutralizationStartTime < 0) {
            neutralizationStartTime = Calendars.SERVER.getTicks();
            this.setChanged();
        } else if (!touchingLiquid && neutralizationStartTime >= 0) {
            neutralizationStartTime = -1L;
            this.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (neutralizationStartTime >= 0) {
            tag.putLong("NeutralizationStartTime", neutralizationStartTime);
        } else {
            tag.remove("NeutralizationStartTime");
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        neutralizationStartTime = tag.contains("NeutralizationStartTime") ? tag.getLong("NeutralizationStartTime") : -1L;
    }
}
