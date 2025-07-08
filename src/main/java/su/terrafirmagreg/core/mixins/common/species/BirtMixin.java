package su.terrafirmagreg.core.mixins.common.species;

import com.ninni.species.server.entity.mob.update_1.Birt;
import com.ninni.species.registry.SpeciesTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Birt.class, remap = false)
public abstract class BirtMixin {

	/**
	 * @author Pyritie
	 * @reason This always returns false for some reason by default?
	 * But now it just has the same spawn conditions as limpets
	 */
	@Inject(method = "canSpawn", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$canSpawn(EntityType<? extends Animal> type, LevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(
			world.getBlockState(pos.below()).is(SpeciesTags.LIMPET_SPAWNABLE_ON)
			&& world.getBlockState(pos.below()).isValidSpawn(world, pos, type));
	}
}
