
package su.terrafirmagreg.core.client.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

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

/**
 * A spider web /radar graph widget for displaying multiple variables on a regular polygon.
 */
@SuppressWarnings("unused")
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

    // External polygon settings.
    private boolean drawExternalPolygon = true;
    private int externalLineColor = 0xFFAAAAAA;
    private float externalLineThickness = 1.0f;

    // Center lines settings.
    private boolean drawCenterLines = true;
    private int centerLineColor = 0x80AAAAAA;
    private float centerLineThickness = 1.0f;

    // Start offset from center (0-1).
    private float startOffset = 0.0f;

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
        if (variables.isEmpty())
            return;

        int n = variables.size();
        double angleStep = 2 * Math.PI / n;
        double startAngle = -Math.PI / 2;

        // Calculate external polygon vertices.
        cachedExternalVertices = new double[n][2];
        for (int i = 0; i < n; i++) {
            double angle = startAngle + i * angleStep;
            cachedExternalVertices[i][0] = centerX + radius * Math.cos(angle);
            cachedExternalVertices[i][1] = centerY + radius * Math.sin(angle);
        }

        // Calculate value polygon vertices with start offset.
        cachedValueVertices = new double[n][2];
        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            double normalizedValue = var.getNormalizedValue();
            double angle = startAngle + i * angleStep;
            // Apply start offset.
            double effectiveValue = startOffset + normalizedValue * (1.0 - startOffset);
            double valueRadius = radius * effectiveValue;
            cachedValueVertices[i][0] = centerX + valueRadius * Math.cos(angle);
            cachedValueVertices[i][1] = centerY + valueRadius * Math.sin(angle);
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

        // Draw filled value polygon.
        drawFilledPolygon(matrix, cachedValueVertices, fillColor);

        // Draw value polygon outline.
        drawPolygonOutline(matrix, cachedValueVertices, lineColor, lineThickness);

        // Draw variable labels/icons at external vertices.
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            float labelX = (float) cachedLabelPositions[i][0];
            float labelY = (float) cachedLabelPositions[i][1];

            if (var.texture != null) {
                // Draw texture.
                int texSize = var.iconSize;
                graphics.blit(var.texture,
                        (int) labelX - texSize / 2,
                        (int) labelY - texSize / 2,
                        0, 0, texSize, texSize, texSize, texSize);
            } else if (var.label != null) {
                // Draw text label.
                int textWidth = font.width(var.label);
                int textX = (int) labelX - textWidth / 2;
                int textY = (int) labelY - font.lineHeight / 2;
                graphics.drawString(font, var.label, textX, textY, var.labelColor, false);
            }
        }
    }

    /**
     * Get the variable being hovered.
     */
    @Nullable
    public Variable getHoveredVariable(int mouseX, int mouseY) {
        if (variables.isEmpty() || cachedLabelPositions == null)
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

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
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
        public Variable setTexture(ResourceLocation texture, int size) {
            this.texture = texture;
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
