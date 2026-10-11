package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

import en.predator97427fr.aliencraft.entity.AncientBeetleEntity;
import en.predator97427fr.aliencraft.procedures.AncientBeetleRightclickedOnEntityProcedure;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.entity.ancient_beetle.AncientBeetleData;

@Mixin(value = AncientBeetleEntity.class, remap = false)
public class AncientBeetleEntityMixin extends TamableAnimal {

    @Shadow
    @Final
    public static EntityDataAccessor<Boolean> DATA_saddled;
    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected AncientBeetleEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 12.0F)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5F)
                .add(Attributes.FOLLOW_RANGE, 16.0F)
                .add(Attributes.ARMOR, 10F));
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    private void tfg$registerGoals(CallbackInfo ci) {
        var beetle = (AncientBeetleEntity) (Object) this;

        this.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true, (entity) -> entity.getType().is(TFGTags.Entities.VenusPrey)));
        this.goalSelector.addGoal(4, new TemptGoal(beetle, 1.0F, Ingredient.of(TFGTags.Items.VenusianArthropodFoods), false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(beetle, Player.class, 0.6F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(beetle));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(beetle));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    // Taming stuff

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), remap = true)
    public void tfg$finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        tfg$setIsMale(random.nextBoolean());
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = true)
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(AncientBeetleData.DATA_IS_MALE, true);
        entityData.define(AncientBeetleData.DATA_FAMILIARITY, 0f);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = true)
    private void tfg$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tfg$setFamiliarity(tag.getFloat("familiarity"));
        tfg$setIsMale(tag.getBoolean("male"));
        tfg$nextFeedTime = tag.getLong("nextFeed");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = true)
    public void tfg$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("male", tfg$isMale());
        tag.putFloat("familiarity", tfg$getFamiliarity());
        tag.putLong("nextFeed", tfg$nextFeedTime);
    }

    @Unique
    public float tfg$getFamiliarity() {
        return entityData.get(AncientBeetleData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(AncientBeetleData.DATA_FAMILIARITY, familiarity);
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(AncientBeetleData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(AncientBeetleData.DATA_IS_MALE);
    }

    @Inject(method = "isFood", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!FoodCapability.isRotten(stack) && Helpers.isItem(stack, TFGTags.Items.VenusianArthropodFoods));
    }

    @Inject(method = "mobInteract", at = @At(value = "HEAD"), remap = true, cancellable = true)
    private void tfg$mobInteractFood(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        final ItemStack stack = player.getItemInHand(hand);
        if (isFood(stack)) {
            if (!level().isClientSide) {
                final long ticks = Calendars.SERVER.getTicks();
                if (ticks > tfg$nextFeedTime) {
                    tfg$setFamiliarity(tfg$getFamiliarity() + 0.07f);
                    tfg$nextFeedTime = ticks + ICalendar.TICKS_IN_DAY;
                    usePlayerItem(player, hand, stack);
                    playSound(SoundEvents.PLAYER_BURP);

                    // If it's now familiar enough, set the owner
                    if (isTame()) {
                        tame(player);
                    }
                }
            }

            // Extra food heals
            if (isTame()) {
                this.heal(10.0F);
                usePlayerItem(player, hand, stack);
                playSound(SoundEvents.PLAYER_BURP);
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Len/predator97427fr/aliencraft/procedures/AncientBeetleRightclickedOnEntityProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V"), remap = true)
    private void tfg$mobInteractSaddle(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, @Local(name = "stack") ItemStack stack) {
        boolean usingSaddle = stack.is(Items.SADDLE);
        boolean wasSaddled = this.entityData.get(DATA_saddled);

        if (usingSaddle && !wasSaddled) {
            if (isTame()) {
                AncientBeetleRightclickedOnEntityProcedure.execute(world, x, y, z, entity, sourceentity);
            } else {
                playSound(SoundEvents.SILVERFISH_HURT);
            }
        }
    }

    @Override
    public boolean isTame() {
        return tfg$getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }
}
