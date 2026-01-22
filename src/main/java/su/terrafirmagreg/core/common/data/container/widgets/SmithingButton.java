package su.terrafirmagreg.core.common.data.container.widgets;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.PacketDistributor;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.recipes.ArtisanType;

public class SmithingButton extends Button {
    public int id;
    @Getter
    private final ResourceLocation texture;
    @Nullable
    @Getter
    private final ResourceLocation inactiveTexture;
    @Getter
    private final ArtisanType currentType;
    private final SoundEvent sound;
    private final int texWidth;
    private final int texHeight;

    public SmithingButton(int id, ArtisanType type, int x, int y, int width, int height, int texWidth, int texHeight, ResourceLocation texture, ResourceLocation inactiveTexture, SoundEvent sound) {
        this(id, type, x, y, width, height, texWidth, texHeight, texture, inactiveTexture, sound, (button) -> {
        });
    }

    public SmithingButton(int id, ArtisanType type, int x, int y, int width, int height, int texWidth, int texHeight, ResourceLocation texture, ResourceLocation inactiveTexture, SoundEvent sound,
            net.minecraft.client.gui.components.Button.OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, RenderHelpers.NARRATION);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.id = id;
        this.texture = texture;
        this.inactiveTexture = inactiveTexture;
        this.sound = sound;
        this.currentType = type;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
        if (this.active) {
            activateButton();
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(this.id, (CompoundTag) null));
            this.playDownSound(Minecraft.getInstance().getSoundManager());
        }

    }

    public void activateButton() {
        if (inactiveTexture == null) {
            this.visible = false;
        }
        this.active = false;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(this.sound, 1.0F));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible)
            return;

        int x = this.getX();
        int y = this.getY();
        if (this.active) {
            RenderSystem.setShaderTexture(0, this.texture);
            this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + this.height;
            graphics.blit(this.texture, x, y, 0.0F, 0.0F, width, height, texWidth, texHeight);
            if (this.isHovered) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
            }
        } else if (inactiveTexture != null) {
            RenderSystem.setShaderTexture(0, this.inactiveTexture);
            graphics.blit(this.inactiveTexture, x, y, 0.0F, 0.0F, width, height, texWidth, texHeight);
        }

    }

}
