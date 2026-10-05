package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import net.minecraft.world.entity.schedule.Activity;

@Mixin(value = PiglinBruteAi.class)
public class PiglinBruteAiMixin {

    @Inject(method = "initCoreActivity", at = @At("TAIL"))
    private static void tfg$initCoreActivity(PiglinBrute piglinBrute, Brain<PiglinBrute> brain, CallbackInfo ci) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F)));
    }
}
