package su.terrafirmagreg.core.common.entity.camels;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Dynamic;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.ai.TFCGroundPathNavigation;
import net.dries007.tfc.common.entities.livestock.CommonAnimalData;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.animals.AnimalConfig;
import net.dries007.tfc.config.animals.MammalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public class TFGDrodemaryCamel extends TFGAbstractCamel implements HorseProperties {
    private static final EntityDataAccessor<Boolean> GENDER;
    private static final EntityDataAccessor<Long> BIRTHDAY;
    private static final EntityDataAccessor<Float> FAMILIARITY;
    private static final EntityDataAccessor<Integer> USES;
    private static final EntityDataAccessor<Boolean> FERTILIZED;
    private static final EntityDataAccessor<Long> OLD_DAY;
    private static final EntityDataAccessor<Integer> GENETIC_SIZE;
    private static final EntityDataAccessor<Long> LAST_FED;
    private static final CommonAnimalData ANIMAL_DATA;
    private static final EntityDataAccessor<Long> PREGNANT_TIME;
    private long lastFDecay;
    private long matingTime;
    @Nullable
    private CompoundTag genes;
    private TFCAnimalProperties.Age lastAge;
    private final AnimalConfig config;
    private final MammalConfig mammalConfig;

    public TFGDrodemaryCamel(EntityType<? extends Camel> type, Level level) {
        super(type, level);
        this.config = TFCConfig.SERVER.catConfig.inner();
        this.mammalConfig = TFCConfig.SERVER.catConfig;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1F)
                .add(Attributes.JUMP_STRENGTH, 0.42F);
        // TODO: 1.21.1 thing? .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return TFGCamelAi.makeBrain(TFGCamelAi.brainProvider().makeBrain(dynamic));
    }

    @Override
    public void createGenes(CompoundTag tag, TFCAnimalProperties maleProperties) {
        super.createGenes(tag, maleProperties);
    }

    @Override
    public void applyGenes(CompoundTag tag, MammalProperties babyProperties) {
        super.applyGenes(tag, babyProperties);
    }

    @Override
    public void setInLove(@Nullable Player player) {
    } // nobody could love a camel

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal.getClass() != this.getClass())
            return false;
        TFGDrodemaryCamel other = (TFGDrodemaryCamel) otherAnimal;
        return this.getGender() != other.getGender()
                && this.isReadyToMate() && other.isReadyToMate()
                && checkExtraBreedConditions(other);
    }

    @Override
    public boolean checkExtraBreedConditions(TFCAnimalProperties otherAnimal) {
        if (otherAnimal instanceof TFGDrodemaryCamel otherCamel) {
            return vanillaParentingCheck(this) && vanillaParentingCheck(otherCamel);
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = super.mobInteract(player, hand);
        if (result == InteractionResult.PASS) {
            ItemStack stack = player.getItemInHand(hand);
            if (!this.isBaby()) {
                if (this.isTamed() && player.isSecondaryUseActive()) {
                    this.openCustomInventoryScreen(player);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }

                if (this.isVehicle()) {
                    return InteractionResult.PASS;
                }
            }

            if (!stack.isEmpty()) {
                InteractionResult res = stack.interactLivingEntity(player, this, hand);
                if (res.consumesAction()) {
                    return res;
                }

                if (!this.isTamed()) {
                    this.makeMad();
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }

                final boolean canBeSaddled = !this.isBaby() && !this.isSaddled() && stack.is(Items.SADDLE);
                if (/* TODO: this.isBodyArmorItem(stack) || */ canBeSaddled) {
                    this.openCustomInventoryScreen(player);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }

            if (this.isBaby()) {
                return InteractionResult.PASS;
            } else {
                if (isTamed() && getOwnerUUID() == null) {
                    tameWithName(player);
                }
                if (canAddPassenger(player)) {
                    this.doPlayerRide(player);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return result;
    }

    @Override
    public boolean isTamed() {
        return getFamiliarity() > TAMED_FAMILIARITY;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, CompoundTag tag) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
        if (spawnType != MobSpawnType.BREEDING) {
            initCommonAnimalData(level, difficulty, spawnType);
        }
        setPregnantTime(-1L);
        return spawnData;
    }

    public static boolean spawnRules(EntityType<? extends TFGDrodemaryCamel> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand) {
        return level.getBlockState(pos).isAir();
    }

    @Override
    public MammalConfig getMammalConfig() {
        return mammalConfig;
    }

    @Override
    public long getPregnantTime() {
        return entityData.get(PREGNANT_TIME);
    }

    @Override
    public void setPregnantTime(long day) {
        entityData.set(PREGNANT_TIME, day);
    }

    @Override
    public @Nullable CompoundTag getGenes() {
        return genes;
    }

    @Override
    public void setGenes(@Nullable CompoundTag tag) {
        genes = tag;
    }

    @Override
    public CommonAnimalData animalData() {
        return ANIMAL_DATA;
    }

    @Override
    public AnimalConfig animalConfig() {
        return config;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.registerCommonData();
        this.entityData.define(PREGNANT_TIME, -1L);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        saveCommonAnimalData(nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        readCommonAnimalData(nbt);
    }

    @Override
    public boolean isBaby() {
        return getAgeType() == Age.CHILD;
    }

    @Override
    public void setAge(int age) {
        super.setAge(0);
    }

    @Override
    public int getAge() {
        return isBaby() ? -24000 : 0;
    }

    @Nullable
    @Override
    public TFGDrodemaryCamel getBreedOffspring(ServerLevel level, AgeableMob other) {
        final AgeableMob mob = super.getBreedOffspring(level, other);
        return mob instanceof TFGDrodemaryCamel camel ? camel : null;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (BIRTHDAY.equals(data)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void customServerAiStep() {
        // Don't want to call super.customServerAiStep() here because of CamelAi.updateActivity(this)
        ((Brain<TFGDrodemaryCamel>) getBrain()).tick((ServerLevel) level(), this);
        TFGCamelAi.updateActivity(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().getGameTime() % 20 == 0) {
            tickAnimalData();
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource src) {
        return src.is(DamageTypes.CACTUS) || super.isInvulnerableTo(src);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).is(TFCTags.Blocks.BUSH_PLANTABLE_ON) ? 10.0F : level.getPathfindingCostFromLightLevels(pos);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new TFCGroundPathNavigation(this, level);
    }

    @Override
    public boolean isInWall() {
        return !level().isClientSide && super.isInWall();
    }

    @Override
    protected void pushEntities() {
        if (!level().isClientSide)
            super.pushEntities();
    }

    public long getLastFamiliarityDecay() {
        return this.lastFDecay;
    }

    public void setLastFamiliarityDecay(long days) {
        this.lastFDecay = days;
    }

    public void setMated(long ticks) {
        this.matingTime = ticks;
    }

    public long getMated() {
        return this.matingTime;
    }

    public TFCAnimalProperties.Age getLastAge() {
        return this.lastAge;
    }

    public void setLastAge(TFCAnimalProperties.Age lastAge) {
        this.lastAge = lastAge;
    }

    static {
        GENDER = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityDataSerializers.BOOLEAN);
        BIRTHDAY = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityHelpers.LONG_SERIALIZER);
        FAMILIARITY = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityDataSerializers.FLOAT);
        USES = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityDataSerializers.INT);
        FERTILIZED = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityDataSerializers.BOOLEAN);
        OLD_DAY = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityHelpers.LONG_SERIALIZER);
        GENETIC_SIZE = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityDataSerializers.INT);
        LAST_FED = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityHelpers.LONG_SERIALIZER);
        ANIMAL_DATA = new CommonAnimalData(GENDER, BIRTHDAY, FAMILIARITY, USES, FERTILIZED, OLD_DAY, GENETIC_SIZE, LAST_FED);
        PREGNANT_TIME = SynchedEntityData.defineId(TFGDrodemaryCamel.class, EntityDataSerializers.LONG);
    }
}
