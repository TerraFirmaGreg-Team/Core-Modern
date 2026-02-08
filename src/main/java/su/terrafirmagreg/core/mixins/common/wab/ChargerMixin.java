package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.wanmine.wab.entity.Charger;
import net.wanmine.wab.init.world.WabSounds;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.entities.charger.ChargerData;

@Mixin(value = Charger.class)
public abstract class ChargerMixin extends AbstractHorse {
    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected ChargerMixin(EntityType<? extends AbstractHorse> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    public void tfg$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        tfg$setIsMale(random.nextBoolean());
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(ChargerData.DATA_IS_MALE, true);
        entityData.define(ChargerData.DATA_FAMILIARITY, 0f);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void tfg$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tfg$setFamiliarity(tag.getFloat("familiarity"));
        tfg$setIsMale(tag.getBoolean("male"));
        tfg$nextFeedTime = tag.getLong("nextFeed");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void tfg$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("male", tfg$isMale());
        tag.putFloat("familiarity", tfg$getFamiliarity());
        tag.putLong("nextFeed", tfg$nextFeedTime);
    }

    @Unique
    public float tfg$getFamiliarity() {
        return entityData.get(ChargerData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(ChargerData.DATA_FAMILIARITY, familiarity);
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(ChargerData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(ChargerData.DATA_IS_MALE);
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("HEAD"), cancellable = true)
    public void tfg$spawnChildFromBreeding(ServerLevel level, Animal mate, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    public void tfg$registerGoals(CallbackInfo ci) {
        this.goalSelector.removeAllGoals(g -> g instanceof TemptGoal);
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.0F, Ingredient.of(TFCItems.FOOD.get(Food.BEET).get()), false));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.0F, Ingredient.of(TFGTags.Items.MartianHerbivoreFoods), false));
    }

    // Chargers eat the same thing as wraptors
    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    public void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!FoodCapability.isRotten(stack) && Helpers.isItem(stack, TFGTags.Items.MartianHerbivoreFoods));
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
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
                    if (isTamed()) {
                        setTamed(true);
                        setOwnerUUID(player.getUUID());
                    }
                }
            }

            // Extra food heals
            if (isTamed()) {
                this.heal(4.0F);
                usePlayerItem(player, hand, held);
                playSound(SoundEvents.PLAYER_BURP);
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        } else if (held.is(TFCItems.FOOD.get(Food.BEET).get())) {
            this.usePlayerItem(player, hand, held);
            ((Charger) (Object) this).setAggressive();
            this.playSound(WabSounds.CHARGER_AGGRESSIVE.get(), this.getSoundVolume() * 3.0F, this.getVoicePitch());
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }
    }

    @Override
    public boolean isTamed() {
        return tfg$getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }
}
