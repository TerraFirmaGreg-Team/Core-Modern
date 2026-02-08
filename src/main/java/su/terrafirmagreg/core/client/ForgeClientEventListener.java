package su.terrafirmagreg.core.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.events.AdvancedOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.events.NormalOreProspectorEventHelper;
import su.terrafirmagreg.core.common.data.events.OreProspectorEvent;
import su.terrafirmagreg.core.common.data.events.WeakOreProspectorEventHelper;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventListener {

    @SubscribeEvent
    public static void onTooltip(@NotNull ItemTooltipEvent event) {
        var tooltip = event.getToolTip();
        var stack = event.getItemStack();

        // Check Weak helpers
        for (WeakOreProspectorEventHelper helper : OreProspectorEvent.getWeakOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Check Normal helpers
        for (NormalOreProspectorEventHelper helper : OreProspectorEvent.getNormalOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_count")
                        .withStyle(ChatFormatting.YELLOW));
                return;
            }
        }

        // Check Advanced helpers
        for (AdvancedOreProspectorEventHelper helper : OreProspectorEvent.getAdvancedOreProspectorListHelper()) {
            if (stack.is(helper.getItemTag())) {
                // Determine the mode key based on centersOnly
                String modeKey = helper.isCentersOnly()
                        ? "tfg.tooltip.ore_prospector_mode_vein"
                        : "tfg.tooltip.ore_prospector_mode_block";

                tooltip.add(Component.translatable(
                        "tfg.tooltip.ore_prospector_stats",
                        helper.getLength(),
                        (int) (helper.getHalfWidth() * 2),
                        (int) (helper.getHalfHeight() * 2)).withStyle(ChatFormatting.YELLOW));

                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_count")
                        .withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("tfg.tooltip.ore_prospector_xray",
                        Component.translatable(modeKey) // pass the localized "vein" or "per block"
                ).withStyle(ChatFormatting.YELLOW));
                return;
            }
        }
    }


    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    private static void onItemTooltip(ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        final List<Component> text = event.getToolTip();
        if (!stack.isEmpty()) {
            final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
            if (egg != null) {
                egg.addTooltipInfo(text);
                return;
            }

            var foodProperties = stack.getFoodProperties(event.getEntity());
            if (foodProperties != null) {
                foodProperties.getEffects().forEach(effect -> event.getToolTip().add(getTooltip(effect.getFirst())));
            }
        }
    }

    // These are taken from TFC Aged Alcohol

    private static Component getTooltip(MobEffectInstance effectInstance) {
        return Component.literal(effectInstance.getEffect().getDisplayName().getString()
                + displayedPotency(effectInstance.getAmplifier()) + "(" + formatDuration(effectInstance) + ")").withStyle(effectInstance.getEffect().getCategory().getTooltipFormatting());
    }

    private static String displayedPotency(int amplifier) {
        return switch (amplifier + 1) {
            case 2 -> " II ";
            case 3 -> " III ";
            default -> " ";
        };
    }

    private static String formatDuration(MobEffectInstance effect) {
        return StringUtil.formatTickDuration(Mth.floor(effect.getDuration()));
    }

}
