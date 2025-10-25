package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.wanmine.wab.entity.Soarer;
import net.wanmine.wab.init.data.WabTags;

import su.terrafirmagreg.core.common.data.entities.soarer.SoarerData;

@Mixin(value = Soarer.class)
public abstract class SoarerMixin extends TamableAnimal {

    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected SoarerMixin(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    public void tfg$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        tfg$setIsMale(random.nextBoolean());
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(SoarerData.DATA_IS_MALE, true);
        entityData.define(SoarerData.DATA_FAMILIARITY, 0f);
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
        return entityData.get(SoarerData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(SoarerData.DATA_FAMILIARITY, familiarity);
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(SoarerData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(SoarerData.DATA_IS_MALE);
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    public void tfg$canMate(Animal pOtherAnimal, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    // Soarers eat the same thing as Surfers
    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    public void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!FoodCapability.isRotten(stack) && Helpers.isItem(stack, WabTags.Items.SURFER_FOOD));
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
                    if (isTame()) {
                        tame(player);
                    }
                }
            }

            // Extra food restores flaps
            if (isTame()) {
                ((Soarer) (Object) this).addFlaps(10);
                usePlayerItem(player, hand, held);
                playSound(SoundEvents.PLAYER_BURP);
            }

            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Override
    public boolean isTame() {
        return tfg$getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }

    // Buffs how far they can glide for
    @ModifyArg(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/wanmine/wab/entity/Soarer;setDeltaMovement(DDD)V", ordinal = 2), index = 1)
    public double tfg$tick(double par1) {
        return par1 / 1.5;
    }
}
