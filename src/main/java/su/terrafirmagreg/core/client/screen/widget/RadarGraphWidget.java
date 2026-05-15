
package su.terrafirmagreg.core.client.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import lombok.Getter;
import lombok.Setter;

/**
 * A spider web /radar graph widget for displaying multiple variables on a regular polygon.
 */
@SuppressWarnings({ "unused", "UnusedReturnValue", "UnnecessaryLocalVariable" })
public class RadarGraphWidget extends AbstractWidget {

    /**
     *  Get the list of variables.
     */
    @Getter
    private final List<Variable> variables = new ArrayList<>();
    /**
     *  Get the center X position of the graph.
     */
    @Getter
    private final int centerX;
    /**
     *  Get the center Y position of the graph.
     */
    @Getter
    private final int centerY;
    /**
     *  Get the radius of the graph.
     */
    @Getter
    private final int radius;

    // Internal polygon settings.
    private int fillColor = 0x8000FF00;
    private int lineColor = 0xFF00FF00;
    private float lineThickness = 2.0f;

    // Dataset settings for multi-polygon mode.
    private final List<Dataset> datasets = new ArrayList<>();
    private static final int MAX_DATASETS = 8;

    // Gradient mode settings.
    private boolean useGradientFill = false;
    private boolean useGradientOutline = false;
    private int centerColor = 0x80FFFFFF;

    // Radius gradient mode settings.
    private boolean useRadiusGradient = false;
    private int radiusInnerColor = 0xFFFF0000;
    private int radiusMiddleColor = 0xFFFFFF00;
    private int radiusOuterColor = 0xFF00FF00;

    // External polygon settings.
    private boolean drawExternalPolygon = true;
    private int externalLineColor = 0xFFAAAAAA;
    private float externalLineThickness = 1.0f;

    // Circle settings.
    private boolean drawCircle = false;
    private int circleColor = 0xFFFFFFFF;
    private float circleThickness = 1.0f;

    // Center lines settings.
    private boolean drawCenterLines = true;
    private int centerLineColor = 0x80AAAAAA;
    private float centerLineThickness = 1.0f;

    // Central icon settings.
    @Nullable
    private Supplier<ResourceLocation> centralIconTexture = null;
    private int centralIconSize = 16;
    private boolean showCentralIcon = false;

    // Start offset from center (0-1).
    private float startOffset = 0.0f;

    // Base rotation of the graph (in radians).
    private float rotation = 0.0f;

    // Tooltip for the graph itself.
    @Nullable
    private Supplier<List<Component>> graphTooltipSupplier = null;

    // Cached positions for external access.
    private double[][] cachedExternalVertices;
    private double[][] cachedValueVertices;
    private double[][] cachedLabelPositions;

    public RadarGraphWidget(int x, int y, int diameter) {
        super(x, y, diameter, diameter, Component.empty());
        this.radius = diameter / 2;
        this.centerX = x + radius;
        this.centerY = y + radius;
    }

    /**
     * Add a variable to the radar graph.
     */
    public RadarGraphWidget addVariable(Variable variable) {
        this.variables.add(variable);
        return this;
    }

    /**
     * Add a dataset for multi-polygon mode.
     * @param dataset The dataset to add.
     */
    public RadarGraphWidget addDataset(Dataset dataset) {
        if (this.datasets.size() < MAX_DATASETS) {
            this.datasets.add(dataset);
        }
        return this;
    }

    /**
     * Clear all datasets.
     */
    public RadarGraphWidget clearDatasets() {
        this.datasets.clear();
        return this;
    }

    /**
     * Set the fill color and opacity for the internal value polygon.
     * @param color ARGB color value.
     */
    public RadarGraphWidget setFillColor(int color) {
        this.fillColor = color;
        return this;
    }

    /**
     * Set the line color for the internal value polygon outline.
     * @param color ARGB color value.
     */
    public RadarGraphWidget setLineColor(int color) {
        this.lineColor = color;
        return this;
    }

    /**
     * Set the line thickness for the internal value polygon outline.
     * @param thickness Line thickness in pixels.
     */
    public RadarGraphWidget setLineThickness(float thickness) {
        this.lineThickness = thickness;
        return this;
    }

    /**
     * Enable gradient fill mode using per-vertex colors from each Variable.
     * When enabled, the fill will blend from the center color to each vertex's color.
     * The base fillColor is still drawn underneath as a background.
     * @param useGradient Whether to use gradient fill.
     */
    public RadarGraphWidget setUseGradientFill(boolean useGradient) {
        this.useGradientFill = useGradient;
        return this;
    }

    /**
     * Enable gradient outline mode using per-vertex colors from each Variable.
     * When enabled, the outline will blend between adjacent vertex colors.
     * @param useGradient Whether to use gradient outline.
     */
    public RadarGraphWidget setUseGradientOutline(boolean useGradient) {
        this.useGradientOutline = useGradient;
        return this;
    }

    /**
     * Set the center color for vertex gradient fill mode.
     * This is the color at the center of the polygon when using vertex gradient fill.
     * @param color ARGB color value.
     */
    public RadarGraphWidget setCenterColor(int color) {
        this.centerColor = color;
        return this;
    }

    /**
     * Enable radius gradient mode.
     * When enabled, fill color is based on how far each point is from center to edge,
     * using a 3-color gradient: inner -> middle -> outer.
     * This overrides vertex gradient mode when enabled.
     * @param useRadiusGradient Whether to use radius gradient mode.
     */
    public RadarGraphWidget setUseRadiusGradient(boolean useRadiusGradient) {
        this.useRadiusGradient = useRadiusGradient;
        return this;
    }

    /**
     * Set the inner color for radius gradient mode (color at center).
     * @param color ARGB color value.
     */
    public RadarGraphWidget setRadiusInnerColor(int color) {
        this.radiusInnerColor = color;
        return this;
    }

    /**
     * Set the middle color for radius gradient mode (color at 50% radius).
     * @param color ARGB color value.
     */
    public RadarGraphWidget setRadiusMiddleColor(int color) {
        this.radiusMiddleColor = color;
        return this;
    }

    /**
     * Set the outer color for radius gradient mode (color at edge).
     * @param color ARGB color value.
     */
    public RadarGraphWidget setRadiusOuterColor(int color) {
        this.radiusOuterColor = color;
        return this;
    }

    /**
     * Enable/disable drawing the external polygon outline.
     */
    public RadarGraphWidget setDrawExternalPolygon(boolean draw) {
        this.drawExternalPolygon = draw;
        return this;
    }

    /**
     * Set the external polygon line color.
     * @param color ARGB color value.
     */
    public RadarGraphWidget setExternalLineColor(int color) {
        this.externalLineColor = color;
        return this;
    }

    /**
     * Set the external polygon line thickness.
     * @param thickness Line thickness in pixels.
     */
    public RadarGraphWidget setExternalLineThickness(float thickness) {
        this.externalLineThickness = thickness;
        return this;
    }

    /**
     * Enable/disable drawing a circle that matches the external polygon's radius.
     */
    public RadarGraphWidget setDrawCircle(boolean draw) {
        this.drawCircle = draw;
        return this;
    }

    /**
     * Set the circle line color.
     * @param color ARGB color value.
     */
    public RadarGraphWidget setCircleColor(int color) {
        this.circleColor = color;
        return this;
    }

    /**
     * Set the circle line thickness.
     * @param thickness Line thickness in pixels.
     */
    public RadarGraphWidget setCircleThickness(float thickness) {
        this.circleThickness = thickness;
        return this;
    }

    /**
     * Enable/disable drawing lines from center to external vertices.
     */
    public RadarGraphWidget setDrawCenterLines(boolean draw) {
        this.drawCenterLines = draw;
        return this;
    }

    /**
     * Set the center lines color.
     * @param color ARGB color value.
     */
    public RadarGraphWidget setCenterLineColor(int color) {
        this.centerLineColor = color;
        return this;
    }

    /**
     * Set the center line thickness.
     * @param thickness Line thickness in pixels.
     */
    public RadarGraphWidget setCenterLineThickness(float thickness) {
        this.centerLineThickness = thickness;
        return this;
    }

    /**
     * Set the start offset from center.
     * @param offset Value from 0.0 (center) to less than 1.0 (edge).
     *               This creates a "dead zone" in the center where values won't render.
     */
    public RadarGraphWidget setStartOffset(float offset) {
        this.startOffset = Mth.clamp(offset, 0.0f, 0.99f);
        return this;
    }

    /**
     * Set the base rotation of the graph.
     * @param rotation Rotation in degrees.
     */
    public RadarGraphWidget setRotation(float rotation) {
        this.rotation = rotation * (float) (Math.PI / 180.0);
        return this;
    }

    /**
     * Set the central icon texture supplier and size.
     * @param texture Supplier for the texture ResourceLocation.
     * @param size The size of the icon (it will be drawn centered at graph center).
     */
    public RadarGraphWidget setCentralIcon(Supplier<ResourceLocation> texture, int size) {
        this.centralIconTexture = texture;
        this.centralIconSize = size;
        this.showCentralIcon = true;
        return this;
    }

    /**
     * Set whether to show the central icon.
     */
    public RadarGraphWidget setShowCentralIcon(boolean show) {
        this.showCentralIcon = show;
        return this;
    }

    /**
     * Set the tooltip supplier for when hovering over the graph itself.
     */
    public RadarGraphWidget setGraphTooltip(Supplier<List<Component>> tooltipSupplier) {
        this.graphTooltipSupplier = tooltipSupplier;
        return this;
    }

    /**
     * Get the external vertex positions. Array of [n][2] where [i][0] is X and [i][1] is Y.
     */
    public double[] @Nullable [] getExternalVertices() {
        return cachedExternalVertices;
    }

    /**
     * Get the value vertex positions. Array of [n][2] where [i][0] is X and [i][1] is Y.
     */
    public double[] @Nullable [] getValueVertices() {
        return cachedValueVertices;
    }

    /**
     * Get the label positions. Array of [n][2] where [i][0] is X and [i][1] is Y.
     */
    public double[] @Nullable [] getLabelPositions() {
        return cachedLabelPositions;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (variables.size() < 3)
            return;

        int n = variables.size();
        double angleStep = 2 * Math.PI / n;
        double startAngle = -Math.PI / 2 + rotation;

        // Calculate external polygon vertices.
        cachedExternalVertices = new double[n][2];
        for (int i = 0; i < n; i++) {
            double angle = startAngle + i * angleStep;
            cachedExternalVertices[i][0] = centerX + radius * Math.cos(angle);
            cachedExternalVertices[i][1] = centerY + radius * Math.sin(angle);
        }

        // Calculate label positions.
        cachedLabelPositions = new double[n][2];
        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            double angle = startAngle + i * angleStep;
            float labelDistance = radius + var.labelOffset;
            cachedLabelPositions[i][0] = centerX + labelDistance * Math.cos(angle);
            cachedLabelPositions[i][1] = centerY + labelDistance * Math.sin(angle);
        }

        PoseStack pose = graphics.pose();
        Matrix4f matrix = pose.last().pose();

        // Draw center lines.
        if (drawCenterLines) {
            for (int i = 0; i < n; i++) {
                drawLine(matrix, centerX, centerY,
                        (float) cachedExternalVertices[i][0], (float) cachedExternalVertices[i][1],
                        centerLineColor, centerLineThickness);
            }
        }

        // Draw external polygon.
        if (drawExternalPolygon) {
            drawPolygonOutline(matrix, cachedExternalVertices, externalLineColor, externalLineThickness);
        }

        // Draw circle.
        if (drawCircle) {
            drawCircleOutline(matrix, centerX, centerY, radius, circleColor, circleThickness);
        }

        if (!datasets.isEmpty()) {
            // Multi-polygon mode.
            for (Dataset dataset : datasets) {
                if (dataset.isVisible()) {
                    double[][] datasetVertices = new double[n][2];
                    for (int i = 0; i < n; i++) {
                        Variable var = variables.get(i);
                        float rawValue = dataset.getValue(i);
                        float normalizedValue = Mth.clamp((rawValue - var.minValue) / (var.maxValue - var.minValue), 0f, 1f);
                        double angle = startAngle + i * angleStep;
                        double effectiveValue = startOffset + normalizedValue * (1.0 - startOffset);
                        double valueRadius = radius * effectiveValue;
                        datasetVertices[i][0] = centerX + valueRadius * Math.cos(angle);
                        datasetVertices[i][1] = centerY + valueRadius * Math.sin(angle);
                    }
                    drawFilledPolygon(matrix, datasetVertices, dataset.fillColor);
                    drawPolygonOutline(matrix, datasetVertices, dataset.lineColor, lineThickness);
                }
            }
        } else {
            // Single polygon mode.
            cachedValueVertices = new double[n][2];
            for (int i = 0; i < n; i++) {
                Variable var = variables.get(i);
                double normalizedValue = var.getNormalizedValue();
                double angle = startAngle + i * angleStep;
                double effectiveValue = startOffset + normalizedValue * (1.0 - startOffset);
                double valueRadius = radius * effectiveValue;
                cachedValueVertices[i][0] = centerX + valueRadius * Math.cos(angle);
                cachedValueVertices[i][1] = centerY + valueRadius * Math.sin(angle);
            }

            // Draw filled value polygon.
            if (useRadiusGradient) {
                RenderSystem.enableDepthTest();
                Minecraft.getInstance().getMainRenderTarget().enableStencil();
                RenderSystem.clearStencil(0);
                RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                RenderSystem.stencilMask(0xFF);
                RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
                RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                RenderSystem.colorMask(false, false, false, false);

                // Draw the mask into the stencil buffer.
                drawFilledPolygon(matrix, cachedValueVertices, 0xFFFFFFFF);

                // Render the full gradient over the mask.
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.stencilMask(0x00);
                RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
                RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

                // Radius gradient mode: color based on distance from center.
                drawRadiusGradientFilledPolygon(matrix, cachedExternalVertices, radiusInnerColor, radiusMiddleColor, radiusOuterColor);

                GL11.glDisable(GL11.GL_STENCIL_TEST);
                RenderSystem.stencilMask(0xFF);
            } else if (useGradientFill) {
                // Vertex gradient mode: color per vertex.
                int[] vertexColors = new int[n];
                for (int i = 0; i < n; i++) {
                    vertexColors[i] = variables.get(i).getVertexColor();
                }
                drawGradientFilledPolygon(matrix, cachedValueVertices, vertexColors, centerColor);
            } else {
                // Solid fill color.
                drawFilledPolygon(matrix, cachedValueVertices, fillColor);
            }

            // Draw value polygon outline.
            if (useRadiusGradient) {
                // For radius gradient, use outline colors based on normalized values.
                float[] normalizedValues = new float[n];
                for (int i = 0; i < n; i++) {
                    normalizedValues[i] = variables.get(i).getNormalizedValue();
                    normalizedValues[i] = startOffset + normalizedValues[i] * (1.0f - startOffset);
                }
                drawRadiusGradientPolygonOutline(matrix, cachedValueVertices, normalizedValues,
                        radiusInnerColor, radiusMiddleColor, radiusOuterColor, lineThickness);
            } else if (useGradientOutline) {
                int[] vertexColors = new int[n];
                for (int i = 0; i < n; i++) {
                    vertexColors[i] = variables.get(i).getVertexColor();
                }
                drawGradientPolygonOutline(matrix, cachedValueVertices, vertexColors, lineThickness);
            } else {
                drawPolygonOutline(matrix, cachedValueVertices, lineColor, lineThickness);
            }
        }

        // Draw variable labels/icons at external vertices.
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            float labelX = (float) cachedLabelPositions[i][0];
            float labelY = (float) cachedLabelPositions[i][1];

            if (var.texture != null) {
                // Draw texture.
                int texSize = var.iconSize;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.blit(var.texture,
                        (int) labelX - texSize / 2,
                        (int) labelY - texSize / 2,
                        0, 0, texSize, texSize, texSize, texSize);
                RenderSystem.disableBlend();
            } else if (var.label != null) {
                // Draw text label.
                int textWidth = font.width(var.label);
                int textX = (int) labelX - textWidth / 2;
                int textY = (int) labelY - font.lineHeight / 2;
                graphics.drawString(font, var.label, textX, textY, var.labelColor, false);
            }
        }

        // Draw central icon.
        if (showCentralIcon && centralIconTexture != null) {
            ResourceLocation texture = centralIconTexture.get();
            if (texture != null) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.blit(texture,
                        centerX - centralIconSize / 2,
                        centerY - centralIconSize / 2,
                        0, 0, centralIconSize, centralIconSize, centralIconSize, centralIconSize);
                RenderSystem.disableBlend();
            }
        }
    }

    /**
     * Get the variable being hovered.
     */
    @Nullable
    public Variable getHoveredVariable(int mouseX, int mouseY) {
        if (variables.size() < 3 || cachedLabelPositions == null)
            return null;

        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < variables.size(); i++) {
            Variable var = variables.get(i);
            float labelX = (float) cachedLabelPositions[i][0];
            float labelY = (float) cachedLabelPositions[i][1];

            int hitboxSize;
            if (var.texture != null) {
                hitboxSize = var.iconSize;
            } else if (var.label != null) {
                hitboxSize = Math.max(font.width(var.label), font.lineHeight);
            } else {
                continue;
            }

            float halfSize = hitboxSize / 2f + 2;
            if (mouseX >= labelX - halfSize && mouseX <= labelX + halfSize &&
                    mouseY >= labelY - halfSize && mouseY <= labelY + halfSize) {
                return var;
            }
        }
        return null;
    }

    /**
     * Check if mouse is hovering over the graph area.
     */
    public boolean isHoveringGraph(int mouseX, int mouseY) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }

    /**
     * Get tooltip for rendering.
     */
    public Optional<List<Component>> getTooltip(int mouseX, int mouseY) {
        Variable hovered = getHoveredVariable(mouseX, mouseY);
        if (hovered != null && hovered.tooltipSupplier != null) {
            return Optional.of(hovered.tooltipSupplier.get());
        }
        if (isHoveringGraph(mouseX, mouseY) && graphTooltipSupplier != null) {
            return Optional.of(graphTooltipSupplier.get());
        }
        return Optional.empty();
    }

    private void drawLine(Matrix4f matrix, float x1, float y1, float x2, float y2, int color, float thickness) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Calculate perpendicular vector for line thickness.
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.0001f)
            return;

        // Normalize and get perpendicular vector.
        float nx = -dy / length;
        float ny = dx / length;

        // Half thickness offset.
        float halfThickness = thickness / 2.0f;
        float offsetX = nx * halfThickness;
        float offsetY = ny * halfThickness;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Draw quad as a thick line.
        buffer.vertex(matrix, x1 - offsetX, y1 - offsetY, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1 + offsetX, y1 + offsetY, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2 + offsetX, y2 + offsetY, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2 - offsetX, y2 - offsetY, 0).color(r, g, b, a).endVertex();

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Draw a circle outline.
     */
    private void drawCircleOutline(Matrix4f matrix, float x, float y, float radius, int color, float thickness) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int segments = 64;
        float halfThickness = thickness / 2.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float angle = i * (float) (2 * Math.PI) / segments;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);

            float outerX = x + (radius + halfThickness) * cos;
            float outerY = y + (radius + halfThickness) * sin;
            float innerX = x + (radius - halfThickness) * cos;
            float innerY = y + (radius - halfThickness) * sin;

            bufferbuilder.vertex(matrix, outerX, outerY, 0).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(matrix, innerX, innerY, 0).color(r, g, b, a).endVertex();
        }

        tesselator.end();
        RenderSystem.disableBlend();
    }

    /**
     * Draw a closed polygon outline with proper beveled joints at corners.
     */
    private void drawPolygonOutline(Matrix4f matrix, double[][] vertices, int color, float thickness) {
        if (vertices.length < 3)
            return;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int n = vertices.length;
        float halfThickness = thickness / 2.0f;

        // Calculate bevel points for each vertex.
        float[][] outerPoints = new float[n][2];
        float[][] innerPoints = new float[n][2];

        for (int i = 0; i < n; i++) {
            int prev = (i - 1 + n) % n;
            int next = (i + 1) % n;

            float x0 = (float) vertices[prev][0];
            float y0 = (float) vertices[prev][1];
            float x1 = (float) vertices[i][0];
            float y1 = (float) vertices[i][1];
            float x2 = (float) vertices[next][0];
            float y2 = (float) vertices[next][1];

            // Direction vectors.
            float dx1 = x1 - x0;
            float dy1 = y1 - y0;
            float dx2 = x2 - x1;
            float dy2 = y2 - y1;

            // Normalize.
            float len1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
            float len2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
            if (len1 < 0.0001f)
                len1 = 1;
            if (len2 < 0.0001f)
                len2 = 1;
            dx1 /= len1;
            dy1 /= len1;
            dx2 /= len2;
            dy2 /= len2;

            // Perpendicular vectors.
            float nx1 = -dy1;
            float ny1 = dx1;
            float nx2 = -dy2;
            float ny2 = dx2;

            // Average normal for bevel.
            float mx = nx1 + nx2;
            float my = ny1 + ny2;
            float mLen = (float) Math.sqrt(mx * mx + my * my);
            if (mLen < 0.0001f) {
                mx = nx1;
                my = ny1;
                mLen = 1;
            }
            mx /= mLen;
            my /= mLen;

            // Calculate bevel length.
            float dot = nx1 * mx + ny1 * my;
            if (Math.abs(dot) < 0.1f)
                dot = 0.1f;
            float miterLength = halfThickness / dot;

            // Clamp bevel length to prevent spikes at sharp angles.
            float maxMiter = halfThickness * 2.0f;
            if (miterLength > maxMiter)
                miterLength = maxMiter;
            if (miterLength < -maxMiter)
                miterLength = -maxMiter;

            outerPoints[i][0] = x1 + mx * miterLength;
            outerPoints[i][1] = y1 + my * miterLength;
            innerPoints[i][0] = x1 - mx * miterLength;
            innerPoints[i][1] = y1 - my * miterLength;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Draw quads connecting each edge.
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;

            buffer.vertex(matrix, innerPoints[i][0], innerPoints[i][1], 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, outerPoints[i][0], outerPoints[i][1], 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, outerPoints[next][0], outerPoints[next][1], 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, innerPoints[next][0], innerPoints[next][1], 0).color(r, g, b, a).endVertex();
        }

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawFilledPolygon(Matrix4f matrix, double[][] vertices, int color) {
        if (vertices.length < 3)
            return;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Center vertex for triangle fan.
        buffer.vertex(matrix, centerX, centerY, 0).color(r, g, b, a).endVertex();

        // Add all vertices.
        for (double[] vertex : vertices) {
            buffer.vertex(matrix, (float) vertex[0], (float) vertex[1], 0).color(r, g, b, a).endVertex();
        }

        // Close the fan.
        buffer.vertex(matrix, (float) vertices[0][0], (float) vertices[0][1], 0).color(r, g, b, a).endVertex();

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Draw a filled polygon with gradient colors from center to each vertex.
     */
    private void drawGradientFilledPolygon(Matrix4f matrix, double[][] vertices, int[] vertexColors, int centerColorValue) {
        if (vertices.length < 3 || vertexColors.length != vertices.length)
            return;

        float ca = ((centerColorValue >> 24) & 0xFF) / 255f;
        float cr = ((centerColorValue >> 16) & 0xFF) / 255f;
        float cg = ((centerColorValue >> 8) & 0xFF) / 255f;
        float cb = (centerColorValue & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // Draw triangles from center to each edge with gradient.
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int n = vertices.length;
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;

            // Get colors for this vertex and next vertex.
            int color1 = vertexColors[i];
            float a1 = ((color1 >> 24) & 0xFF) / 255f;
            float r1 = ((color1 >> 16) & 0xFF) / 255f;
            float g1 = ((color1 >> 8) & 0xFF) / 255f;
            float b1 = (color1 & 0xFF) / 255f;

            int color2 = vertexColors[next];
            float a2 = ((color2 >> 24) & 0xFF) / 255f;
            float r2 = ((color2 >> 16) & 0xFF) / 255f;
            float g2 = ((color2 >> 8) & 0xFF) / 255f;
            float b2 = (color2 & 0xFF) / 255f;

            buffer.vertex(matrix, centerX, centerY, 0).color(cr, cg, cb, ca).endVertex();
            buffer.vertex(matrix, (float) vertices[i][0], (float) vertices[i][1], 0).color(r1, g1, b1, a1).endVertex();
            buffer.vertex(matrix, (float) vertices[next][0], (float) vertices[next][1], 0).color(r2, g2, b2, a2).endVertex();
        }

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Draw a polygon outline with gradient colors between adjacent vertices.
     */
    private void drawGradientPolygonOutline(Matrix4f matrix, double[][] vertices, int[] vertexColors, float thickness) {
        if (vertices.length < 3 || vertexColors.length != vertices.length)
            return;

        int n = vertices.length;
        float halfThickness = thickness / 2.0f;

        // Calculate bevel points for each vertex.
        float[][] outerPoints = new float[n][2];
        float[][] innerPoints = new float[n][2];

        for (int i = 0; i < n; i++) {
            int prev = (i - 1 + n) % n;
            int next = (i + 1) % n;

            float x0 = (float) vertices[prev][0];
            float y0 = (float) vertices[prev][1];
            float x1 = (float) vertices[i][0];
            float y1 = (float) vertices[i][1];
            float x2 = (float) vertices[next][0];
            float y2 = (float) vertices[next][1];

            // Direction vectors.
            float dx1 = x1 - x0;
            float dy1 = y1 - y0;
            float dx2 = x2 - x1;
            float dy2 = y2 - y1;

            // Normalize.
            float len1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
            float len2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
            if (len1 < 0.0001f)
                len1 = 1;
            if (len2 < 0.0001f)
                len2 = 1;
            dx1 /= len1;
            dy1 /= len1;
            dx2 /= len2;
            dy2 /= len2;

            // Perpendicular vectors.
            float nx1 = -dy1;
            float ny1 = dx1;
            float nx2 = -dy2;
            float ny2 = dx2;

            // Average normal for miter.
            float mx = nx1 + nx2;
            float my = ny1 + ny2;
            float mLen = (float) Math.sqrt(mx * mx + my * my);
            if (mLen < 0.0001f) {
                mx = nx1;
                my = ny1;
                mLen = 1;
            }
            mx /= mLen;
            my /= mLen;

            // Calculate miter length.
            float dot = nx1 * mx + ny1 * my;
            if (Math.abs(dot) < 0.1f)
                dot = 0.1f;
            float miterLength = halfThickness / dot;

            // Clamp miter length.
            float maxMiter = halfThickness * 2.0f;
            if (miterLength > maxMiter)
                miterLength = maxMiter;
            if (miterLength < -maxMiter)
                miterLength = -maxMiter;

            outerPoints[i][0] = x1 + mx * miterLength;
            outerPoints[i][1] = y1 + my * miterLength;
            innerPoints[i][0] = x1 - mx * miterLength;
            innerPoints[i][1] = y1 - my * miterLength;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Draw quads connecting each edge with gradient colors.
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;

            int color1 = vertexColors[i];
            float a1 = ((color1 >> 24) & 0xFF) / 255f;
            float r1 = ((color1 >> 16) & 0xFF) / 255f;
            float g1 = ((color1 >> 8) & 0xFF) / 255f;
            float b1 = (color1 & 0xFF) / 255f;

            int color2 = vertexColors[next];
            float a2 = ((color2 >> 24) & 0xFF) / 255f;
            float r2 = ((color2 >> 16) & 0xFF) / 255f;
            float g2 = ((color2 >> 8) & 0xFF) / 255f;
            float b2 = (color2 & 0xFF) / 255f;

            buffer.vertex(matrix, innerPoints[i][0], innerPoints[i][1], 0).color(r1, g1, b1, a1).endVertex();
            buffer.vertex(matrix, outerPoints[i][0], outerPoints[i][1], 0).color(r1, g1, b1, a1).endVertex();
            buffer.vertex(matrix, outerPoints[next][0], outerPoints[next][1], 0).color(r2, g2, b2, a2).endVertex();
            buffer.vertex(matrix, innerPoints[next][0], innerPoints[next][1], 0).color(r2, g2, b2, a2).endVertex();
        }

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Interpolate between two colors based on a factor.
     */
    private int lerpColor(int color1, int color2, float factor) {
        float a1 = ((color1 >> 24) & 0xFF) / 255f;
        float r1 = ((color1 >> 16) & 0xFF) / 255f;
        float g1 = ((color1 >> 8) & 0xFF) / 255f;
        float b1 = (color1 & 0xFF) / 255f;

        float a2 = ((color2 >> 24) & 0xFF) / 255f;
        float r2 = ((color2 >> 16) & 0xFF) / 255f;
        float g2 = ((color2 >> 8) & 0xFF) / 255f;
        float b2 = (color2 & 0xFF) / 255f;

        float a = a1 + (a2 - a1) * factor;
        float r = r1 + (r2 - r1) * factor;
        float g = g1 + (g2 - g1) * factor;
        float b = b1 + (b2 - b1) * factor;

        return ((int) (a * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    /**
     * Get color for a normalized value using 3-point radius gradient.
     */
    private int getRadiusGradientColor(float normalizedValue, int innerColor, int middleColor, int outerColor) {
        if (normalizedValue <= 0.5f) {
            // Interpolate from inner to middle.
            return lerpColor(innerColor, middleColor, normalizedValue * 2f);
        } else {
            // Interpolate from middle to outer.
            return lerpColor(middleColor, outerColor, (normalizedValue - 0.5f) * 2f);
        }
    }

    /**
     * Draw a filled polygon with radius gradient colors based on normalized values.
     * Uses multiple concentric rings to create a smooth 3-color radial gradient.
     */
    private void drawRadiusGradientFilledPolygon(Matrix4f matrix, double[][] vertices,
            int innerColor, int middleColor, int outerColor) {
        if (vertices.length < 3)
            return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int n = vertices.length;

        // Number of rings for smooth gradient.
        int numRings = 20;

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;

            // Direction vectors from center to each vertex.
            float dx1 = (float) vertices[i][0] - centerX;
            float dy1 = (float) vertices[i][1] - centerY;
            float dx2 = (float) vertices[next][0] - centerX;
            float dy2 = (float) vertices[next][1] - centerY;

            // For each ring segment.
            for (int ring = 0; ring < numRings; ring++) {
                float t0 = (float) ring / numRings;
                float t1 = (float) (ring + 1) / numRings;

                // Calculate positions at this ring level.
                float x0_1 = centerX + dx1 * t0;
                float y0_1 = centerY + dy1 * t0;
                float x1_1 = centerX + dx1 * t1;
                float y1_1 = centerY + dy1 * t1;

                float x0_2 = centerX + dx2 * t0;
                float y0_2 = centerY + dy2 * t0;
                float x1_2 = centerX + dx2 * t1;
                float y1_2 = centerY + dy2 * t1;

                // For the full gradient, value is just the ring ratio.
                float value0 = t0;
                float value1 = t1;

                // Get colors based on radius at each corner.
                int c0 = getRadiusGradientColor(value0, innerColor, middleColor, outerColor);
                int c1 = getRadiusGradientColor(value1, innerColor, middleColor, outerColor);

                // Extract color components.
                float a0 = ((c0 >> 24) & 0xFF) / 255f;
                float r0 = ((c0 >> 16) & 0xFF) / 255f;
                float g0 = ((c0 >> 8) & 0xFF) / 255f;
                float b0 = (c0 & 0xFF) / 255f;

                float a1 = ((c1 >> 24) & 0xFF) / 255f;
                float r1 = ((c1 >> 16) & 0xFF) / 255f;
                float g1 = ((c1 >> 8) & 0xFF) / 255f;
                float b1 = (c1 & 0xFF) / 255f;

                // Draw quad as two triangles.
                buffer.vertex(matrix, x0_1, y0_1, 0).color(r0, g0, b0, a0).endVertex();
                buffer.vertex(matrix, x1_1, y1_1, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(matrix, x0_2, y0_2, 0).color(r0, g0, b0, a0).endVertex();

                // Triangle 2
                buffer.vertex(matrix, x1_1, y1_1, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(matrix, x1_2, y1_2, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(matrix, x0_2, y0_2, 0).color(r0, g0, b0, a0).endVertex();
            }
        }

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Draw a polygon outline with radius gradient colors based on normalized values.
     */
    private void drawRadiusGradientPolygonOutline(Matrix4f matrix, double[][] vertices, float[] normalizedValues,
            int innerColor, int middleColor, int outerColor, float thickness) {
        if (vertices.length < 3 || normalizedValues.length != vertices.length)
            return;

        int n = vertices.length;
        float halfThickness = thickness / 2.0f;

        // Calculate bevel points.
        float[][] outerPoints = new float[n][2];
        float[][] innerPoints = new float[n][2];

        for (int i = 0; i < n; i++) {
            int prev = (i - 1 + n) % n;
            int next = (i + 1) % n;

            float x0 = (float) vertices[prev][0];
            float y0 = (float) vertices[prev][1];
            float x1 = (float) vertices[i][0];
            float y1 = (float) vertices[i][1];
            float x2 = (float) vertices[next][0];
            float y2 = (float) vertices[next][1];

            float dx1 = x1 - x0;
            float dy1 = y1 - y0;
            float dx2 = x2 - x1;
            float dy2 = y2 - y1;

            float len1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
            float len2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
            if (len1 < 0.0001f)
                len1 = 1;
            if (len2 < 0.0001f)
                len2 = 1;
            dx1 /= len1;
            dy1 /= len1;
            dx2 /= len2;
            dy2 /= len2;

            float nx1 = -dy1;
            float ny1 = dx1;
            float nx2 = -dy2;
            float ny2 = dx2;

            float mx = nx1 + nx2;
            float my = ny1 + ny2;
            float mLen = (float) Math.sqrt(mx * mx + my * my);
            if (mLen < 0.0001f) {
                mx = nx1;
                my = ny1;
                mLen = 1;
            }
            mx /= mLen;
            my /= mLen;

            float dot = nx1 * mx + ny1 * my;
            if (Math.abs(dot) < 0.1f)
                dot = 0.1f;
            float miterLength = halfThickness / dot;

            float maxMiter = halfThickness * 2.0f;
            if (miterLength > maxMiter)
                miterLength = maxMiter;
            if (miterLength < -maxMiter)
                miterLength = -maxMiter;

            outerPoints[i][0] = x1 + mx * miterLength;
            outerPoints[i][1] = y1 + my * miterLength;
            innerPoints[i][0] = x1 - mx * miterLength;
            innerPoints[i][1] = y1 - my * miterLength;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;

            int color1 = getRadiusGradientColor(normalizedValues[i], innerColor, middleColor, outerColor);
            float a1 = ((color1 >> 24) & 0xFF) / 255f;
            float r1 = ((color1 >> 16) & 0xFF) / 255f;
            float g1 = ((color1 >> 8) & 0xFF) / 255f;
            float b1 = (color1 & 0xFF) / 255f;

            int color2 = getRadiusGradientColor(normalizedValues[next], innerColor, middleColor, outerColor);
            float a2 = ((color2 >> 24) & 0xFF) / 255f;
            float r2 = ((color2 >> 16) & 0xFF) / 255f;
            float g2 = ((color2 >> 8) & 0xFF) / 255f;
            float b2 = (color2 & 0xFF) / 255f;

            buffer.vertex(matrix, innerPoints[i][0], innerPoints[i][1], 0).color(r1, g1, b1, a1).endVertex();
            buffer.vertex(matrix, outerPoints[i][0], outerPoints[i][1], 0).color(r1, g1, b1, a1).endVertex();
            buffer.vertex(matrix, outerPoints[next][0], outerPoints[next][1], 0).color(r2, g2, b2, a2).endVertex();
            buffer.vertex(matrix, innerPoints[next][0], innerPoints[next][1], 0).color(r2, g2, b2, a2).endVertex();
        }

        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
    }

    /**
     * Represents a dataset for the radar graph.
     */
    public static class Dataset {
        @Getter
        @Setter
        private Component title;
        private final List<Supplier<Float>> values;
        private final int fillColor;
        @Getter
        private final int lineColor;
        @Getter
        @Setter
        private boolean visible = true;

        public Dataset(Component title, List<Supplier<Float>> values, int fillColor, int lineColor) {
            this.title = title;
            this.values = values;
            this.fillColor = fillColor;
            this.lineColor = lineColor;
        }

        public float getValue(int index) {
            if (index >= 0 && index < values.size()) {
                return values.get(index).get();
            }
            return 0f;
        }

        /**
         * Create a dataset with random colors.
         */
        public static Dataset random(Component title, List<Supplier<Float>> values) {
            int r = (int) (Math.random() * 255);
            int g = (int) (Math.random() * 255);
            int b = (int) (Math.random() * 255);
            int fillColor = (0x80 << 24) | (r << 16) | (g << 8) | b;
            int lineColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
            return new Dataset(title, values, fillColor, lineColor);
        }
    }

    /**
     * Represents a variable on the radar graph.
     */
    public static class Variable {
        private final Supplier<Float> valueSupplier;
        /**
         *  Get the minimum value of the range.
         */
        @Getter
        private final float minValue;
        /**
         *  Get the maximum value of the range.
         */
        @Getter
        private final float maxValue;

        @Nullable
        private Component label;
        @Nullable
        private ResourceLocation texture;
        private int iconSize = 16;
        private int labelOffset = 15;
        private int labelColor = 0x404040;
        /**
         *  Get the vertex color for gradient mode.
         */
        @Getter
        private int vertexColor = 0xFFFFFFFF; // Color for gradient mode (ARGB).

        @Nullable
        private Supplier<List<Component>> tooltipSupplier;

        /**
         * Create a variable with a value supplier and range.
         * @param valueSupplier Supplies the current value.
         * @param minValue Minimum value of the range.
         * @param maxValue Maximum value of the range.
         */
        public Variable(Supplier<Float> valueSupplier, float minValue, float maxValue) {
            this.valueSupplier = valueSupplier;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        /**
         * Set a text label for this variable.
         */
        public Variable setLabel(Component label) {
            this.label = label;
            this.texture = null;
            return this;
        }

        /**
         * Set a texture icon for this variable.
         */
        public Variable setTexture(Supplier<ResourceLocation> texture, int size) {
            this.texture = texture.get();
            this.iconSize = size;
            this.label = null;
            return this;
        }

        /**
         * Set the offset distance from the external polygon edge.
         */
        public Variable setLabelOffset(int offset) {
            this.labelOffset = offset;
            return this;
        }

        /**
         * Set the label text color.
         */
        public Variable setLabelColor(int color) {
            this.labelColor = color;
            return this;
        }

        /**
         * Set the vertex color for gradient mode.
         * This color is used when gradient fill or outline is enabled.
         * @param color ARGB color value.
         */
        public Variable setVertexColor(int color) {
            this.vertexColor = color;
            return this;
        }

        /**
         * Set the tooltip supplier for when hovering over this variable.
         */
        public Variable setTooltip(Supplier<List<Component>> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        /**
         * Get the normalized value (0.0 to 1.0) based on the range.
         */
        public float getNormalizedValue() {
            float value = valueSupplier.get();
            return Mth.clamp((value - minValue) / (maxValue - minValue), 0f, 1f);
        }

        /**
         * Get the current raw value.
         */
        public float getValue() {
            return valueSupplier.get();
        }

        /**
         * Get the label component.
         */
        @Nullable
        public Component getLabel() {
            return label;
        }
    }
}
