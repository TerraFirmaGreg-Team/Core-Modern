package su.terrafirmagreg.core.mixins.common.ad_astra;

import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.OvenBottomBlock;
import earth.terrarium.adastra.common.systems.EnvironmentEffects;
import earth.terrarium.adastra.common.tags.ModBlockTags;
import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.*;
import net.dries007.tfc.common.blocks.devices.*;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.IGrassBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.LampFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

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

	// Bloomery and Blast furnace can't be extinguished after being started (outside of like, breaking them),
	// so they get their own mixins
	@Unique
	private static void tfg$tickBlockBugWorkaround(ServerLevel level, BlockPos pos, BlockState state)
	{
		if (state.is(TFGTags.Blocks.DoNotDestroyInSpace))
			return;

		Block block = state.getBlock();
		if (hasOxygenOnAnySide(level, pos))
			return;

		if (state.is(ModBlockTags.DESTROYED_IN_SPACE)){
			level.destroyBlock(pos, true);
		}
		if (block instanceof TFCTorchBlock) {
			Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
			level.setBlockAndUpdate(pos, TFCBlocks.DEAD_TORCH.get().defaultBlockState());
		} else if (block instanceof TFCWallTorchBlock) {
			Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
			level.setBlockAndUpdate(pos, Helpers.copyProperty(TFCBlocks.DEAD_WALL_TORCH.get().defaultBlockState(), state, WallTorchBlock.FACING));
		} else if (block instanceof IGrassBlock grassBlock) {
			level.setBlockAndUpdate(pos, grassBlock.getDirt());
		} else if (block instanceof FarmlandBlock) {
			FarmlandBlock.turnToDirt(state, level, pos);
		} else if (block instanceof TFCCandleBlock) {
			if (state.getValue(TFCCandleBlock.LIT)) {
				Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
				level.setBlockAndUpdate(pos, state.setValue(TFCCandleBlock.LIT, false));
			}
		} else if (block instanceof FirepitBlock) {
			if (state.getValue(FirepitBlock.LIT)) {
				var be = level.getBlockEntity(pos);
				if (be instanceof AbstractFirepitBlockEntity<?> firepit) {
					firepit.extinguish(state);
				}
			}
		} else if (block instanceof TFCCandleCakeBlock) {
			if (state.getValue(TFCCandleCakeBlock.LIT)) {
				Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
				level.setBlockAndUpdate(pos, state.setValue(TFCCandleCakeBlock.LIT, false));
			}
		} else if (block instanceof PitKilnBlock) {
			Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
			level.destroyBlock(pos, false);
		} else if (block instanceof OvenBottomBlock) {
			if (state.getValue(OvenBottomBlock.LIT)) {
				var be = level.getBlockEntity(pos, FLBlockEntities.OVEN_BOTTOM.get());
				if (be.isPresent()) {
					be.get().extinguish(state);
				}
			}
		} else if (block instanceof CharcoalForgeBlock) {
			Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
			level.setBlockAndUpdate(pos, TFCBlocks.CHARCOAL_PILE.get().defaultBlockState().setValue(CharcoalPileBlock.LAYERS, 7));
		} else if (block instanceof JackOLanternBlock jackOLantern) {
			jackOLantern.extinguish(level, pos, state);
		} else if (block instanceof LampBlock) {
			if (state.getValue(LampBlock.LIT)) {
				var be = level.getBlockEntity(pos, TFCBlockEntities.LAMP.get());
				if (be.isPresent()) {
					LampFuel fuel = be.get().getFuel();
					if (fuel != null && fuel.getBurnRate() < 0) {
						return;
					}
				}
				Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
				level.setBlockAndUpdate(pos, state.setValue(LampBlock.LIT, false));
			}
		}
	}
}
