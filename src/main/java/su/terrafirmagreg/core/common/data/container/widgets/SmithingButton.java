package su.terrafirmagreg.core.common.data.container.widgets;

import java.awt.*;

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

public class SmithingButton extends Button {
    public int id;
    @Getter
    private final ResourceLocation texture;
    private final SoundEvent sound;
    private final int texWidth;
    private final int texHeight;

    public SmithingButton(int id, int x, int y, int width, int height, int texWidth, int texHeight, ResourceLocation texture, SoundEvent sound) {
        this(id, x, y, width, height, texWidth, texHeight, texture, sound, (button) -> {
        });
    }

    public SmithingButton(int id, int x, int y, int width, int height, int texWidth, int texHeight, ResourceLocation texture, SoundEvent sound,
            net.minecraft.client.gui.components.Button.OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, RenderHelpers.NARRATION);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.id = id;
        this.texture = texture;
        this.sound = sound;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
        if (this.active) {
            this.visible = false;
            PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(this.id, (CompoundTag) null));
            this.playDownSound(Minecraft.getInstance().getSoundManager());
        }

    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(this.sound, 1.0F));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int x = this.getX();
            int y = this.getY();
            RenderSystem.setShaderTexture(0, this.texture);
            this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + this.height;
            graphics.blit(this.texture, x, y, 0.0F, 0.0F, width, height, texWidth, texHeight);
        }

    }

}
