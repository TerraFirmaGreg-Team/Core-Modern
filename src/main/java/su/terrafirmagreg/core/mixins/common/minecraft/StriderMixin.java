package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(value = Strider.class)
public abstract class StriderMixin extends Animal {

    protected StriderMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // Acid rain still counts as rain apparently

    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    public void tfg$isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    // Stop zombified piglin jockeys

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), cancellable = true)
    public void tfg$finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        cir.setReturnValue(super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag));
    }
}
