package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import dev.dh.arthropocolypse.entity.Platerodrilus;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.entity.platerodrilus.PlaterodrilusData;

@Mixin(value = Platerodrilus.class, remap = false)
public abstract class PlaterodrilusMixin extends TamableAnimal {

    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected PlaterodrilusMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    protected void tfg$registerGoals(CallbackInfo ci) {
        Animal mob = (Animal) (Object) this;

        mob.goalSelector.addGoal(4, new NearestAttackableTargetGoal<>(mob, Animal.class, true, (entity) -> entity.getType().is(TFGTags.Entities.VenusPrey)));
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 90.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ATTACK_DAMAGE, 14.0F)
                .add(Attributes.ARMOR, 5.0F)
                .add(Attributes.JUMP_STRENGTH, 1.0F)
				.add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1.0f));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag) {
        tfg$setIsMale(random.nextBoolean());
        return super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = true)
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(PlaterodrilusData.DATA_IS_MALE, true);
        entityData.define(PlaterodrilusData.DATA_FAMILIARITY, 0f);
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
        return entityData.get(PlaterodrilusData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(PlaterodrilusData.DATA_FAMILIARITY, familiarity);
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(PlaterodrilusData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(PlaterodrilusData.DATA_IS_MALE);
    }

    @Inject(method = "isFood", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!FoodCapability.isRotten(stack) && Helpers.isItem(stack, TFGTags.Items.VenusianArthropodFoods));
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        final ItemStack held = player.getItemInHand(hand);
        if (isFood(held)) {
            if (!level().isClientSide) {
                final long ticks = Calendars.SERVER.getTicks();
                if (ticks > tfg$nextFeedTime) {
                    tfg$setFamiliarity(tfg$getFamiliarity() + 0.07f);
                    tfg$nextFeedTime = ticks + ICalendar.TICKS_IN_DAY;
                    usePlayerItem(player, hand, held);
                    playSound(SoundEvents.PLAYER_BURP);

                    // If it's now familiar enough, set the owner
                    if (isTame()) {
                        this.setOrderedToSit(true);
                        tame(player);
                    }
                }
            }

            // Extra food restores flaps and heals
            if (isTame()) {
                this.setOrderedToSit(true);
                this.heal(20.0F);
                usePlayerItem(player, hand, held);
                playSound(SoundEvents.PLAYER_BURP);
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }

        // Remove default "food" handling (they eat logs?)
        if (held.is(ItemTags.LOGS)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Override
    public boolean isTame() {
        return tfg$getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }
}
