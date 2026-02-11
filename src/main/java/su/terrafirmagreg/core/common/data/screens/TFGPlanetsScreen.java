package su.terrafirmagreg.core.common.data.screens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.scissor.ClosingScissorBox;
import com.teamresourceful.resourcefullib.client.utils.RenderUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.client.events.AdAstraClientEvents;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.components.LabeledImageButton;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.client.utils.DimensionRenderingUtils;
import earth.terrarium.adastra.common.constants.ConstantComponents;
import earth.terrarium.adastra.common.constants.PlanetConstants;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.handlers.base.SpaceStation;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.network.NetworkHandler;
import earth.terrarium.adastra.common.network.messages.ServerboundLandOnSpaceStationPacket;
import earth.terrarium.adastra.common.network.messages.ServerboundLandPacket;
import earth.terrarium.adastra.common.planets.AdAstraData;

//Originally a copy of the AdAstra one,
public class TFGPlanetsScreen extends AbstractContainerScreen<PlanetsMenu> {

    public static final ResourceLocation BUTTON = PlanetsScreen.BUTTON;
    public static final ResourceLocation BACK_BUTTON = PlanetsScreen.BACK_BUTTON;
    public static final ResourceLocation PLUS_BUTTON = PlanetsScreen.PLUS_BUTTON;
    public static final ResourceLocation SELECTION_MENU = PlanetsScreen.SELECTION_MENU;
    public static final ResourceLocation SMALL_SELECTION_MENU = PlanetsScreen.SMALL_SELECTION_MENU;
    private final List<Button> buttons = new ArrayList<>();
    private Button backButton;
    private double scrollAmount;
    private final List<Button> spaceStationButtons = new ArrayList<>();
    private Button addSpaceStatonButton;
    private double spaceStationScrollAmount;
    private final boolean hasMultipleSolarSystems;
    private int pageIndex;
    private @Nullable ResourceLocation selectedSolarSystem;
    private @Nullable Planet selectedPlanet;

    public TFGPlanetsScreen(PlanetsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.selectedSolarSystem = PlanetConstants.SOLAR_SYSTEM;
        this.imageWidth = this.width;
        this.imageHeight = this.height;
        List<Planet> planets = AdAstraData.planets().values().stream().filter((planet) -> !menu.disabledPlanets().contains(planet.dimension().location()))
                .filter((planet) -> menu.tier() >= planet.tier()).toList();
        this.hasMultipleSolarSystems = planets.stream().map(Planet::solarSystem).distinct().count() > 1L;
        this.pageIndex = this.hasMultipleSolarSystems ? 0 : 1;
    }

    protected void init() {
        super.init();
        this.buttons.clear();
        this.spaceStationButtons.clear();
        this.spaceStationScrollAmount = (double) 0.0F;
        switch (this.pageIndex) {
            case 0:
                this.createSolarSystemButtons();
                break;
            case 1:
            case 2:
                this.createPlanetButtons();
                if (this.pageIndex == 2 && this.selectedPlanet != null) {
                    this.createSelectedPlanetButtons();
                }
        }

        this.backButton = (Button) this.addRenderableWidget(new LabeledImageButton(10, this.height / 2 - 85, 12, 12, 0, 12, 12, BACK_BUTTON, 12, 24, (b) -> {
            if (this.pageIndex != 2) {
                this.scrollAmount = (double) 0.0F;
            }

            --this.pageIndex;
            this.rebuildWidgets();
        }));
        this.addSpaceStatonButton = (Button) this.addRenderableWidget(new LabeledImageButton(114, this.height / 2 - 41, 12, 12, 0, 12, 12, PLUS_BUTTON, 12, 24, (b) -> {
            if (this.selectedPlanet != null) {
                int ownedSpaceStationCount = ((PlanetsMenu) this.menu).getOwnedAndTeamSpaceStations(this.selectedPlanet.orbitIfPresent()).size();
                Component name = Component.translatable("text.ad_astra.text.space_station_name", new Object[] { ownedSpaceStationCount + 1 });
                ((PlanetsMenu) this.menu).constructSpaceStation(this.selectedPlanet.dimension(), name);
                this.close();
            }

        }));
        if (this.selectedPlanet != null) {
            this.addSpaceStatonButton.setTooltip(this.getSpaceStationRecipeTooltip(this.selectedPlanet.orbitIfPresent()));
            this.addSpaceStatonButton.active = this.selectedPlanet != null && ((PlanetsMenu) this.menu).canConstruct(this.selectedPlanet.orbitIfPresent())
                    && !((PlanetsMenu) this.menu).isInSpaceStation(this.selectedPlanet.orbitIfPresent());
        }

        this.backButton.visible = this.pageIndex > (this.hasMultipleSolarSystems ? 0 : 1);
        this.addSpaceStatonButton.visible = this.pageIndex == 2 && this.selectedPlanet != null;
    }

    private void createSolarSystemButtons() {
        this.selectedSolarSystem = null;
        List<ResourceLocation> solarSystems = new ArrayList(AdAstraData.solarSystems());
        solarSystems.sort(Comparator.comparing(ResourceLocation::getPath));
        solarSystems.forEach((solarSystem) -> {
            LabeledImageButton button = (LabeledImageButton) this.addWidget(new LabeledImageButton(10, 0, 99, 20, 0, 0, 20, BUTTON, 99, 40, (b) -> {
                this.pageIndex = 1;
                this.selectedSolarSystem = solarSystem;
                this.rebuildWidgets();
            }, Component.translatableWithFallback("solar_system.%s.%s".formatted(solarSystem.getNamespace(), solarSystem.getPath()), title(solarSystem.getPath()))));
            this.buttons.add(button);
        });
    }

    private void createPlanetButtons() {
        for (Planet planet : ((PlanetsMenu) this.menu).getSortedPlanets()) {
            if (!planet.isSpace() && ((PlanetsMenu) this.menu).tier() >= planet.tier() && planet.solarSystem().equals(this.selectedSolarSystem)) {
                this.buttons.add((Button) this.addWidget(new LabeledImageButton(10, 0, 99, 20, 0, 0, 20, BUTTON, 99, 40, (b) -> {
                    this.pageIndex = 2;
                    this.selectedPlanet = planet;
                    this.rebuildWidgets();
                }, ((PlanetsMenu) this.menu).getPlanetName(planet.dimension()))));
            }
        }

    }

    private void createSelectedPlanetButtons() {
        if (this.selectedPlanet != null) {
            BlockPos pos = ((PlanetsMenu) this.menu).getLandingPos(this.selectedPlanet.dimension(), true);
            LabeledImageButton button = (LabeledImageButton) this.addRenderableWidget(
                    new LabeledImageButton(114, this.height / 2 - 77, 99, 20, 0, 0, 20, BUTTON, 99, 40, (b) -> this.land(this.selectedPlanet.dimension()), ConstantComponents.LAND));
            button.setTooltip(
                    Tooltip.create(Component.translatable("tooltip.ad_astra.land", new Object[] { ((PlanetsMenu) this.menu).getPlanetName(this.selectedPlanet.dimension()), pos.getX(), pos.getZ() })
                            .withStyle(ChatFormatting.AQUA)));
            this.addSpaceStationButtons(this.selectedPlanet.orbitIfPresent());
        }
    }

    private void addSpaceStationButtons(ResourceKey<Level> dimension) {
        ((PlanetsMenu) this.menu).getOwnedAndTeamSpaceStations(dimension).forEach((station) -> {
            ChunkPos pos = ((SpaceStation) station.getSecond()).position();
            LabeledImageButton button = (LabeledImageButton) this.addWidget(
                    new LabeledImageButton(114, this.height / 2, 99, 20, 0, 0, 20, BUTTON, 99, 40, (b) -> this.landOnSpaceStation(dimension, pos), ((SpaceStation) station.getSecond()).name()));
            button.setTooltip(this.getSpaceStationLandTooltip(dimension, pos, (String) station.getFirst()));
            this.spaceStationButtons.add(button);
        });
    }

    public Tooltip getSpaceStationLandTooltip(ResourceKey<Level> dimension, ChunkPos pos, String owner) {
        return Tooltip
                .create(CommonComponents
                        .joinLines(
                                new Component[] {
                                        Component
                                                .translatable("tooltip.ad_astra.space_station_land",
                                                        new Object[] { ((PlanetsMenu) this.menu).getPlanetName(dimension), pos.getMiddleBlockX(), pos.getMiddleBlockZ() })
                                                .withStyle(ChatFormatting.AQUA),
                                        Component.translatable("tooltip.ad_astra.space_station_owner", new Object[] { owner }).withStyle(ChatFormatting.GOLD) }));
    }

    public Tooltip getSpaceStationRecipeTooltip(ResourceKey<Level> planet) {
        List<Component> tooltip = new ArrayList();
        BlockPos pos = ((PlanetsMenu) this.menu).getLandingPos(planet, false);
        tooltip.add(Component.translatable("tooltip.ad_astra.construct_space_station_at", new Object[] { ((PlanetsMenu) this.menu).getPlanetName(planet), pos.getX(), pos.getZ() })
                .withStyle(ChatFormatting.AQUA));
        if (!((PlanetsMenu) this.menu).isInSpaceStation(planet) && !((PlanetsMenu) this.menu).isClaimed(planet)) {
            tooltip.add(ConstantComponents.CONSTRUCTION_COST.copy().withStyle(ChatFormatting.AQUA));
            List<Pair<ItemStack, Integer>> ingredients = (List) ((PlanetsMenu) this.menu).ingredients().get(planet);
            if (ingredients == null) {
                return Tooltip.create(CommonComponents.joinLines(tooltip));
            } else {
                for (Pair<ItemStack, Integer> ingredient : ingredients) {
                    ItemStack stack = (ItemStack) ingredient.getFirst();
                    int amountOwned = (Integer) ingredient.getSecond();
                    boolean hasEnough = ((PlanetsMenu) this.menu).player().isCreative() || ((PlanetsMenu) this.menu).player().isSpectator() || amountOwned >= stack.getCount();
                    tooltip.add(Component.translatable("tooltip.ad_astra.requirement", new Object[] { amountOwned, stack.getCount(), stack.getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA) })
                            .copy().withStyle(hasEnough ? ChatFormatting.GREEN : ChatFormatting.RED));
                }

                return Tooltip.create(CommonComponents.joinLines(tooltip));
            }
        } else {
            tooltip.add(ConstantComponents.SPACE_STATION_ALREADY_EXISTS);
            return Tooltip.create(CommonComponents.joinLines(tooltip));
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderButtons(graphics, mouseX, mouseY, partialTick);
        this.backButton.visible = this.pageIndex > (this.hasMultipleSolarSystems ? 0 : 1);
        this.addSpaceStatonButton.visible = this.pageIndex == 2 && this.selectedPlanet != null;
        this.buttons.forEach((button) -> button.active = button.getY() > this.height / 2 - 63 && button.getY() < this.height / 2 + 88);
    }

    private void renderButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int scrollPixels = (int) this.scrollAmount;

        try (ClosingScissorBox ignored = RenderUtils.createScissorBox(Minecraft.getInstance(), graphics.pose(), 0, this.height / 2 - 43, 112, 131)) {
            for (Button button : this.buttons) {
                button.render(graphics, mouseX, mouseY, partialTick);
            }

            for (int i = 0; i < this.buttons.size(); ++i) {
                Button button = (Button) this.buttons.get(i);
                button.setY(i * 24 - scrollPixels + (this.height / 2 - 41));
            }
        }

        if (this.pageIndex == 2 && this.selectedPlanet != null) {
            int spaceStationScrollPixels = (int) this.spaceStationScrollAmount;

            try (ClosingScissorBox ignored = RenderUtils.createScissorBox(Minecraft.getInstance(), graphics.pose(), 112, this.height / 2 - 2, 112, 90)) {
                for (Button button : this.spaceStationButtons) {
                    button.render(graphics, mouseX, mouseY, partialTick);
                }

                for (int i = 0; i < this.spaceStationButtons.size(); ++i) {
                    Button button = (Button) this.spaceStationButtons.get(i);
                    button.setY(i * 24 - spaceStationScrollPixels + this.height / 2);
                }
            }
        }

    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (this.pageIndex == 2) {
            graphics.blit(SELECTION_MENU, 7, this.height / 2 - 88, 0.0F, 0.0F, 209, 177, 209, 177);
            graphics.drawCenteredString(this.font, ConstantComponents.SPACE_STATION, 163, this.height / 2 - 15, 16777215);
        } else {
            graphics.blit(SMALL_SELECTION_MENU, 7, this.height / 2 - 88, 0.0F, 0.0F, 105, 177, 105, 177);
        }

        if (this.pageIndex == 2 && this.selectedPlanet != null) {
            MutableComponent title = Component.translatableWithFallback(
                    "planet.%s.%s".formatted(this.selectedPlanet.dimension().location().getNamespace(), this.selectedPlanet.dimension().location().getPath()),
                    title(this.selectedPlanet.dimension().location().getPath()));
            graphics.drawCenteredString(this.font, title, 57, this.height / 2 - 60, 16777215);
        } else if (this.pageIndex == 1 && this.selectedSolarSystem != null) {
            MutableComponent title = Component.translatableWithFallback("solar_system.%s.%s".formatted(this.selectedSolarSystem.getNamespace(), this.selectedSolarSystem.getPath()),
                    title(this.selectedSolarSystem.getPath()));
            graphics.drawCenteredString(this.font, title, 57, this.height / 2 - 60, 16777215);
        } else {
            graphics.drawCenteredString(this.font, ConstantComponents.CATALOG, 57, this.height / 2 - 60, 16777215);
        }

    }

    public void renderBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, -16776167);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = -this.height; i <= this.width; i += 24) {
            bufferBuilder.vertex((double) i, (double) 0.0F, (double) 0.0F).color(-15784615).endVertex();
            bufferBuilder.vertex((double) (i + this.height), (double) this.height, (double) 0.0F).color(-15784615).endVertex();
        }

        for (int i = this.width + this.height; i >= 0; i -= 24) {
            bufferBuilder.vertex((double) i, (double) 0.0F, (double) 0.0F).color(-15784615).endVertex();
            bufferBuilder.vertex((double) (i - this.height), (double) this.height, (double) 0.0F).color(-15784615).endVertex();
        }

        tessellator.end();
        AdAstraClientEvents.RenderSolarSystemEvent.fire(graphics, this.selectedSolarSystem, this.width, this.height);
    }

    public static void drawCircles(int start, int count, int color, BufferBuilder bufferBuilder, int width, int height) {
        for (int i = 1 + start; i < count + start + 1; ++i) {
            drawCircle(bufferBuilder, (double) ((float) width / 2.0F), (double) ((float) height / 2.0F), (double) (30 * i), 75, color);
        }

    }

    public static void drawCircle(BufferBuilder bufferBuilder, double x, double y, double radius, int sides, int color) {
        for (double r = radius - (double) 0.5F; r <= radius + (double) 0.5F; r += 0.1) {
            for (int i = 0; i < sides; ++i) {
                double angle = (double) i * (double) 2.0F * Math.PI / (double) sides;
                double nextAngle = (double) (i + 1) * (double) 2.0F * Math.PI / (double) sides;
                double x1 = x + r * Math.cos(angle);
                double y1 = y + r * Math.sin(angle);
                double x2 = x + r * Math.cos(nextAngle);
                double y2 = y + r * Math.sin(nextAngle);
                bufferBuilder.vertex(x1, y1, (double) 0.0F).color(color).endVertex();
                bufferBuilder.vertex(x2, y2, (double) 0.0F).color(color).endVertex();
            }
        }

    }

    public boolean isPauseScreen() {
        return true;
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX < (double) 112.0F && mouseX > (double) 6.0F && mouseY > (double) ((float) this.height / 2.0F - 43.0F) && mouseY < (double) ((float) this.height / 2.0F + 88.0F)) {
            this.setScrollAmount(this.scrollAmount - delta * (double) 16.0F / (double) 2.0F);
        } else if (mouseX > (double) 112.0F && mouseX < (double) 224.0F && mouseY > (double) ((float) this.height / 2.0F - 2.0F) && mouseY < (double) ((float) this.height / 2.0F + 88.0F)) {
            this.setSpaceStationScrollAmount(this.spaceStationScrollAmount - delta * (double) 16.0F / (double) 2.0F);
        }

        return true;
    }

    public void onClose() {
        if (this.pageIndex > 0) {
            if (this.pageIndex != 2) {
                this.scrollAmount = (double) 0.0F;
            }

            --this.pageIndex;
            this.rebuildWidgets();
        } else {
            Player player = ((PlanetsMenu) this.menu).player();
            if (!player.isCreative() && !player.isSpectator()) {
                if (!(player.getVehicle() instanceof Rocket)) {
                    super.onClose();
                }
            } else {
                super.onClose();
            }

        }
    }

    protected void close() {
        this.pageIndex = 0;
        this.onClose();
    }

    protected void setScrollAmount(double amount) {
        this.scrollAmount = Mth.clamp(amount, (double) 0.0F, (double) Math.max(0, this.buttons.size() * 24 - 131));
    }

    protected void setSpaceStationScrollAmount(double amount) {
        this.spaceStationScrollAmount = Mth.clamp(amount, (double) 0.0F, (double) Math.max(0, this.spaceStationButtons.size() * 24 - 90));
    }

    public void land(ResourceKey<Level> dimension) {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundLandPacket(dimension, true));
        this.close();
    }

    public void landOnSpaceStation(ResourceKey<Level> dimension, ChunkPos pos) {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundLandOnSpaceStationPacket(dimension, pos));
        this.close();
    }

    public static String title(String string) {
        return WordUtils.capitalizeFully(string.replace("_", " "));
    }

    static {
        AdAstraClientEvents.RenderSolarSystemEvent.register((graphics, solarSystem, width, height) -> {
            if (PlanetConstants.SOLAR_SYSTEM.equals(solarSystem)) {
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                drawCircles(0, 4, -14404997, bufferBuilder, width, height);
                tessellator.end();
                graphics.blit(DimensionRenderingUtils.SUN, width / 2 - 8, height / 2 - 8, 0.0F, 0.0F, 16, 16, 16, 16);
                float rotation = (float) Util.getMillis() / 100.0F;

                for (int i = 1; i < 5; ++i) {
                    graphics.pose().pushPose();
                    graphics.pose().translate((float) width / 2.0F, (float) height / 2.0F, 0.0F);
                    graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation * (float) (5 - i) / 2.0F));
                    graphics.pose().translate((float) (31 * i - 10), 0.0F, 0.0F);
                    graphics.blit((ResourceLocation) DimensionRenderingUtils.SOLAR_SYSTEM_TEXTURES.get(i - 1), 0, 0, 0.0F, 0.0F, 12, 12, 12, 12);
                    graphics.pose().popPose();
                }
            }

        });
        AdAstraClientEvents.RenderSolarSystemEvent.register((graphics, solarSystem, width, height) -> {
            if (PlanetConstants.PROXIMA_CENTAURI.equals(solarSystem)) {
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                drawCircles(1, 1, -16744320, bufferBuilder, width, height);
                tessellator.end();
                graphics.blit(DimensionRenderingUtils.BLUE_SUN, width / 2 - 8, height / 2 - 8, 0.0F, 0.0F, 16, 16, 16, 16);
                float rotation = (float) Util.getMillis() / 100.0F % 360.0F;
                graphics.pose().pushPose();
                graphics.pose().translate((float) width / 2.0F, (float) height / 2.0F, 0.0F);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
                graphics.pose().translate(53.0F, 0.0F, 0.0F);
                graphics.blit(DimensionRenderingUtils.GLACIO, 0, 0, 0.0F, 0.0F, 12, 12, 12, 12);
                graphics.pose().popPose();
            }

        });
    }

}
