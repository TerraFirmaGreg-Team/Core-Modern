package su.terrafirmagreg.core.mixins.common.ad_astra;

import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import earth.terrarium.adastra.common.systems.EnvironmentEffects;
import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.*;
import net.dries007.tfc.common.blocks.devices.*;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.IGrassBlock;
import net.dries007.tfc.util.LampFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.TFGCore;

@Mixin(value = EnvironmentEffects.class, remap = false)
public abstract class EnvironmentEffectsMixin {

	@Shadow
	private static boolean hasOxygenOnAnySide(ServerLevel level, BlockPos pos) {
		TFGCore.LOGGER.warn("EnvironmentEffectsMixin - Failed to bind mixin");
		return false;
	}

	// This was supposed to just be a mixin into tickBlock, but due to a bug in ad astra, that's never called
	// unless you're on a temperate planet with no oxygen
	// See: https://github.com/terrarium-earth/Ad-Astra/pull/734

	@Inject(method = "tickHot", at = @At("TAIL"), remap = false)
	private static void tfg$tickHot(ServerLevel level, BlockPos pos, BlockState state, CallbackInfo ci)
	{
		tfg$tickBlockBugWorkaround(level, pos, state);
	}

	@Inject(method = "tickCold", at = @At("TAIL"), remap = false)
	private static void tfg$tickCold(ServerLevel level, BlockPos pos, BlockState state, CallbackInfo ci)
	{
		tfg$tickBlockBugWorkaround(level, pos, state);
	}

	@Unique
	private static void tfg$tickBlockBugWorkaround(ServerLevel level, BlockPos pos, BlockState state)
	{
		Block block = state.getBlock();
		if (hasOxygenOnAnySide(level, pos))
			return;

		if (block instanceof TFCTorchBlock) {
			level.setBlockAndUpdate(pos, TFCBlocks.DEAD_TORCH.get().defaultBlockState());
		} else if (block instanceof TFCWallTorchBlock) {
			level.setBlockAndUpdate(pos, TFCBlocks.DEAD_WALL_TORCH.get().defaultBlockState());
		} else if (block instanceof IGrassBlock grassBlock) {
			level.setBlockAndUpdate(pos, grassBlock.getDirt());
		} else if (block instanceof FarmlandBlock) {
			FarmlandBlock.turnToDirt(state, level, pos);
		} else if (block instanceof TFCCandleBlock) {
			level.setBlockAndUpdate(pos, state.setValue(TFCCandleBlock.LIT, false));
		} else if (block instanceof FirepitBlock) {
			var be = level.getBlockEntity(pos);
			if (be instanceof AbstractFirepitBlockEntity<?> firepit) {
				firepit.extinguish(state);
			}
		} else if (block instanceof TFCCandleCakeBlock) {
			level.setBlockAndUpdate(pos, state.setValue(TFCCandleCakeBlock.LIT, false));
		} else if (block instanceof PitKilnBlock) {
			level.destroyBlock(pos, false);
		} else if (block instanceof BloomeryBlock) {
			// TODO: doesn't seem to work
			var be = level.getBlockEntity(pos, TFCBlockEntities.BLOOMERY.get());
			if (be.isPresent()) {
				be.get().ejectInventory();
			}
			level.setBlockAndUpdate(pos, state.setValue(BloomeryBlock.LIT, false));
			level.setBlockAndUpdate(pos, state.setValue(BloomeryBlock.OPEN, true));
		} else if (block instanceof BlastFurnaceBlock) {
			// TODO: doesn't seem to work
			var be = level.getBlockEntity(pos, TFCBlockEntities.BLAST_FURNACE.get());
			if (be.isPresent()) {
				be.get().extinguish(state);
			}
		} else if (block instanceof OvenBottomBlock) {
			var be = level.getBlockEntity(pos, FLBlockEntities.OVEN_BOTTOM.get());
			if (be.isPresent()) {
				be.get().extinguish(state);
			}
		} else if (block instanceof CharcoalForgeBlock) {
			level.setBlockAndUpdate(pos, TFCBlocks.CHARCOAL_PILE.get().defaultBlockState().setValue(CharcoalPileBlock.LAYERS, 7));
		} else if (block instanceof JackOLanternBlock jackOLantern) {
			jackOLantern.extinguish(level, pos, state);
		} else if (block instanceof LampBlock) {
			var be = level.getBlockEntity(pos, TFCBlockEntities.LAMP.get());
			if (be.isPresent()) {
				LampFuel fuel = be.get().getFuel();
				if (fuel != null && fuel.getBurnRate() < 0) {
					return;
				}
			}
			level.setBlockAndUpdate(pos, state.setValue(LampBlock.LIT, false));
		}
	}
}
