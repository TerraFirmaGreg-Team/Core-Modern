package su.terrafirmagreg.core.mixins.common.minecraft;

import com.ninni.species.registry.SpeciesEntities;
import earth.terrarium.adastra.common.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Monster.class, remap = true)
public class MonsterMixin {

	// Some mobs use this instead of their own canSpawn method, so we gotta override their spawn behaviour here

	@Inject(method = "checkMonsterSpawnRules", at = @At("HEAD"), remap = true, cancellable = true)
	private static void checkMonsterSpawnRules(EntityType<? extends Monster> pType, ServerLevelAccessor levelAccessor, MobSpawnType pSpawnType, BlockPos blockPos, RandomSource pRandom, CallbackInfoReturnable<Boolean> cir) {

		if (pType == SpeciesEntities.QUAKE.get() || pType == ModEntityTypes.MARTIAN_RAPTOR.get())
		{
			// Make these only spawn underground
			cir.setReturnValue(levelAccessor.getBrightness(LightLayer.BLOCK, blockPos) == 0
				&& levelAccessor.getBrightness(LightLayer.SKY, blockPos) == 0
				&& levelAccessor.getBlockState(blockPos.below()).isValidSpawn(levelAccessor, blockPos, pType));
		}
	}
}
