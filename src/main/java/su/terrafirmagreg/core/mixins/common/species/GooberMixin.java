package su.terrafirmagreg.core.mixins.common.species;

import com.ninni.species.server.entity.mob.update_2.Goober;
import earth.terrarium.adastra.common.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Goober.class, remap = false)
public class GooberMixin {

	@Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$canSpawn(EntityType<Goober> entity, ServerLevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(
			world.getBlockState(pos.below()).is(ModBlockTags.MARS_STONE_REPLACEABLES)
				&& world.getBlockState(pos.below()).isValidSpawn(world, pos, entity));
	}
}
