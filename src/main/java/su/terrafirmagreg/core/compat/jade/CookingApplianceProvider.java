package su.terrafirmagreg.core.compat.jade;

import net.dries007.tfc.config.TFCConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.recipe.CookingApplianceRecipe;

public class CookingApplianceProvider implements IBlockComponentProvider {

    public static final CookingApplianceProvider INSTANCE = new CookingApplianceProvider();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "cooking_appliance_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof CookingApplianceBlockEntity appliance) {

            // Temp display.
            float temp = appliance.getTemperature();
            MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
            if (formattedTemp == null) {
                formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
            }
            if (formattedTemp != null) {
                Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp);
                tooltip.add(tempText);
            }

            // Cooking timer.
            CookingApplianceRecipe recipe = appliance.getCachedRecipe();
            if (recipe != null && appliance.getCookingTicks() > 0) {
                int remainingTicks = Math.max(0, appliance.getMaxDuration() - appliance.getCookingTicks());
                float remainingSeconds = (float) remainingTicks / 20.0f;

                MutableComponent timerIcon = ((int) remainingSeconds % 2 != 0) ? Component.literal("⏳ ") : Component.literal("⌛ ");
                Component timerText = timerIcon.append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", remainingSeconds)));

                tooltip.add(timerText);
            // Errors
            } else if (appliance.getRecipeError() != CookingApplianceBlockEntity.CookingError.NONE) {
                tooltip.add(appliance.getRecipeError().getTooltipComponent());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
