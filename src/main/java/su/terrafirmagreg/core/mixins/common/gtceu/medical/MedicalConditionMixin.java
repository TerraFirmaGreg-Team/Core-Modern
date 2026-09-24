package su.terrafirmagreg.core.mixins.common.gtceu.medical;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.resources.ResourceLocation;

@Mixin(value = MedicalCondition.class, remap = false)
public class MedicalConditionMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), remap = false, argsOnly = true)
    private static MedicalCondition.IdleProgressionType tfg$makeWeakPoisonHeal1(MedicalCondition.IdleProgressionType progressionType, @Local(argsOnly = true) ResourceLocation id) {
        if (id.getPath().equals("weak_poison")) {
            return MedicalCondition.IdleProgressionType.HEAL;
        } else {
            return progressionType;
        }
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), remap = false, argsOnly = true)
    private static float tfg$makeWeakPoisonHeal2(float idleProgressionRate, @Local(argsOnly = true) ResourceLocation id) {
        if (id.getPath().equals("weak_poison")) {
            return 2;
        } else {
            return idleProgressionRate;
        }
    }
}
