package su.terrafirmagreg.core.mixins.common.beneath;

import com.eerussianguy.beneath.common.blocks.BurpingFlowerBlock;
import com.eerussianguy.beneath.common.blocks.FacingFlowerBlock;
import com.eerussianguy.beneath.misc.BeneathParticles;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the default burpflower recipes with our own,
 * because they're done through code instead of through recipes
 */
@Mixin(value = BurpingFlowerBlock.class, remap = false)
public abstract class BurpingFlowerBlockMixin {

	@Shadow
	public abstract boolean meetsConditions(Level level, BlockPos pos, BlockState state);

	@Inject(method = "performAction", at = @At("HEAD"), remap = false, cancellable = true)
	public void tfg$performAction(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfo ci)
	{
		final Direction dir = state.getValue(FacingFlowerBlock.FACING);
		final BlockPos resultPos = pos.relative(dir);
		final BlockState stateAtResult = level.getBlockState(resultPos);
		level.sendParticles(BeneathParticles.SULFURIC_SMOKE.get(), resultPos.getX() + 0.5, resultPos.getY() + 0.5,
			resultPos.getZ() + 0.5, 5, 0, 0, 0, 1);

		boolean worked = true;
		if (Helpers.isBlock(stateAtResult, Blocks.SHROOMLIGHT))
		{
			var rand = random.nextFloat();
			if (rand < 0.33f)
			{
				level.setBlockAndUpdate(resultPos, Blocks.OCHRE_FROGLIGHT.defaultBlockState());
			}
			else if (rand < 0.67f)
			{
				level.setBlockAndUpdate(resultPos, Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState());
			}
			else
			{
				level.setBlockAndUpdate(resultPos, Blocks.VERDANT_FROGLIGHT.defaultBlockState());
			}
		}
		else
		{
			worked = false;
		}

		if (worked && meetsConditions(level, pos, state))
		{
			final BlockPos sulfurPos = pos.relative(state.getValue(FacingFlowerBlock.FACING).getOpposite());
			level.setBlockAndUpdate(sulfurPos, level.getBlockState(sulfurPos).getFluidState().createLegacyBlock());
		}

		ci.cancel();
	}
}
