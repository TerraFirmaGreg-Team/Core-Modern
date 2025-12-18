package su.terrafirmagreg.core.mixins.common.gtceu.medical;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;

import su.terrafirmagreg.core.common.data.TFGEffects;

@Mixin(value = MedicalConditionTracker.class, remap = false)
public abstract class MedicalConditionTrackerMixin {

    @Shadow
    public abstract Player getPlayer();

    @Shadow
    public abstract Object2FloatMap<MedicalCondition> getMedicalConditions();

    @Shadow
    @Final
    private Set<MedicalCondition> flaggedForRemoval;
    @Unique
    private static final Map<MedicalCondition, MobEffect> tfg$warningEffects = Map.ofEntries(
            Map.entry(GTMedicalConditions.CHEMICAL_BURNS, TFGEffects.CHEMICAL_BURNS_WARNING.get()),
            Map.entry(GTMedicalConditions.POISON, TFGEffects.POISON_WARNING.get()),
            Map.entry(GTMedicalConditions.WEAK_POISON, TFGEffects.WEAK_POISON_WARNING.get()),
            Map.entry(GTMedicalConditions.IRRITANT, TFGEffects.IRRITANT_WARNING.get()),
            Map.entry(GTMedicalConditions.NAUSEA, TFGEffects.NAUSEA_WARNING.get()),
            Map.entry(GTMedicalConditions.CARCINOGEN, TFGEffects.CARCINOGEN_WARNING.get()),
            Map.entry(GTMedicalConditions.ASBESTOSIS, TFGEffects.ASBESTOSIS_WARNING.get()),
            Map.entry(GTMedicalConditions.ARSENICOSIS, TFGEffects.ARSENICOSIS_WARNING.get()),
            Map.entry(GTMedicalConditions.SILICOSIS, TFGEffects.SILICOSIS_WARNING.get()),
            Map.entry(GTMedicalConditions.BERYLLIOSIS, TFGEffects.BERYLLIOSIS_WARNING.get()),
            Map.entry(GTMedicalConditions.METHANOL_POISONING, TFGEffects.METHANOL_POISONING_WARNING.get()),
            Map.entry(GTMedicalConditions.CARBON_MONOXIDE_POISONING, TFGEffects.CARBON_MONOXIDE_POISONING_WARNING.get()));

    @Inject(method = "updateActiveSymptoms", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;getFloat(Ljava/lang/Object;)F"), remap = false)
    private void tfg$updateActiveSymptoms(CallbackInfo ci, @Local MedicalCondition condition) {
        Player player = getPlayer();
        if (player.level().getGameTime() % 20 == 0) {
            if (flaggedForRemoval.contains(condition)) {
                player.removeEffect(tfg$warningEffects.get(condition));
            } else {
                MobEffect effect = tfg$warningEffects.get(condition);
                int durationInSeconds = (int) getMedicalConditions().getFloat(condition);

                player.removeEffect(effect);
                player.addEffect(new MobEffectInstance(effect, durationInSeconds * 20, 0, false, false));
            }
        }
    }

    @Inject(method = "removeMedicalCondition", at = @At("TAIL"), remap = false)
    private void tfg$removeMedicalCondition(MedicalCondition condition, CallbackInfo ci) {
        getPlayer().removeEffect(tfg$warningEffects.get(condition));
    }
}
