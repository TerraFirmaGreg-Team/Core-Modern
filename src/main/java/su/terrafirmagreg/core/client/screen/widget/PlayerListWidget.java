package su.terrafirmagreg.core.client.screen.widget;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

/**
 * Creates a widget that displays a list of players with checkboxes.
 */
@SuppressWarnings("UnusedReturnValue")
public class PlayerListWidget extends GenericScrollableListWidget<PlayerListWidget.PlayerEntry> {

    private ResourceLocation playerHeadBackground;
    private ToIntFunction<RadarGraphWidget.Dataset> playerHeadTintProvider;
    private ResourceLocation checkboxTexture;
    private int checkboxTextureWidth = 0;
    private int checkboxTextureHeight = 0;
    private int playerHeadBackgroundXOffset = 24;
    private int playerHeadBackgroundYOffset = 0;
    private int playerHeadBackgroundWidth = 16;
    private int playerHeadBackgroundHeight = 16;

    private Component hoveredName;
    private int tooltipX, tooltipY;

    public PlayerListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
    }

    public PlayerListWidget setPlayerHeadBackground(ResourceLocation playerHeadBackground) {
        this.playerHeadBackground = playerHeadBackground;
        return this;
    }

    public PlayerListWidget setPlayerHeadTintProvider(ToIntFunction<RadarGraphWidget.Dataset> playerHeadTintProvider) {
        this.playerHeadTintProvider = playerHeadTintProvider;
        return this;
    }

    public PlayerListWidget setPlayerHeadBackgroundBounds(int xOffset, int yOffset, int width, int height) {
        this.playerHeadBackgroundXOffset = xOffset;
        this.playerHeadBackgroundYOffset = yOffset;
        this.playerHeadBackgroundWidth = Math.max(1, width);
        this.playerHeadBackgroundHeight = Math.max(1, height);
        return this;
    }

    public PlayerListWidget setCheckboxTextureOverride(ResourceLocation texture, int textureWidth, int textureHeight) {
        this.checkboxTexture = texture;
        this.checkboxTextureWidth = Math.max(2, textureWidth);
        this.checkboxTextureHeight = Math.max(1, textureHeight);
        return this;
    }

    @Override
    protected void beforeRenderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredName = null;
    }

    @Override
    protected void afterRenderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.hoveredName != null) {
            graphics.renderTooltip(this.minecraft.font, this.hoveredName, this.tooltipX, this.tooltipY);
        }
    }

    public void addPlayer(Component name, UUID uuid, RadarGraphWidget.Dataset dataset1, RadarGraphWidget.Dataset dataset2, boolean visible) {
        this.addEntry(new PlayerEntry(name, uuid, dataset1, dataset2, visible));
    }

    public void clearPlayers() {
        this.clearEntries();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public class PlayerEntry extends ObjectSelectionList.Entry<PlayerEntry> {

        @Getter
        private Component name;
        private final UUID uuid;
        private final Checkbox checkbox;
        private final RadarGraphWidget.Dataset dataset1;
        private final RadarGraphWidget.Dataset dataset2;
        private GameProfile profile;
        private boolean profileResolved = false;
        private boolean resolving = false;

        public PlayerEntry(Component name, UUID uuid, RadarGraphWidget.Dataset dataset1, RadarGraphWidget.Dataset dataset2, boolean visible) {
            this.name = name;
            this.uuid = uuid;
            this.dataset1 = dataset1;
            this.dataset2 = dataset2;
            this.profile = new GameProfile(uuid, name.getString());
            this.checkbox = new Checkbox(0, 0, 20, 20, Component.empty(), visible) {
                @Override
                public void onPress() {
                    super.onPress();
                    boolean selected = this.selected();
                    dataset1.setVisible(selected);
                    dataset2.setVisible(selected);
                }
            };
            dataset1.setVisible(visible);
            dataset2.setVisible(visible);
        }

        private void resolveProfile() {
            if (this.profileResolved || this.resolving) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();
            var connection = minecraft.getConnection();
            if (connection != null) {
                var playerInfo = connection.getPlayerInfo(uuid);
                if (playerInfo != null) {
                    this.profile = playerInfo.getProfile();
                    this.name = Component.literal(this.profile.getName());
                    this.dataset1.setTitle(this.name);
                    this.dataset2.setTitle(this.name);
                    this.profileResolved = true;
                    return;
                }
            }

            this.resolving = true;
            CompletableFuture.runAsync(() -> {
                try {
                    GameProfile filled = minecraft.getMinecraftSessionService().fillProfileProperties(this.profile, true);
                    minecraft.execute(() -> {
                        this.profile = filled;
                        if (this.name.getString().startsWith("Player ")) {
                            this.name = Component.literal(filled.getName());
                            this.dataset1.setTitle(this.name);
                            this.dataset2.setTitle(this.name);
                        }
                        this.profileResolved = true;
                        this.resolving = false;
                    });
                } catch (Exception e) {
                    this.resolving = false;
                }
            }, Util.backgroundExecutor());
        }

        public void setX(int x) {
            this.checkbox.setX(x);
        }

        public void setY(int y) {
            this.checkbox.setY(y);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            resolveProfile();

            int headX = left + 24;
            int headY = top + (height - 16) / 2;

            if (playerHeadBackground != null) {
                int backgroundX = left + playerHeadBackgroundXOffset;
                int backgroundY = headY + playerHeadBackgroundYOffset;
                int color = this.dataset1.getLineColor();
                if (playerHeadTintProvider != null) {
                    color = playerHeadTintProvider.applyAsInt(this.dataset1);
                }
                float alpha = ((color >> 24) & 0xFF) / 255f;
                float red = ((color >> 16) & 0xFF) / 255f;
                float green = ((color >> 8) & 0xFF) / 255f;
                float blue = (color & 0xFF) / 255f;

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.setColor(red, green, blue, Math.max(0.35f, alpha));
                graphics.blit(playerHeadBackground, backgroundX, backgroundY, 0, 0, playerHeadBackgroundWidth, playerHeadBackgroundHeight, playerHeadBackgroundWidth, playerHeadBackgroundHeight);
                graphics.setColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
            }

            checkbox.setX(left);
            checkbox.setY(top + (height - 20) / 2);
            if (checkboxTexture != null) {
                final int frameW = checkboxTextureWidth / 2;
                final int frameH = checkboxTextureHeight;
                final int drawW = checkbox.getWidth();
                final int drawH = checkbox.getHeight();
                final int u = checkbox.selected() ? frameW : 0;

                graphics.blit(checkboxTexture, checkbox.getX(), checkbox.getY(), drawW, drawH, (float) u, 0.0f, frameW, frameH, checkboxTextureWidth, checkboxTextureHeight);

                if (checkbox.active && checkbox.isMouseOver(mouseX, mouseY)) {
                    graphics.fill(checkbox.getX(), checkbox.getY(), checkbox.getX() + checkbox.getWidth(), checkbox.getY() + checkbox.getHeight(), 0x40FFFFFF);
                }

                if (!checkbox.active) {
                    graphics.fill(checkbox.getX(), checkbox.getY(), checkbox.getX() + checkbox.getWidth(), checkbox.getY() + checkbox.getHeight(), 0x80000000);
                }
            } else {
                checkbox.render(graphics, mouseX, mouseY, partialTick);
            }

            Minecraft minecraft = Minecraft.getInstance();
            ResourceLocation skinLocation = DefaultPlayerSkin.getDefaultSkin(uuid);

            var connection = minecraft.getConnection();
            if (connection != null) {
                var playerInfo = connection.getPlayerInfo(uuid);
                if (playerInfo != null) {
                    skinLocation = playerInfo.getSkinLocation();
                    this.name = Component.literal(playerInfo.getProfile().getName());
                    this.profile = playerInfo.getProfile();
                    this.dataset1.setTitle(this.name);
                    this.dataset2.setTitle(this.name);
                    this.profileResolved = true;
                } else {
                    Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(this.profile);
                    if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                        skinLocation = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                    }
                }
            } else {
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(this.profile);
                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    skinLocation = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
            }

            // Render player head.
            graphics.blit(skinLocation, headX, headY, 16, 16, 8.0f, 8.0f, 8, 8, 64, 64); // Inner layer
            graphics.blit(skinLocation, headX, headY, 16, 16, 40.0f, 8.0f, 8, 8, 64, 64); // Outer layer

            if (mouseX >= headX && mouseX <= headX + 16 && mouseY >= headY && mouseY <= headY + 16) {
                // Check if head is visible within the list's vertical bounds
                if (headY >= y0 && headY + 16 <= y1) {
                    PlayerListWidget.this.hoveredName = this.name;
                    PlayerListWidget.this.tooltipX = mouseX;
                    PlayerListWidget.this.tooltipY = mouseY;
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseY < y0 || mouseY > y1) {
                return false;
            }
            return checkbox.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public @NotNull Component getNarration() {
            return name;
        }
    }
}
