package su.terrafirmagreg.core.mixins.common.tfc_astikor_carts;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.common.TFCEffects;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.util.Helpers;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import de.mennomax.astikorcarts.entity.AbstractDrawnInventoryEntity;
import tfcastikorcarts.common.entities.carts.TFCSupplyCartEntity;
import tfcastikorcarts.config.TFCAstikorCartsConfig;

@Mixin(value = TFCSupplyCartEntity.class, remap = false)
public abstract class TFCSupplyCartEntityMixin extends AbstractDrawnInventoryEntity {

    public TFCSupplyCartEntityMixin(EntityType<? extends Entity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Unique
    private static final float HORSE_TO_PLAYER_PULL_RATIO = 3;

    @Shadow
    public abstract float countOverburdened();

    /** Updates for cart and pulling entity each tick
     * @author Ujhik
     * @reason When riding an animal pulling a cart, the debuffs where applied to the player. This improves the logic adding a correct handling for pulling animals.
     */
    @Overwrite(remap = false)
    public void pulledTick() {
        super.pulledTick();

        if (level().isClientSide) {
            return;
        }

        Entity pullingEntity = this.getPulling();

        if (!(pullingEntity instanceof LivingEntity livingEntity)) {
            return;
        }

        Player player;
        double weightFactor = countOverburdened();

        var tfcAstikorConfig = TFCAstikorCartsConfig.COMMON;
        final double pinnedThreshold = tfcAstikorConfig.pinnedThreshold.get();
        final double overburdenedThreshold = tfcAstikorConfig.overburdenedThreshold.get();
        final double exhaustedThreshold = tfcAstikorConfig.exhaustedThreshold.get();

        // Handling Player pulling cart
        if (livingEntity instanceof Player pl) {
            player = pl;

            if (!(player.getFoodData() instanceof TFCFoodData foodData)) {
                return;
            }

            if (weightFactor > pinnedThreshold) {
                player.addEffect(new MobEffectInstance(TFCEffects.PINNED.get(), 2, 0, false, false));
            } else if (weightFactor > overburdenedThreshold) {
                player.addEffect(Helpers.getOverburdened(false));
            } else if (weightFactor > exhaustedThreshold) {
                player.addEffect(Helpers.getExhausted(false));
            }

            final double healthFactor = tfcAstikorConfig.toggleFoodSpeed.get()
                    ? Mth.map(foodData.getNutrition().getAverageNutrition(), 0D, 1.0D, 0.75D, 1.0D) * Mth.map(foodData.getThirst(), 0D, 100D, 0.5D, 1.0D)
                    : 1.0D;

            player.setDeltaMovement(player.getDeltaMovement().multiply(healthFactor, 1.0D, healthFactor));

            return;
        }

        // Handling animal pulling cart
        final double pinnedThresholdAnimal = pinnedThreshold * HORSE_TO_PLAYER_PULL_RATIO;
        final double overburdenedThresholdAnimal = overburdenedThreshold * HORSE_TO_PLAYER_PULL_RATIO;
        final double exhaustedThresholdAnimal = exhaustedThreshold * HORSE_TO_PLAYER_PULL_RATIO;

        final double overburdenedSpeedFactor = 0.2;
        final double exhaustedSpeedFactor = 0.5;

        double speedFactor = 1;

        // Calculating speed factor
        if (weightFactor > pinnedThresholdAnimal) {
            speedFactor = 0.0;
        } else if (weightFactor > overburdenedThresholdAnimal) {
            speedFactor = Mth.map(weightFactor, overburdenedThresholdAnimal, pinnedThresholdAnimal, overburdenedSpeedFactor, 0.0);
        } else if (weightFactor > exhaustedThresholdAnimal) {
            speedFactor = Mth.map(weightFactor, exhaustedThresholdAnimal, overburdenedThresholdAnimal, exhaustedSpeedFactor, overburdenedSpeedFactor);
        } else {
            // Exponential interpolation so speed decreases slow at first and faster as weightFactor increases
            double t = Mth.map(weightFactor, 0, exhaustedThresholdAnimal, 0.0, 1.0);
            speedFactor = Mth.lerp(Math.pow(t, 2.5), 1.0, exhaustedSpeedFactor);
        }

        // Handle pinned
        if (speedFactor <= 0) {
            livingEntity.addEffect(new MobEffectInstance(TFCEffects.PINNED.get(), 2, 0, false, false));
            return;
        }

        // Obtaining the slowdown coefficient to future-proof
        AttributeModifier modifier = MobEffects.MOVEMENT_SLOWDOWN
                .getAttributeModifiers()
                .get(Attributes.MOVEMENT_SPEED);
        float slowdownReduction = Mth.abs((float) modifier.getAmount());
        int maxSlowDownReduction = (int) Math.floor((1.0 / slowdownReduction) - 1);
        int slowAmplifier = (int) ((1 - speedFactor) / slowdownReduction);

        // Handle no slow
        if (slowAmplifier == 0)
            return;

        //Adjusting because 0 applies the first slowness effect
        slowAmplifier -= 1;
        slowAmplifier = Mth.clamp(slowAmplifier, 0, maxSlowDownReduction); // Safety clamp

        // Apply slowness effect based on speedFactor
        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, slowAmplifier, false, false));
    }
}
