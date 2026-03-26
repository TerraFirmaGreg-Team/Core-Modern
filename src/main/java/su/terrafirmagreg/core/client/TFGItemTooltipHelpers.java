package su.terrafirmagreg.core.client;

import java.util.List;

import javax.annotation.Nullable;

import net.dries007.tfc.util.Drinkable;
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
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.capability.ILargeEgg;
import su.terrafirmagreg.core.common.capability.LargeEggCapability;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class TFGItemTooltipHelpers {
    private static final TagKey<Fluid> TFC_DRINKABLE_TAG = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfc", "drinkables"));

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
                if (fluidStack.getFluid().is(TFC_DRINKABLE_TAG) && !ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).getNamespace().equals("tfcagedalcohol")) {
                    Drinkable drink = Drinkable.get(fluidStack.getFluid());
                    if (drink != null) {
                        drink.getEffects().forEach(effect -> event.getToolTip().add(getTooltip(new MobEffectInstance(effect.type(), effect.duration(), effect.amplifier()))));
                    }
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
