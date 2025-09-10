package su.terrafirmagreg.core.mixins.common.immersive_aircraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;

import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirplaneEntity;
import immersive_aircraft.entity.BambooHopperEntity;

import su.terrafirmagreg.core.common.data.TFGFluids;

@Mixin(value = BambooHopperEntity.class)
public abstract class BambooHopperEntityMixin extends AirplaneEntity {
    @Unique
    private static final FluidType[] FLOATABLE_FLUIDS = {
            ForgeMod.WATER_TYPE.get(),
            TFCFluids.SALT_WATER.type().get(),
            TFCFluids.SPRING_WATER.type().get(),
            TFCFluids.RIVER_WATER.get().getFluidType(),
            TFGFluids.MARS_WATER.type().get()
    };

    private BambooHopperEntityMixin(EntityType<? extends AircraftEntity> entityType, Level world,
            boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Inject(method = "getGravity", at = @At("HEAD"), remap = false, cancellable = true)
    void tfg$getGravity(CallbackInfoReturnable<Float> cir) {
        float water = 0;

        for (FluidType fluid : FLOATABLE_FLUIDS) {
            water += (float) this.getFluidTypeHeight(fluid);
        }

        cir.setReturnValue(water > 0.0F ? 0.04F * water : (1.0F - this.getEnginePower()) * super.getGravity());
    }

}
