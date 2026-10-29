package su.terrafirmagreg.core.utils;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;

@SuppressWarnings({ "unused", "NoTranslation" })
public final class TFGHelpers {

    public static final Random RANDOM = new Random();

    public static boolean isMaterialRegistrationFinished;

    public static Material getMaterial(@NotNull String materialName) {
        var material = GTCEuAPI.materialManager.getMaterial(materialName);
        if (material == null) {
            material = GTCEuAPI.materialManager.getMaterial(TFGCore.MOD_ID + ":" + materialName);
        }

        return material;
    }

    public static void sendChatMessagePortalsIsDisabled(Level level, Entity entity) {
        if (level.isClientSide()) {
            if (level.getGameTime() % 100 == 0) {
                entity.sendSystemMessage(
                        Component.translatable("tfg.disabled_portal").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    // Second parameter should be like [Material, double, Material, double, ...]
    public static void registerMaterialInfo(ItemStack itemStack, Object[] materialStacks) {
        if (itemStack.isEmpty()) {
            TFGCore.LOGGER.error("Error in registerMaterialInfoAdv - item not found: {}", itemStack);
            return;
        }

        if (materialStacks.length % 2 != 0) {
            TFGCore.LOGGER.error("Error in registerMaterialInfoAdv - input array length is not a multiple of 2: {}", itemStack);
            return;
        }

        var matStacks = new ArrayList<MaterialStack>();
        for (int i = 0; i < materialStacks.length; i += 2) {
            if (materialStacks[i] instanceof Material mat && materialStacks[i + 1] instanceof Double count) {
                matStacks.add(new MaterialStack(mat, Math.round(count * GTValues.M)));
            } else {
                TFGCore.LOGGER.error("Error in registerMaterialInfoAdv - input item is not a material or double: {}", itemStack);
                return;
            }
        }

        ItemMaterialData.registerMaterialInfo(itemStack.getItem(), new ItemMaterialInfo(matStacks));
    }

    public static void clearMaterialInfo(ItemStack itemStack) {
        ItemMaterialData.clearMaterialInfo(itemStack.getItem());
    }

    public static void registerCobbleBlock(String tagPrefix, ResourceLocation cobbleBlock) {
        GTBlocks.registerCobbleBlock(TagPrefix.get(tagPrefix),
                () -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(cobbleBlock)).defaultBlockState());
    }

    /**
     * Returns server glassworking stats for an optional player or base duration and cooldown.
     * If the player has the Water Breathing effect, the duration is divided by (2 + the effect amplifier).
     * @param player The glassworker. Null for base durations.
     * @param isCooldown true for cooldown, false for duration.
     * @return The glassworking duration or cooldown in ticks.
     */
    public static int getGlassworkingStat(@Nullable Player player, Boolean isCooldown) {
        int duration;
        if (isCooldown) {
            duration = TFGConfig.SERVER.GLASSBLOWING_COOLDOWN.get();
        } else {
            duration = TFGConfig.SERVER.GLASSBLOWING_DURATION.get();
        }
        if (player == null) {
            return duration;
        }
        boolean bigLungs = player.getEffect(MobEffects.WATER_BREATHING) != null;

        if (bigLungs) {
            int amplifier = Objects.requireNonNull(player.getEffect(MobEffects.WATER_BREATHING)).getAmplifier();
            return Math.max(1, duration / (2 + amplifier));
        } else {
            return duration;
        }
    }

    /**
     * Helper method to format a timer countdown tooltip.
     * @param currentTick Current tick count.
     * @param maxTick Maximum tick count.
     * @return Formatted timer tooltip component with days to seconds after passing 24k ticks.
     */
    public static Component formattedTimer(int currentTick, int maxTick) {
        int remainingTicks = Math.max(0, maxTick - currentTick);
        float remainingSeconds = (float) remainingTicks / 20.0f;
        float remainingDays = remainingTicks / 24000.0f;

        Component timerText;
        Component daysText = Component.translatable("tfc.tooltip.time_delta_days", String.format("%.1f", remainingDays));
        MutableComponent timerIcon = ((int) remainingSeconds % 2 != 0) ? Component.literal("⏳ ") : Component.literal("⌛ ");

        if (remainingDays >= 1.0F) {
            timerText = timerIcon.append(daysText);
        } else {
            timerText = timerIcon.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));
        }
        return timerText;
    }

    /**
     * Helper method to format time ticks into seconds or days.
     * @param timeTicks Ticks.
     * @return Formatted time component with days to seconds after passing 24k ticks.
     */
    public static Component formatTime(int timeTicks) {
        float remainingSeconds = (float) timeTicks / 20.0f;
        float remainingDays = timeTicks / 24000.0f;

        MutableComponent timerText = Component.literal("⏳ ");
        Component daysText = Component.translatable("tfc.tooltip.time_delta_days", String.format("%.1f", remainingDays));

        if (remainingDays >= 1.0F) {
            timerText.append(daysText);
        } else {
            timerText.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));
        }
        return timerText;
    }

    /**
     * Quest IDs are longs (64-bit integers). In javascript, everything is a 64-bit FLOAT.
     * So trying to use quest IDs in kubejs rounds them, which we don't want.
     */
    public static String getQuestID(QuestObjectBase quest) {
        return String.valueOf(quest.getId());
    }
}
