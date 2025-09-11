package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;

/**
 * Makes the sliding doors work with GT/Create wrenches for locking/unlocking/destroying
 */

@Mixin(value = SlidingDoorBlock.class, remap = false)
public abstract class SlidingDoorBlockMixin implements IWrenchable {

    @Shadow
    public abstract void onWrench(Level level, BlockPos pos, BlockState state, Direction side, Player user,
            Vec3 hitPos);

    @Shadow
    protected abstract void destroy(Level level, BlockPos pos, BlockState state);

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        onWrench(context.getLevel(), context.getClickedPos(), state, context.getHorizontalDirection(),
                context.getPlayer(), Vec3.ZERO);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        destroy(context.getLevel(), context.getClickedPos(), state);
        return InteractionResult.SUCCESS;
    }
}
