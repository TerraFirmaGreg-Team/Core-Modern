
package su.terrafirmagreg.core.client.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

/**
 * A spider web /radar graph widget for displaying multiple variables on a regular polygon.
 */
public class RadarGraphWidget extends AbstractWidget {

    private final List<Variable> variables = new ArrayList<>();
    private final int centerX;
    private final int centerY;
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

    // Tooltip for the graph itself.
    @Nullable
    private Supplier<List<Component>> graphTooltipSupplier = null;

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
     * Set the center line thickness'.
     */
    public RadarGraphWidget setCenterLineThickness(float thickness) {
        this.centerLineThickness = thickness;
        return this;
    }

    /**
     * Set the tooltip supplier for when hovering over the graph itself.
     */
    public RadarGraphWidget setGraphTooltip(Supplier<List<Component>> tooltipSupplier) {
        this.graphTooltipSupplier = tooltipSupplier;
        return this;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (variables.isEmpty())
            return;

        int n = variables.size();
        double angleStep = 2 * Math.PI / n;
        double startAngle = -Math.PI / 2;

        // Calculate external polygon vertices.
        double[][] externalVertices = new double[n][2];
        for (int i = 0; i < n; i++) {
            double angle = startAngle + i * angleStep;
            externalVertices[i][0] = centerX + radius * Math.cos(angle);
            externalVertices[i][1] = centerY + radius * Math.sin(angle);
        }

        // Calculate value polygon vertices.
        double[][] valueVertices = new double[n][2];
        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            double normalizedValue = var.getNormalizedValue();
            double angle = startAngle + i * angleStep;
            double valueRadius = radius * normalizedValue;
            valueVertices[i][0] = centerX + valueRadius * Math.cos(angle);
            valueVertices[i][1] = centerY + valueRadius * Math.sin(angle);
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();

        // Draw center lines.
        if (drawCenterLines) {
            for (int i = 0; i < n; i++) {
                drawLine(graphics, centerX, centerY,
                        (float) externalVertices[i][0], (float) externalVertices[i][1],
                        centerLineColor, centerLineThickness);
            }
        }

        // Draw external polygon.
        if (drawExternalPolygon) {
            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                drawLine(graphics,
                        (float) externalVertices[i][0], (float) externalVertices[i][1],
                        (float) externalVertices[next][0], (float) externalVertices[next][1],
                        externalLineColor, externalLineThickness);
            }
        }

        // Draw filled value polygon.
        drawFilledPolygon(graphics, valueVertices, fillColor);

        // Draw value polygon outline.
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            drawLine(graphics,
                    (float) valueVertices[i][0], (float) valueVertices[i][1],
                    (float) valueVertices[next][0], (float) valueVertices[next][1],
                    lineColor, lineThickness);
        }

        // Draw variable labels/icons at external vertices.
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            double angle = startAngle + i * angleStep;

            // Position label slightly outside the external polygon.
            float labelDistance = radius + var.labelOffset;
            float labelX = (float) (centerX + labelDistance * Math.cos(angle));
            float labelY = (float) (centerY + labelDistance * Math.sin(angle));

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

        pose.popPose();
    }

    /**
     * Get the variable being hovered.
     */
    @Nullable
    public Variable getHoveredVariable(int mouseX, int mouseY) {
        if (variables.isEmpty())
            return null;

        int n = variables.size();
        double angleStep = 2 * Math.PI / n;
        double startAngle = -Math.PI / 2;
        Font font = Minecraft.getInstance().font;

        for (int i = 0; i < n; i++) {
            Variable var = variables.get(i);
            double angle = startAngle + i * angleStep;
            float labelDistance = radius + var.labelOffset;
            float labelX = (float) (centerX + labelDistance * Math.cos(angle));
            float labelY = (float) (centerY + labelDistance * Math.sin(angle));

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

    private void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color, float thickness) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(thickness);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x1, y1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(x2, y2, 0).color(r, g, b, a).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private void drawFilledPolygon(GuiGraphics graphics, double[][] vertices, int color) {
        if (vertices.length < 3)
            return;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Center vertex for triangle fan.
        buffer.vertex(centerX, centerY, 0).color(r, g, b, a).endVertex();

        // Add all vertices.
        for (double[] vertex : vertices) {
            buffer.vertex(vertex[0], vertex[1], 0).color(r, g, b, a).endVertex();
        }

        // Close the fan.
        buffer.vertex(vertices[0][0], vertices[0][1], 0).color(r, g, b, a).endVertex();

        tesselator.end();
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
        private final float minValue;
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
    }
}
