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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelAccessor;
import net.wanmine.wab.event.setup.CommonSetup;
import net.wanmine.wab.init.world.WabEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.common.data.TFGBlocks;

@Mixin(value = CommonSetup.class, remap = false)
public class CommonSetupMixin {

	// Surfer doesn't have its own canSpawn method, so override its spawn behaviour here

	@Inject(method = "checkAncientAnimalSpawnRules", at = @At("HEAD"), cancellable = true, remap = false)
	private static void tfg$checkAncientAnimalSpawnRules(EntityType<? extends Animal> pAnimal, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom, CallbackInfoReturnable<Boolean> cir)
	{
		if (pAnimal == WabEntities.SURFER.get())
		{
			cir.setReturnValue(pLevel.getBlockState(pPos).is(TFGBlocks.MARS_WATER.get()));
		}
	}
}
