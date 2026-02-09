package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import su.terrafirmagreg.core.common.data.entities.axolotl.AxolotlData;

@Mixin(value = Axolotl.class, remap = true)
public abstract class AxolotlMixin extends Animal {

    protected AxolotlMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    public void tfg$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        tfg$setIsMale(random.nextBoolean());
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(AxolotlData.DATA_IS_MALE, true);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void tfg$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tfg$setIsMale(tag.getBoolean("male"));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void tfg$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("male", tfg$isMale());
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(AxolotlData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(AxolotlData.DATA_IS_MALE);
    }
}
