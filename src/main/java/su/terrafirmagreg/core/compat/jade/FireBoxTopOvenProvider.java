package su.terrafirmagreg.core.compat.jade;

import com.eerussianguy.firmalife.common.recipes.WrappedHeatingRecipe;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxTopOvenBlockEntity;
import su.terrafirmagreg.core.common.item.BakingDishItem;
import su.terrafirmagreg.core.common.recipe.BakingDishRecipe;

public class FireBoxTopOvenProvider implements IBlockComponentProvider {

    public static final FireBoxTopOvenProvider INSTANCE = new FireBoxTopOvenProvider();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "fire_box_top_oven_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof FireBoxTopOvenBlockEntity blockEntity) {

            // Current Temp Display
            float temp = blockEntity.getTemperature();
            MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);

            if (formattedTemp != null) {
                Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp);
                tooltip.add(tempText);
            }

            // Item Tooltips
            if (!Screen.hasShiftDown()) {
                int slots = blockEntity.getInventory().getSlots();
                boolean anyItems = false;
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = blockEntity.getInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        anyItems = true;
                        break;
                    }
                }
                if (anyItems) {
                    tooltip.add(Component.translatable("tfg.tooltip.shift_hint"));
                }
            } else {
                blockEntity.getCapability(Capabilities.ITEM).ifPresent(inv -> {
                    for (int i = 0; i < inv.getSlots(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);

                        if (!stack.isEmpty()) {
                            float itemTemp = HeatCapability.getTemperature(stack);
                            WrappedHeatingRecipe recipe = WrappedHeatingRecipe.getRecipe(stack);
                            float targetTemp = temp;
                            if (recipe != null) {
                                targetTemp = recipe.temperature();
                            } else if (stack.getItem() instanceof BakingDishItem) {
                                BakingDishRecipe bakingRecipe = BakingDishRecipe.getRecipe(stack, accessor.getLevel());
                                if (bakingRecipe != null) {
                                    targetTemp = bakingRecipe.getMinTemp();
                                }
                            }
                            int tempPercentage = targetTemp > 0 ? Mth.clamp(Math.round((itemTemp / targetTemp) * 20f) * 5, 0, 100) : 0;
                            Component tempComponent = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(itemTemp);
                            MutableComponent line;

                            if (temp > 0 && tempComponent != null) {
                                line = Component.translatable("firmalife.jade.cook_left_temp", stack.getHoverName(), String.format("%s%%", tempPercentage), tempComponent);

                                // Add "- Danger!!" tooltip if near recipe temp and there is no output (Item Burning).
                                if (recipe != null && itemTemp > (recipe.temperature() * 0.8f) && recipe.assemble(new ItemStackInventory(stack), accessor.getLevel().registryAccess()).isEmpty()) {
                                    line.append(Component.translatable("tfc.tooltip.danger").withStyle(ChatFormatting.WHITE));
                                }
                                tooltip.add(line);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
