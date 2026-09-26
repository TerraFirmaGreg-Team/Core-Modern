/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.client.screen.button.PlayerInventoryTabButton;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.compat.patchouli.PatchouliIntegration;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.TemperatureDisplayStyle;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.SwitchInventoryTabPacket;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.dries007.tfc.util.climate.OverworldClimateModel;

public class ClimateScreen extends TFCContainerScreen<Container>
{
    public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/player_climate.png");

    public ClimateScreen(Container container, Inventory playerInv, Component name)
    {
        super(container, playerInv, name, BACKGROUND);
    }

    @Override
    public void init()
    {
        super.init();

        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.INVENTORY, button -> {
            playerInventory.player.containerMenu = playerInventory.player.inventoryMenu;
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode != null && mc.gameMode.isServerControlledInventory() && mc.player != null) {
                mc.setScreen(new CreativeModeInventoryScreen(playerInventory.player, mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get()));
            } else {
                mc.setScreen(new InventoryScreen(playerInventory.player));
            }
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new SwitchInventoryTabPacket(PlayerInventoryTabButton.Tab.INVENTORY));
        }));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.CALENDAR));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.NUTRITION));
        addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, true, false, PlayerInventoryTabButton.Tab.CLIMATE, button -> {}));
        PatchouliIntegration.ifEnabled(() -> addRenderableWidget(new PlayerInventoryTabButton(leftPos, topPos, false, false, PlayerInventoryTabButton.Tab.BOOK)));
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY)
    {
        super.renderLabels(stack, mouseX, mouseY);

        // Climate at the current player
        final float averageTemp = ClimateRenderCache.INSTANCE.getAverageTemperature();
        final float rainfall = ClimateRenderCache.INSTANCE.getRainfall();
        final float currentTemp = ClimateRenderCache.INSTANCE.getTemperature();

        final TemperatureDisplayStyle style = TFCConfig.CLIENT.climateTooltipStyle.get();

        drawCenteredLine(stack, Component.translatable("tfc.tooltip.climate_koppen_climate_classification", Helpers.translateEnum(KoppenClimateClassification.classify(averageTemp, rainfall))), 16);
        drawCenteredLine(stack, Component.translatable("tfc.tooltip.climate_average_temperature", style.format(averageTemp, true)), 27);
        drawCenteredLine(stack, Component.translatable("tfc.tooltip.climate_annual_rainfall", String.format("%.1f", rainfall)), 38);
        drawCenteredLine(stack, Component.translatable("tfc.tooltip.climate_current_temp", style.format(currentTemp, true)), 49);

        renderHemisphere(stack);
    }

    private void renderHemisphere(GuiGraphics stack)
    {
        final Player player = ClientHelpers.getPlayer();
        if (player == null || player.level().dimension() != Level.OVERWORLD)
        {
            return;
        }
        if (!(Climate.model(player.level()) instanceof OverworldClimateModel model))
        {
            return;
        }

        final String hemisphere = OverworldClimateModel.getInNorthernHemisphere(player.getBlockZ(), model.getTemperatureScale())
            ? "tfc.tooltip.hemisphere_northern"
            : "tfc.tooltip.hemisphere_southern";
        drawCenteredLine(stack, Component.translatable("tfc.tooltip.climate_hemisphere", Component.translatable(hemisphere)), 60);
    }
}