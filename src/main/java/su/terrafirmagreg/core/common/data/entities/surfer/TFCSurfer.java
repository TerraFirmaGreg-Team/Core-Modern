package su.terrafirmagreg.core.common.data.entities.surfer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.entities.BrainBreeder;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeMod;
import net.wanmine.wab.entity.Surfer;
import net.wanmine.wab.entity.Toxlacanth;
import net.wanmine.wab.init.data.WabTags;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGEntities;

/**
 * Most of this code is copied and pasted from TFCFrog, because it's a very simple animal that only has
 * gender and familiarity and nothing else. The rest of it used to be a mixin we had to the base Surfer.
 * There may be other bits of code here that are (c) WanMine, used with permission
 */

public class TFCSurfer extends Surfer implements BrainBreeder {

    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(TFCSurfer.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(TFCSurfer.class, EntityDataSerializers.FLOAT);

    private long lastMated = Long.MIN_VALUE;
    private long nextFeedTime = Long.MIN_VALUE;

    public TFCSurfer(EntityType<? extends AbstractHorse> animal, Level level) {
        super(animal, level);
    }

    public static TFCSurfer makeTFCSurfer(EntityType<? extends AbstractHorse> type, Level level) {
        return new TFCSurfer(type, level);
    }

    // Randomly set their color
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag) {
        float r = random.nextFloat();
        if (r > 0.9)
            this.setCoralColor("brain");
        else if (r > 0.8)
            this.setCoralColor("bubble");
        else if (r > 0.7)
            this.setCoralColor("tube");
        else if (r > 0.6)
            this.setCoralColor("fire");
        else if (r > 0.5)
            this.setCoralColor("horn");

        setIsMale(random.nextBoolean());
        return super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
    }

    public static boolean spawnRules(EntityType<? extends TFCSurfer> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand) {
        return level.getBlockState(pos) == TFGBlocks.MARS_WATER.get().defaultBlockState();
    }

    public static AttributeSupplier.Builder getDefaultAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, Attributes.MOVEMENT_SPEED.getDefaultValue())
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.JUMP_STRENGTH, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(ForgeMod.SWIM_SPEED.get(), 8.0F);
    }

    @Override
    public void tick() {
        this.setAirSupply(300);
        super.tick();
    }

    @Override
    public void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Toxlacanth.class, false, false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_IS_MALE, true);
        entityData.define(DATA_FAMILIARITY, 0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFamiliarity(tag.getFloat("familiarity"));
        setIsMale(tag.getBoolean("male"));
        lastMated = tag.getLong("lastMated");
        nextFeedTime = tag.getLong("nextFeed");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("male", isMale());
        tag.putFloat("familiarity", getFamiliarity());
        tag.putLong("lastMated", lastMated);
        tag.putLong("nextFeed", nextFeedTime);
    }

    public float getFamiliarity() {
        return entityData.get(DATA_FAMILIARITY);
    }

    public void setFamiliarity(float familiarity) {
        entityData.set(DATA_FAMILIARITY, familiarity);
    }

    public void setIsMale(boolean male) {
        entityData.set(DATA_IS_MALE, male);
    }

    @Override
    public boolean isMale() {
        return entityData.get(DATA_IS_MALE);
    }

    @Override
    public void setMated(long ticks) {
        lastMated = ticks;
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal != this && animal instanceof TFCSurfer other) {
            final float min = TFCAnimalProperties.READY_TO_MATE_FAMILIARITY;
            return other.isMale() != isMale() && beenLongEnoughToMate() && other.beenLongEnoughToMate() && getFamiliarity() > min && other.getFamiliarity() > min && fedRecently()
                    && other.fedRecently();
        }
        return false;
    }

    protected boolean beenLongEnoughToMate() {
        return Calendars.get(level()).getTicks() > lastMated + (ICalendar.TICKS_IN_DAY * 12);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return !FoodCapability.isRotten(stack) && Helpers.isItem(stack, WabTags.Items.SURFER_FOOD);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob ageableMob) {
        return (AgeableMob) ((EntityType) TFGEntities.SURFER.get()).create(level);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        final ItemStack held = player.getItemInHand(hand);
        if (isFood(held)) {
            if (!level().isClientSide) {
                final long ticks = Calendars.SERVER.getTicks();
                if (ticks > nextFeedTime) {
                    setFamiliarity(getFamiliarity() + 0.1f);
                    nextFeedTime = ticks + ICalendar.TICKS_IN_DAY;
                    usePlayerItem(player, hand, held);
                    playSound(SoundEvents.FROG_EAT);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private boolean fedRecently() {
        return Calendars.get(level()).getTicks() < nextFeedTime;
    }

    @Override
    public boolean isTamed() {
        return getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }
}
