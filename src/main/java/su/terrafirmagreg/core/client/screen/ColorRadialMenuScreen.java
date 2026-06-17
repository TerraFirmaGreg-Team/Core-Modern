package su.terrafirmagreg.core.client.screen;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.SelectColorPacket;

public class ColorRadialMenuScreen extends Screen {

    private final InteractionHand hand;

    private static final int RADIUS = 85;
    private static final int INNER_RADIUS = 20;
    private static final int ITEM_RADIUS = 60;
    private static final int BTN_WIDTH = 110;
    private static final int BTN_HEIGHT = 20;

    private static final int COLOR_SOLVENT_HOVER = 0xFFFF55;
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final int COLOR_DOTS = 0xAAFFFFFF;
    private static final int COLOR_SEGMENT_HOVER = 0x44FFFFFF;
    private static final int COLOR_BTN_BG = 0xAA000000;
    private static final int COLOR_BTN_BG_HOVER = 0xAA333333;
    private static final int COLOR_BTN_BORDER = 0xFF00FF00;

    public ColorRadialMenuScreen(InteractionHand hand) {
        super(Component.translatable("gui.tfg.color_select.title"));
        this.hand = hand;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        DyeColor[] colors = DyeColor.values();
        int numSegments = colors.length;
        float segmentAngle = 360.0f / numSegments;

        double distToCenter = Math.sqrt(Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));
        double mouseAngle = Math.toDegrees(Math.atan2(mouseY - centerY, mouseX - centerX));
        mouseAngle = (mouseAngle + 360 + 90) % 360;

        boolean hoveringSolvent = distToCenter < INNER_RADIUS;

        int solventColor = hoveringSolvent ? COLOR_SOLVENT_HOVER : COLOR_WHITE;
        Component solventText = Component.translatable("behaviour.paintspray.solvent.short");
        guiGraphics.drawCenteredString(this.font, solventText, centerX, centerY - 4, solventColor);

        for (int i = 0; i < numSegments; i++) {
            float startAngleDeg = i * segmentAngle;
            float endAngleDeg = (i + 1) * segmentAngle;
            boolean hoveringThis = !hoveringSolvent && distToCenter <= RADIUS && distToCenter > INNER_RADIUS &&
                    mouseAngle >= startAngleDeg && mouseAngle < endAngleDeg;

            float midAngleRad = (float) Math.toRadians(startAngleDeg - 90);
            float itemAngleRad = (float) Math.toRadians(((startAngleDeg + endAngleDeg) / 2.0f) - 90);

            int x1 = centerX + (int) (Mth.cos(midAngleRad) * INNER_RADIUS);
            int y1 = centerY + (int) (Mth.sin(midAngleRad) * INNER_RADIUS);
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, COLOR_DOTS);

            int itemX = centerX + (int) (Mth.cos(itemAngleRad) * ITEM_RADIUS) - 8;
            int itemY = centerY + (int) (Mth.sin(itemAngleRad) * ITEM_RADIUS) - 8;

            if (hoveringThis) {
                RenderSystem.setShaderColor(1, 1, 1, 0.2f);
                guiGraphics.fill(itemX - 4, itemY - 4, itemX + 20, itemY + 20, COLOR_SEGMENT_HOVER);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                guiGraphics.renderTooltip(this.font,
                        Component.translatable("color.minecraft." + colors[i].getSerializedName()), mouseX, mouseY);
            }

            ItemStack dyeStack = new ItemStack(getDyeItem(colors[i]));
            guiGraphics.renderFakeItem(dyeStack, itemX, itemY);
        }

        int buttonX = centerX - (BTN_WIDTH / 2);
        int buttonY = centerY + RADIUS + 15;
        boolean hoveringCustom = mouseX >= buttonX && mouseX <= buttonX + BTN_WIDTH && mouseY >= buttonY &&
                mouseY <= buttonY + BTN_HEIGHT;

        guiGraphics.fill(buttonX, buttonY, buttonX + BTN_WIDTH, buttonY + BTN_HEIGHT, hoveringCustom ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG);
        guiGraphics.renderOutline(buttonX, buttonY, BTN_WIDTH, BTN_HEIGHT, COLOR_BTN_BORDER);

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.tfg.color_select.chromatic_btn"), centerX, buttonY + 6, COLOR_WHITE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int buttonX = centerX - (BTN_WIDTH / 2);
        int buttonY = centerY + RADIUS + 15;

        if (mouseX >= buttonX && mouseX <= buttonX + BTN_WIDTH && mouseY >= buttonY && mouseY <= buttonY + BTN_HEIGHT) {
            if (this.minecraft != null) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.minecraft.setScreen(new ChromaticEffectSelectScreen(this.hand));
            }
            return true;
        }
        double distToCenter = Math.sqrt(Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));

        if (distToCenter < INNER_RADIUS) {
            sendColorSelection(-1);
            return true;
        }

        if (distToCenter <= RADIUS) {
            double angle = Math.toDegrees(Math.atan2(mouseY - centerY, mouseX - centerX));
            angle = (angle + 360 + 90) % 360;

            DyeColor[] colors = DyeColor.values();
            int selectedSegment = (int) (angle / (360.0f / colors.length));

            if (selectedSegment >= 0 && selectedSegment < colors.length) {
                sendColorSelection(selectedSegment);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendColorSelection(int id) {
        TFGNetworkHandler.INSTANCE.sendToServer(new SelectColorPacket(hand, id));
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.onClose();
    }

    private Item getDyeItem(DyeColor color) {
        ResourceLocation id = new ResourceLocation("minecraft", color.getSerializedName() + "_dye");
        return BuiltInRegistries.ITEM.get(id);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
