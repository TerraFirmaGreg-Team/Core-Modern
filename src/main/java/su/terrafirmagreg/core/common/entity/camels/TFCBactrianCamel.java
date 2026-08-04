package su.terrafirmagreg.core.common.entity.camels;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Dynamic;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.ai.TFCGroundPathNavigation;
import net.dries007.tfc.common.entities.livestock.CommonAnimalData;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.animals.AnimalConfig;
import net.dries007.tfc.config.animals.MammalConfig;
import net.dries007.tfc.config.animals.ProducingMammalConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.MinecraftForge;

import su.terrafirmagreg.core.mixins.common.minecraft.entities.CamelAccessor;

public class TFCBactrianCamel extends TFCAbstractCamel implements HorseProperties, IForgeShearable {
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
    private static final EntityDataAccessor<Long> DATA_PRODUCED;
    private long lastFDecay;
    private long matingTime;
    @Nullable
    private CompoundTag genes;
    private TFCAnimalProperties.Age lastAge;
    private final AnimalConfig config;
    private final MammalConfig mammalConfig;
    private final ProducingMammalConfig producingMammalConfig;

    static double familiarityCap = 0.35;
    static int adulthoodDays = 80;
    static int uses = 80;
    static boolean eatsRottenFood = false;
    static int produceTicks = 168000;
    static double produceFamiliarity = 0.15;
    static int childCount = 1;
    static long gestationDays = 24;

    public TFCBactrianCamel(EntityType<? extends Camel> type, Level level) {
        super(type, level);
        this.config = TFCConfig.SERVER.sheepConfig.inner().inner();
        this.mammalConfig = TFCConfig.SERVER.sheepConfig.inner();
        this.producingMammalConfig = TFCConfig.SERVER.sheepConfig;
    }

    // region Config Bypass
    @Override
    public float getAdultFamiliarityCap() {
        return (float) familiarityCap;
    }

    @Override
    public int getDaysToAdulthood() {
        return adulthoodDays;
    }

    @Override
    public int getUsesToElderly() {
        return uses;
    }

    @Override
    public boolean eatsRottenFood() {
        return eatsRottenFood;
    }

    @Override
    public boolean isReadyForAnimalProduct() {
        return getFamiliarity() > produceFamiliarity && hasProduct();
    }

    @Override
    public long getProductsCooldown() {
        return Math.max(0, produceTicks + getProducedTick() - Calendars.get(level()).getTicks());
    }

    @Override
    public int getChildCount() {
        return childCount;
    }

    @Override
    public long getGestationDays() {
        return gestationDays;
    }
    // endregion

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return TFCCamelAi.makeBrain(TFCCamelAi.brainProvider().makeBrain(dynamic));
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
    public void setProductsCooldown() {
        setProducedTick(Calendars.get(level()).getTicks());
    }

    public long getProducedTick() {
        return entityData.get(DATA_PRODUCED);
    }

    public void setProducedTick(long producedTick) {
        entityData.set(DATA_PRODUCED, producedTick);
    }

    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return isReadyForAnimalProduct();
    }

    @Override
    public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
        setProductsCooldown();
        playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);

        AnimalProductEvent event = new AnimalProductEvent(level, pos, player, this, getWoolItem(), item, 1);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            addUses(event.getUses());
        }
        return List.of(event.getProduct());
    }

    @Override
    public boolean hasProduct() {
        return getProducedTick() <= 0 || getProductsCooldown() <= 0;
    }

    public ItemStack getWoolItem() {
        final int amount = getFamiliarity() > 0.99f ? 2 : 1;
        return new ItemStack(TFCItems.WOOL.get(), amount);
    }

    @Override
    public MutableComponent getProductReadyName() {
        return Component.translatable("tfc.jade.product.wool");
    }

    @Override
    public void setInLove(@Nullable Player player) {
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal.getClass() != this.getClass())
            return false;
        TFCBactrianCamel other = (TFCBactrianCamel) otherAnimal;
        return this.getGender() != other.getGender()
                && this.isReadyToMate() && other.isReadyToMate()
                && checkExtraBreedConditions(other);
    }

    @Override
    public boolean checkExtraBreedConditions(TFCAnimalProperties otherAnimal) {
        if (otherAnimal instanceof TFCBactrianCamel otherCamel) {
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
                if (this.isArmor(stack) || canBeSaddled) {
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
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() <= 1;
    }

    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        int i = this.getPassengers().indexOf(passenger);
        if (i >= 0) {
            boolean flag = i == 0;
            float f = 0F;
            float f1 = (float) (this.isRemoved() ? (double) 0.01F : ((CamelAccessor) this).invoke$getBodyAnchorAnimationYOffset(flag, 0.0F) + passenger.getMyRidingOffset());
            if (this.getPassengers().size() > 1) {
                if (!flag) {
                    f = -0.7F;
                }

                if (passenger instanceof Animal) {
                    f += 0.2F;
                }
            }

            Vec3 vec3 = (new Vec3((double) 0.0F, (double) 0.0F, (double) f)).yRot(-this.yBodyRot * ((float) Math.PI / 180F));
            callback.accept(passenger, this.getX() + vec3.x, this.getY() + (double) f1, this.getZ() + vec3.z);
            this.clampRotation(passenger);
        }

    }

    private void clampRotation(Entity entity) {
        entity.setYBodyRot(this.getYRot());
        float f = entity.getYRot();
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        float f2 = Mth.clamp(f1, -160.0F, 160.0F);
        entity.yRotO += f2 - f1;
        float f3 = f + f2 - f1;
        entity.setYRot(f3);
        entity.setYHeadRot(f3);
    }

    @Override
    public boolean isTamed() {
        return getFamiliarity() > produceFamiliarity;
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

    public static boolean spawnRules(EntityType<? extends TFCBactrianCamel> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand) {
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
        this.entityData.define(DATA_PRODUCED, 0L);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        saveCommonAnimalData(nbt);
        nbt.putLong("produced", getProducedTick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        readCommonAnimalData(nbt);
        setProducedTick(nbt.getLong("produced"));
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

    @Override
    public @Nullable TFCBactrianCamel getBreedOffspring(ServerLevel level, AgeableMob other) {
        final AgeableMob mob = super.getBreedOffspring(level, other);
        return mob instanceof TFCBactrianCamel camel ? camel : null;
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
        ((Brain<TFCBactrianCamel>) getBrain()).tick((ServerLevel) level(), this);
        TFCCamelAi.updateActivity(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().getGameTime() % 20 == 0) {
            tickAnimalData();
        }
    }

    // A sprinting Bactrian Camel is a bit faster than a sprinting player (0.13 vs 0.15)
    @Override
    protected float getRiddenSpeed(Player player) {
        float sprintSpeedBonus = 0.0875F;
        float f = player.isSprinting() && this.getJumpCooldown() == 0 ? sprintSpeedBonus : 0.0F;
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
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
        GENDER = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.BOOLEAN);
        BIRTHDAY = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityHelpers.LONG_SERIALIZER);
        FAMILIARITY = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.FLOAT);
        USES = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.INT);
        FERTILIZED = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.BOOLEAN);
        OLD_DAY = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityHelpers.LONG_SERIALIZER);
        GENETIC_SIZE = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.INT);
        LAST_FED = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityHelpers.LONG_SERIALIZER);
        ANIMAL_DATA = new CommonAnimalData(GENDER, BIRTHDAY, FAMILIARITY, USES, FERTILIZED, OLD_DAY, GENETIC_SIZE, LAST_FED);
        PREGNANT_TIME = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.LONG);
        DATA_PRODUCED = SynchedEntityData.defineId(TFCBactrianCamel.class, EntityDataSerializers.LONG);
    }
}
