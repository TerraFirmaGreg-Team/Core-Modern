package su.terrafirmagreg.core.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosApi;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.items.TFGItems;
import su.terrafirmagreg.core.common.item.wearable.FlippersItem;
import su.terrafirmagreg.core.common.item.wearable.SnorkelItem;
import su.terrafirmagreg.core.common.item.wearable.SnowshoesItem;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WearableAccessoryHandler {

    private static final String PERSIST_SNORKEL = TFGCore.id("had_snorkel_equipped").toString();

    private static final int WEARABLE_LOGIC_INTERVAL = 5;

    private WearableAccessoryHandler() {
    }

    public static boolean hasSnorkelEquipped(Player player) {
        return hasEquipped(player, EquipmentSlot.HEAD, TFGItems.SNORKEL.get());
    }

    public static boolean hasFlippersEquipped(Player player) {
        return hasEquipped(player, EquipmentSlot.FEET, TFGItems.FLIPPERS.get());
    }

    public static boolean hasSnowshoesEquipped(Player player) {
        return hasEquipped(player, EquipmentSlot.FEET, TFGItems.SNOWSHOES.get());
    }

    private static boolean hasEquipped(Player player, EquipmentSlot slot, Item item) {
        if (player.getItemBySlot(slot).is(item)) {
            return true;
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(stack -> stack.is(item)))
                .orElse(false);
    }

    public static boolean isFeetInSnowshoesTaggedBlock(Entity entity) {
        return entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()))
                .is(TFGTags.Blocks.NOT_SLOWED_WITH_SNOWSHOES);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        if (player.tickCount % WEARABLE_LOGIC_INTERVAL != 0) {
            return;
        }

        tickSnorkel(player);
        tickFlippers(player);
        tickSnowshoesStep(player);
    }

    private static void tickSnorkel(Player player) {
        boolean hasSnorkel = hasSnorkelEquipped(player);
        boolean hadSnorkel = player.getPersistentData().getBoolean(PERSIST_SNORKEL);

        if (hadSnorkel && !hasSnorkel) {
            removeOurSnorkelWaterBreathing(player);
        }
        player.getPersistentData().putBoolean(PERSIST_SNORKEL, hasSnorkel);

        if (!hasSnorkel || !SnorkelItem.shouldApplyWaterBreathing(player)) {
            return;
        }

        int duration = SnorkelItem.getEffectDurationTicks(player) - 1;
        player.addEffect(new MobEffectInstance(
                MobEffects.WATER_BREATHING,
                duration,
                0,
                false,
                false,
                SnorkelItem.shouldShowIcon()));
    }

    private static void removeOurSnorkelWaterBreathing(Player player) {
        MobEffectInstance effectInstance = player.getEffect(MobEffects.WATER_BREATHING);
        if (effectInstance == null) {
            return;
        }
        int maxDuration = SnorkelItem.getEffectDurationTicks(player);
        if (effectInstance.getAmplifier() == 0
                && !effectInstance.isVisible()
                && effectInstance.getDuration() < maxDuration) {
            player.removeEffect(MobEffects.WATER_BREATHING);
        }
    }

    private static void tickFlippers(Player player) {
        AttributeInstance swim = player.getAttribute(ForgeMod.SWIM_SPEED.get());
        if (swim == null) {
            return;
        }
        if (hasFlippersEquipped(player)) {
            if (swim.getModifier(FlippersItem.SWIM_SPEED_MODIFIER_UUID) == null) {
                swim.addPermanentModifier(new AttributeModifier(
                        FlippersItem.SWIM_SPEED_MODIFIER_UUID,
                        FlippersItem.SWIM_SPEED_MODIFIER_NAME,
                        FlippersItem.DEFAULT_SWIM_SPEED_ADDITION,
                        AttributeModifier.Operation.ADDITION));
            }
        } else {
            swim.removeModifier(FlippersItem.SWIM_SPEED_MODIFIER_UUID);
        }
    }

    private static void tickSnowshoesStep(Player player) {
        AttributeInstance step = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (step == null) {
            return;
        }
        boolean apply = hasSnowshoesEquipped(player) && isFeetInSnowshoesTaggedBlock(player);
        if (apply) {
            if (step.getModifier(SnowshoesItem.STEP_HEIGHT_MODIFIER_UUID) == null) {
                step.addPermanentModifier(new AttributeModifier(
                        SnowshoesItem.STEP_HEIGHT_MODIFIER_UUID,
                        SnowshoesItem.STEP_HEIGHT_MODIFIER_NAME,
                        SnowshoesItem.STEP_HEIGHT_ADDITION_ON_SNOW,
                        AttributeModifier.Operation.ADDITION));
            }
        } else {
            step.removeModifier(SnowshoesItem.STEP_HEIGHT_MODIFIER_UUID);
        }
    }
}
