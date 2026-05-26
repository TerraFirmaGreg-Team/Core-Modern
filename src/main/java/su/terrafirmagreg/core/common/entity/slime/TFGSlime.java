package su.terrafirmagreg.core.common.entity.slime;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.pet.TFCCat;
import net.dries007.tfc.common.entities.livestock.pet.TamableMammal;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import su.terrafirmagreg.core.common.data.TFGEntityDataSerializers;
import su.terrafirmagreg.core.common.data.TFGSounds;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

public class TFGSlime extends TamableMammal {
    public static final EntityDataAccessor<SlimeVariant> DATA_VARIANT;

    public TFGSlime(EntityType<? extends TFCAnimal> animal, Level level) {
        super(animal, level, TFGSounds.FOX, TFCConfig.SERVER.catConfig);
    }

    public void setSize(int size, boolean resetHealth) {
        int i = Mth.clamp(size, 1, 127);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) (i * i));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) (0.2F + 0.1F * (float) i));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) i);
        if (resetHealth) {
            this.setHealth(this.getMaxHealth());
        }

        this.xpReward = i;
    }

    public int getSize() {
        return this.getGeneticSize();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, SlimeVariant.GREEN);
    }

    public SlimeVariant getVariant() {
        if (!this.entityData.hasItem(DATA_VARIANT)) {
            return SlimeVariant.GREEN;
        }
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(SlimeVariant type) {
        this.entityData.set(DATA_VARIANT, type);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("variant", this.getVariant().id());
    }
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSize(this.getGeneticSize(), false);
        this.setVariant(SlimeVariant.GREEN); // TODO
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
        this.setVariant(SlimeVariant.GREEN);
    }

    @Override
    public TagKey<Item> getFoodTag() {
        return TFCTags.Items.FOODS;
    }

    @Override
    public boolean canAttack(LivingEntity entity) {
        return super.canAttack(entity) && (Helpers.isEntity(entity, TFCTags.Entities.HUNTED_BY_CATS) || entity instanceof Monster);
    }

    @Override
    public void receiveCommand(ServerPlayer player, Command command) {
        if (getOwner() != null && getOwner().equals(player)) {
            playSound(SoundEvents.SLIME_SQUISH, getSoundVolume(), getVoicePitch());
        }
        super.receiveCommand(player, command);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return size.height * 0.5F;
    }

    @Override
    public boolean isReadyToMate() {
        return false;
    }

    static {
        DATA_VARIANT = SynchedEntityData.defineId(TFGSlime.class, TFGEntityDataSerializers.SLIME_VARIANT.get());
    }
}
