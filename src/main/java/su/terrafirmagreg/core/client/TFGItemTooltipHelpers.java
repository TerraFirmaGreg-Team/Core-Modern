package su.terrafirmagreg.core.client;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.util.Drinkable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.capability.ILargeEgg;
import su.terrafirmagreg.core.common.capability.LargeEggCapability;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.event.AdvancedOreProspectorEventHelper;
import su.terrafirmagreg.core.common.event.NormalOreProspectorEventHelper;
import su.terrafirmagreg.core.common.event.OreProspectorEvent;
import su.terrafirmagreg.core.common.event.WeakOreProspectorEventHelper;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class TFGItemTooltipHelpers {
    private static final TagKey<Fluid> TFC_DRINKABLE = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfc", "drinkables"));
    private static final TagKey<Fluid> TFC_AGED_ALCOHOLS = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfcagedalcohol", "aged_alcohols"));
    private static final TagKey<Fluid> TFG_COOLING_DRINK = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfg", "cooling_drinks"));
    private static final TagKey<Fluid> TFG_WARMING_DRINK = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfg", "warming_drinks"));

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

    @SuppressWarnings({ "deprecation", "ConstantConditions" })
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
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

            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(capability -> {
                FluidStack fluidStack = capability.getFluidInTank(0);
                if (fluidStack.isEmpty())
                    return;

                Fluid fluid = fluidStack.getFluid();
                if (fluid == TFGFluids.ASPHALT_MIX.getSource() || fluid == TFGFluids.ASPHALT_MIX.getFlowing()) {
                    text.add(1, Component.translatable("tfg.tooltip.asphalt_mix.pouring",
                            AsphaltRoadHelper.FIELD_POUR_MB));
                    text.add(2, Component.translatable("tfg.tooltip.asphalt_mix.patch",
                            AsphaltRoadHelper.PATCH_POUR_MB));
                    return;
                }

                if (fluid.is(TFC_DRINKABLE) && !fluid.is(TFC_AGED_ALCOHOLS)) {
                    Drinkable drink = Drinkable.get(fluid);
                    if (drink == null)
                        return;

                    drink.getEffects().forEach(effect -> event.getToolTip().add(1, getTooltip(new MobEffectInstance(effect.type(), effect.duration(), effect.amplifier()))));
                    if (fluid.is(TFG_COOLING_DRINK))
                        event.getToolTip().add(1, Component.translatable("tfg.tooltip.cooling_foods"));
                    else if (fluid.is(TFG_WARMING_DRINK))
                        event.getToolTip().add(1, Component.translatable("tfg.tooltip.warming_foods"));
                }
            });
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
