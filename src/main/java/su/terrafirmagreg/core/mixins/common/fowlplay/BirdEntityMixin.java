package su.terrafirmagreg.core.mixins.common.fowlplay;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.entities.aquatic.AquaticMob;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import aqario.fowlplay.common.entity.bird.BirdEntity;
import aqario.fowlplay.common.entity.bird.penguin.PenguinEntity;

@Mixin(value = BirdEntity.class, remap = false)
public class BirdEntityMixin extends Animal implements AquaticMob {

    protected BirdEntityMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "shouldBeAmbient", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$shouldBeAmbient(CallbackInfoReturnable<Boolean> cir) {
        if ((BirdEntity) (Object) this instanceof PenguinEntity) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean canSpawnIn(@NotNull Fluid fluid) {
        return fluid.isSame(TFCFluids.SALT_WATER.getSource()) || fluid.isSame(Fluids.WATER.getSource());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }
}
