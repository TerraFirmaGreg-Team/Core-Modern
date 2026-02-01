package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.wanmine.wab.entity.Snatcher;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.entities.snatcher.SnatcherData;

@Mixin(value = Snatcher.class)
public abstract class SnatcherMixin extends ShoulderRidingEntity {

    @Shadow(remap = false)
    public abstract boolean hasBarrel();

    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected SnatcherMixin(EntityType<? extends ShoulderRidingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(SnatcherData.DATA_IS_MALE, true);
        entityData.define(SnatcherData.DATA_FAMILIARITY, 0f);
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
        return entityData.get(SnatcherData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(SnatcherData.DATA_FAMILIARITY, familiarity);
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(SnatcherData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(SnatcherData.DATA_IS_MALE);
    }

    @Unique
    public boolean tfg$isFood(ItemStack stack) {
        return !FoodCapability.isRotten(stack) && Helpers.isItem(stack, TFGTags.Items.MartianPiscivoreFoods);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void tfg$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        final ItemStack held = player.getItemInHand(hand);
        if (tfg$isFood(held)) {
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

            if (isTame()) {
                this.heal(4.0F);
                usePlayerItem(player, hand, held);
                playSound(SoundEvents.PLAYER_BURP);
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        } else if (this.isTame() && player.getUUID().equals(this.getOwnerUUID())) {
            if (player.isShiftKeyDown()) {
                if (held.is(Tags.Items.CHESTS) && !this.hasBarrel()) {
                    ((Snatcher) (Object) this).setBarrel(true);
                    held.shrink(1);
                    cir.setReturnValue(InteractionResult.CONSUME);
                }
            }
        }
    }

    @Override
    public boolean isTame() {
        return tfg$getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }
}
