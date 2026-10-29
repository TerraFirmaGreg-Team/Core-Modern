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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.Tags;
import net.wanmine.wab.entity.Walker;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.entity.walker.WalkerData;

@Mixin(value = Walker.class, remap = false)
public abstract class WalkerMixin extends AbstractChestedHorse {

    @Shadow
    protected abstract void equipChest(Player player, ItemStack item);

    @Unique
    private long tfg$nextFeedTime = Long.MIN_VALUE;

    protected WalkerMixin(EntityType<? extends AbstractChestedHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), remap = true)
    public void tfg$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        tfg$setIsMale(random.nextBoolean());
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = true)
    private void tfg$defineSyncedData(CallbackInfo ci) {
        entityData.define(WalkerData.DATA_IS_MALE, true);
        entityData.define(WalkerData.DATA_FAMILIARITY, 0f);
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
        return entityData.get(WalkerData.DATA_FAMILIARITY);
    }

    @Unique
    public void tfg$setFamiliarity(float familiarity) {
        entityData.set(WalkerData.DATA_FAMILIARITY, familiarity);
    }

    @Unique
    public void tfg$setIsMale(boolean male) {
        entityData.set(WalkerData.DATA_IS_MALE, male);
    }

    @Unique
    public boolean tfg$isMale() {
        return entityData.get(WalkerData.DATA_IS_MALE);
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true, remap = true)
    public void tfg$canMate(Animal pOtherAnimal, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true, remap = true)
    public void tfg$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!FoodCapability.isRotten(stack) && Helpers.isItem(stack, TFGTags.Items.MartianHerbivoreFoods));
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, remap = true)
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
                        tameWithName(player);
                    }
                }
            }

            // Extra food heals
            if (isTamed()) {
                this.heal(10.0F);
                usePlayerItem(player, hand, held);
                playSound(SoundEvents.PLAYER_BURP);
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }

        if (!this.hasChest() && held.is(Tags.Items.CHESTS)) {
            this.equipChest(player, held);
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
        }
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.hasChest()) {
            this.setChest(false);
        }
    }

    @Override
    public boolean isTamed() {
        return tfg$getFamiliarity() > HorseProperties.TAMED_FAMILIARITY;
    }
}
