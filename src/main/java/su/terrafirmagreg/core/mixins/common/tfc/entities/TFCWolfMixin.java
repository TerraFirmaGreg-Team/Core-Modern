package su.terrafirmagreg.core.mixins.common.tfc.entities;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.dries007.tfc.common.entities.predator.Predator;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfInterface;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

@Mixin(value = PackPredator.class)
public abstract class TFCWolfMixin extends Predator implements TFCWolfInterface {
    @Shadow(remap = false)
    public abstract void setRespect(int amount);

    @Shadow(remap = false)
    @Final
    private boolean tamable;
    @Unique
    private static final EntityDataAccessor<Integer> DATA_VARIANT;

    static {
        DATA_VARIANT = SynchedEntityData.defineId(TFCWolfMixin.class, EntityDataSerializers.INT);
    }

    public TFCWolfMixin(EntityType<? extends Predator> type, Level level, boolean diurnal, TFCSounds.EntitySound sounds, boolean tamable) {
        super(type, level, diurnal, sounds);
    }

    @Unique
    public TFCWolfVariant tfg$getVariant() {
        if (!this.entityData.hasItem(DATA_VARIANT)) {
            return TFCWolfVariant.DEFAULT;
        }
        return TFCWolfVariant.byId((Integer) this.entityData.get(DATA_VARIANT));
    }

    @Unique
    public void tfg$setVariant(TFCWolfVariant id) {
        this.entityData.set(DATA_VARIANT, id.id);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSynchedData(CallbackInfo ci) {
        this.entityData.define(DATA_VARIANT, TFCWolfVariant.DEFAULT.id);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void tfg$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("TFCWolfVariant", this.tfg$getVariant().id);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void tfg$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        this.tfg$setVariant(TFCWolfVariant.byId(tag.getInt("TFCWolfVariant")));
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    public void tfg$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, SpawnGroupData data, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (this.tamable) {
            BlockPos pos = this.blockPosition();
            ChunkData chunkData = ChunkDataProvider.get(level.getLevel()).get(level.getChunk(pos));
            ForestType forestType = chunkData.getForestType();
            boolean forestSpawn = switch (forestType) {
                case NONE, SPARSE -> false;
                default -> true;
            };
            float temperature = chunkData.getAverageTemp(pos);
            float rainfall = chunkData.getRainfall(pos);
            KoppenClimateClassification climate = KoppenClimateClassification.classify(temperature, rainfall);

            TFCWolfVariant variant = switch (climate) {
                case HUMID_SUBTROPICAL, TROPICAL_RAINFOREST -> TFCWolfVariant.RUSTY;
                case HUMID_SUBARCTIC, TUNDRA -> forestSpawn ? TFCWolfVariant.BLACK : TFCWolfVariant.CHESTNUT;
                case SUBTROPICAL, HOT_DESERT, TROPICAL_SAVANNA -> forestSpawn ? TFCWolfVariant.SPOTTED : TFCWolfVariant.STRIPED;
                case TEMPERATE -> forestSpawn ? TFCWolfVariant.WOODS : TFCWolfVariant.DEFAULT;
                case SUBARCTIC, ARCTIC -> forestSpawn ? TFCWolfVariant.ASHEN : TFCWolfVariant.SNOWY;
                default -> TFCWolfVariant.DEFAULT; // HUMID_OCEANIC, COLD_DESERT
            };

            this.tfg$setVariant(variant);
        } else {
            this.tfg$setVariant(TFCWolfVariant.DEFAULT);
        }
    }

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/common/entities/livestock/pet/Dog;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;", shift = At.Shift.AFTER))
    private void tfg$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "dog") Dog dog) {
        if (dog instanceof TFCWolfInterface access) {
            access.tfg$setVariant(this.tfg$getVariant());
        }
    }
}
