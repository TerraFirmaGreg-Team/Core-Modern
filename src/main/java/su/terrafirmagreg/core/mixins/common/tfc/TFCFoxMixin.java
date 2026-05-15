package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.entities.prey.TFCFox;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import su.terrafirmagreg.core.common.data.TFGEntities;
import su.terrafirmagreg.core.common.entity.fox.FoxData;
import su.terrafirmagreg.core.common.entity.fox.TFGFox;

@Mixin(value = TFCFox.class)
public abstract class TFCFoxMixin extends Fox {
    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected TFCFoxMixin(EntityType<? extends Fox> type, Level level) {
        super(type, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("familiarity", tfg$getFamiliarity());
        tag.putLong("nextFeed", tfg$nextFeedTime);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(FoxData.DATA_FAMILIARITY, 0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tfg$setFamiliarity(tag.getFloat("familiarity"));
        tfg$nextFeedTime = tag.getLong("nextFeed");
    }

    @Unique
    public float tfg$getFamiliarity() {
        return entityData.get(FoxData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(FoxData.DATA_FAMILIARITY, familiarity);
    }

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    public void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!FoodCapability.isRotten(stack) && Helpers.isItem(stack, TFCTags.Items.FOODS));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        final ItemStack held = player.getItemInHand(hand);
        if (isFood(held)) {
            if (!level().isClientSide) {
                final long ticks = Calendars.SERVER.getTicks();
                if (ticks > tfg$nextFeedTime) {
                    tfg$setFamiliarity(tfg$getFamiliarity() + 0.1f);
                    tfg$nextFeedTime = ticks + ICalendar.TICKS_IN_DAY;
                    usePlayerItem(player, hand, held);
                    playSound(SoundEvents.PLAYER_BURP);

                    if (tfg$getFamiliarity() > 0.99f) {
                        final TFGFox fox = convertTo(TFGEntities.TFG_FOX.get(), false);
                        if (fox != null && level() instanceof ServerLevelAccessor server) {
                            fox.finalizeSpawn(server, level().getCurrentDifficultyAt(blockPosition()), MobSpawnType.CONVERSION, null, null);
                            fox.setVariant(this.getVariant());
                            fox.setBirthDay(Calendars.get(this.level()).getTotalDays() - 120L);
                        }
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

}
