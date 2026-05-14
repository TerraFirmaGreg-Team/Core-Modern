package su.terrafirmagreg.core.common.entity.fox;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.pet.TamableMammal;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import su.terrafirmagreg.core.common.data.TFGSounds;

public class TFGFox extends TamableMammal {
    private static final EntityDataAccessor<Integer> DATA_VARIANT;

    float crouchAmount;
    float previousCrouchAmount;

    static {
        DATA_VARIANT = SynchedEntityData.defineId(TFGFox.class, EntityDataSerializers.INT);
    }

    public Fox.Type getVariant() {
        return Fox.Type.byId((Integer) this.entityData.get(DATA_VARIANT));
    }

    public void setVariant(Fox.Type id) {
        this.entityData.set(DATA_VARIANT, id.getId());
    }

    public TFGFox(EntityType<? extends TFCAnimal> animal, Level level) {
        super(animal, level, TFGSounds.FOX, TFCConfig.SERVER.catConfig);
    }

    @Override
    public boolean willListenTo(Command command, boolean isClientSide) {
        if (!isClientSide && command == Command.SIT && getRandom().nextFloat() < 0.25f) {
            return false;
        } else {
            return super.willListenTo(command, isClientSide);
        }
    }

    @Override
    public void initCommonAnimalData(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason) {
        super.initCommonAnimalData(level, difficulty, reason);
    }

    @Override
    public TagKey<Item> getFoodTag() {
        return TFCTags.Items.FOODS;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
    }

    @Override
    public boolean canAttack(LivingEntity entity) {
        return super.canAttack(entity) && (Helpers.isEntity(entity, TFCTags.Entities.HUNTED_BY_CATS) || entity instanceof Monster);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FoxType", this.getVariant().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Fox.Type.byId(tag.getInt("FoxType")));
    }

    @Override
    public void receiveCommand(ServerPlayer player, Command command) {
        if (getOwner() != null && getOwner().equals(player)) {
            playSound(SoundEvents.FOX_AMBIENT, getSoundVolume(), getVoicePitch());
        }
        super.receiveCommand(player, command);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return size.height * 0.5F;
    }

    public float getCrouchAmount(float partialTick) {
        return Mth.lerp(partialTick, this.previousCrouchAmount, this.crouchAmount);
    }

    @Override
    public boolean isReadyToMate() {
        return false;
    }

    public void tick() {
        super.tick();

        this.previousCrouchAmount = this.crouchAmount;
        if (this.isCrouching()) {
            this.crouchAmount += 0.2F;
            if (this.crouchAmount > 3.0F) {
                this.crouchAmount = 3.0F;
            }
        } else {
            this.crouchAmount = 0.0F;
        }
    }
}
