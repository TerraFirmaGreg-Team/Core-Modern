/*
 * This file includes code from Wan's Ancient Beasts (https://www.curseforge.com/minecraft/mc-mods/wans-ancient-beasts)
 * Copyright (c) 2024 WanMine
 * All Rights Reserved, though permission to use mixins was given
 */

package su.terrafirmagreg.core.mixins.common.wab;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.wanmine.wab.entity.Toxlacanth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.common.data.TFGBlocks;

@Mixin(value = Toxlacanth.class, remap = false)
public class ToxlacanthMixin {

	@Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$canSpawn(EntityType<? extends Toxlacanth> entity, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(level.getBlockState(pos).is(TFGBlocks.MARS_WATER.get()));
	}
}
