package su.terrafirmagreg.core.common.environment;

import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.tags.ModEntityTypeTags;
import earth.terrarium.adastra.common.tags.ModItemTags;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGDamageSources;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class HypoxiaHandler {

    private static final int BREATH_LOSS = 1;
    private static final int BREATH_LOSS_VACUUM = 5;

    /** Armstrong limit, constant use to see if the player is in a vacuum dimension depending of the defined value in DimensionEnvironment */
    private static final float ARMSTRONG_LIMIT = 0.0618f;

    private static final int DAMAGE_INTERVAL = 20;

    private static final float DAMAGE_START = 0.05f;
    private static final float DAMAGE_STEP = 0.03f;
    private static final float DAMAGE_MAX = 0.25f;
    private static final int SEVERE_AFTER = 60;

    /** Debt which stacks at 1 per tick */
    private static final int RECOVERY_RATE = 2;
    private static final int MAX_DEBT = 200;

    /** Negative effects duration */
    private static final int EFFECT_DURATION = 60;
    private static final int NAUSEA_DURATION = 100;

    private static final String OXYGEN_DEBT = TFGCore.id("oxygen_debt").toString();

    private HypoxiaHandler() {
    }

    /** Follows the same method as Ad Astra */
    public static boolean hasBreathableAir(Player player) {
        if (AdAstraConfig.disableOxygen) {
            return true;
        }
        if (player.getAbilities().invulnerable) {
            return true;
        }
        if (OxygenApi.API.hasOxygen(player.level())) {
            return true;
        }
        if (player.getType().is(ModEntityTypeTags.LIVES_WITHOUT_OXYGEN)) {
            return true;
        }
        if (player.getType().is(ModEntityTypeTags.CAN_SURVIVE_IN_SPACE)) {
            return true;
        }
        if (SpaceSuitItem.hasFullSet(player, ModItemTags.SPACE_RESISTANT_ARMOR)) {
            return true;
        }
        if (SpaceSuitItem.hasFullSet(player, ModItemTags.OXYGEN_SUPPLYING_ARMOR)) {
            return true;
        }
        if (SpaceSuitItem.hasFullSet(player) && SpaceSuitItem.hasOxygen(player)) {
            return true;
        }
        return OxygenApi.API.hasOxygen(player);
    }

    public static boolean isSuffocating(Player player) {
        return player.getAirSupply() <= 0 && !hasBreathableAir(player);
    }

    public static int getOxygenDebt(Player player) {
        return player.getPersistentData().getInt(OXYGEN_DEBT);
    }

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        int debt = data.getInt(OXYGEN_DEBT);

        if (hasBreathableAir(player)) {
            if (debt > 0 && event.canBreathe()) {
                debt = Math.max(0, debt - RECOVERY_RATE);
                setOxygenDebt(data, debt);
                limitRefill(event, player, debt);
            }
            return;
        }

        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        float pressure = EnvironmentSystem.getPressure(player.level(), eyePos);
        int breathLoss = pressure < ARMSTRONG_LIMIT ? BREATH_LOSS_VACUUM : BREATH_LOSS;
        int air = Math.max(0, player.getAirSupply());

        event.setCanBreathe(false);
        event.setConsumeAirAmount(Math.min(breathLoss, air));
        if (air > 0) {
            return;
        }

        debt = Math.min(debt + 1, MAX_DEBT);
        setOxygenDebt(data, debt);

        if (player.level().getGameTime() % DAMAGE_INTERVAL == 0) {
            hurt(player, debt);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isSuffocating(player)) {
            event.setCanceled(true);
        }
    }

    private static void hurt(ServerPlayer player, int debt) {
        float fraction = Math.min(DAMAGE_START + DAMAGE_STEP * (debt / DAMAGE_INTERVAL), DAMAGE_MAX);

        float healthModifier = player.getFoodData() instanceof TFCFoodData foodData ? foodData.getHealthModifier() : 1f;
        TFGDamageSources.hypoxia(player, player.getMaxHealth() * fraction * healthModifier);

        if (!player.isAlive()) {
            return;
        }

        addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION, 1);
        addEffect(player, MobEffects.DIG_SLOWDOWN, EFFECT_DURATION, 1);
        addEffect(player, MobEffects.WEAKNESS, EFFECT_DURATION, 0);
        if (debt > SEVERE_AFTER) {
            addEffect(player, MobEffects.DARKNESS, EFFECT_DURATION, 0);
            addEffect(player, MobEffects.CONFUSION, NAUSEA_DURATION, 0);
        }
    }

    // Debt locking the breath bar - may need a better visual indicator

    private static void limitRefill(LivingBreatheEvent event, Player player, int debt) {
        int capacity = player.getMaxAirSupply() * (MAX_DEBT - debt) / MAX_DEBT;
        int room = capacity - player.getAirSupply();
        if (room <= 0) {
            event.setCanRefillAir(false);
        } else {
            event.setRefillAirAmount(Math.min(event.getRefillAirAmount(), room));
        }
    }

    private static void setOxygenDebt(CompoundTag data, int debt) {
        if (debt > 0) {
            data.putInt(OXYGEN_DEBT, debt);
        } else {
            data.remove(OXYGEN_DEBT);
        }
    }

    private static void addEffect(ServerPlayer player, MobEffect effect, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, true));
    }
}
