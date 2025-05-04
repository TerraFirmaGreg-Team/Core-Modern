package su.terrafirmagreg.core.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dries007.tfc.client.RenderHelpers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class TFGClientHelpers {
    /**
     * Просто скопированный метод из RenderHelper.java (TFC) + добавленный аргумент для цвета.
     * */
    public static void renderTexturedCuboid(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int packedLight, int packedOverlay, AABB bounds, int color1, int color2)
    {
        renderTexturedCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, (float) bounds.minX, (float) bounds.minY, (float) bounds.minZ, (float) bounds.maxX, (float) bounds.maxY, (float) bounds.maxZ, color1, color2);
    }

    /**
     * Просто скопированный метод из RenderHelper.java (TFC) + добавленный аргумент для цвета.
     * */
    public static void renderTexturedCuboid(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int packedLight, int packedOverlay, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color1, int color2)
    {
        renderTexturedCuboid(poseStack, buffer, sprite, packedLight, packedOverlay, minX, minY, minZ, maxX, maxY, maxZ, 16f * (maxX - minX), 16f * (maxY - minY), 16f * (maxZ - minZ), color1, color2);
    }

    /**
     * Просто скопированный метод из RenderHelper.java (TFC) + добавленный аргумент для цвета.
     * */
    public static void renderTexturedCuboid(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int packedLight, int packedOverlay, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float xPixels, float yPixels, float zPixels, int color1, int color2)
    {
        renderTexturedQuads(poseStack, buffer, sprite, packedLight, packedOverlay, RenderHelpers.getXVertices(minX, minY, minZ, maxX, maxY, maxZ), zPixels, yPixels, 1, 0, 0, color1, color2);
        renderTexturedQuads(poseStack, buffer, sprite, packedLight, packedOverlay, RenderHelpers.getYVertices(minX, minY, minZ, maxX, maxY, maxZ), zPixels, xPixels, 0, 1, 0, color1, color2);
        renderTexturedQuads(poseStack, buffer, sprite, packedLight, packedOverlay, RenderHelpers.getZVertices(minX, minY, minZ, maxX, maxY, maxZ), xPixels, yPixels, 0, 0, 1, color1, color2);
    }

    /**
     * Просто скопированный метод из RenderHelper.java (TFC) + добавленный аргумент для цвета.
     * */
    public static void renderTexturedTrapezoidalCuboid(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int packedLight, int packedOverlay, float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY, float xPixels, float yPixels, float zPixels, boolean invertNormal, int color1, int color2)
    {
        renderTexturedQuads(poseStack, buffer, sprite, packedLight, packedOverlay, RenderHelpers.getTrapezoidalCuboidXVertices(pMinX, pMaxX, pMinZ, pMaxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY), zPixels, yPixels, invertNormal ? 0 : 1, 0, invertNormal ? 1 : 0, color1, color2);
        renderTexturedQuads(poseStack, buffer, sprite, packedLight, packedOverlay, RenderHelpers.getTrapezoidalCuboidYVertices(pMinX, pMaxX, pMinZ, pMaxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY), zPixels, xPixels, 0, 1, 0, color1, color2);
        renderTexturedQuads(poseStack, buffer, sprite, packedLight, packedOverlay, RenderHelpers.getTrapezoidalCuboidZVertices(pMinX, pMaxX, pMinZ, pMaxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY), xPixels, yPixels, invertNormal ? 1 : 0, 0, invertNormal ? 0 : 1, color1, color2);
    }

    /**
     * Просто скопированный метод из RenderHelper.java (TFC) + добавленный аргумент для цвета.
     * */
    public static void renderTexturedQuads(PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int packedLight, int packedOverlay, float[][] vertices, float uSize, float vSize, float normalX, float normalY, float normalZ, int color1, int color2)
    {
        for (float[] v : vertices)
        {
            renderTexturedVertex(poseStack, buffer, packedLight, packedOverlay, v[0], v[1], v[2], sprite.getU(v[3] * uSize), sprite.getV(v[4] * vSize), v[5] * normalX, v[5] * normalY, v[5] * normalZ, color1, color2);
        }
    }

    /**
     * Просто скопированный метод из RenderHelper.java (TFC) + добавленный аргумент для цвета.
     * airrice: Updated this to also use the side shading (TFC)
     * */
    public static void renderTexturedVertex(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int color1, int color2)
    {
        final int pColor =  increaseBrightness(FastColor.ARGB32.multiply(color1, color2), 90);
        final int shadeToInt = (int)(RenderHelpers.getShade(normalX, normalY, normalZ) * 255);
        final int pColor2 = FastColor.ARGB32.multiply(pColor, FastColor.ARGB32.color(255, shadeToInt,shadeToInt,shadeToInt));
        buffer.vertex(poseStack.last().pose(), x, y, z)
                .color(pColor2)
                .uv(u, v)
                .uv2(packedLight)
                .overlayCoords(packedOverlay)
                .normal(poseStack.last().normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    /**
     * Увеличивает яркость у передаваемого цвета ARGB.
     * */
    public static int increaseBrightness(int argbValue, int increment) {
        // Получаем составляющие ARGB значения
        int alpha = (argbValue >> 24) & 0xFF;
        int red = (argbValue >> 16) & 0xFF;
        int green = (argbValue >> 8) & 0xFF;
        int blue = argbValue & 0xFF;

        // Увеличиваем яркость для каждой компоненты цвета
        red = Math.min(255, red + increment);
        green = Math.min(255, green + increment);
        blue = Math.min(255, blue + increment);

        // Собираем новое ARGB значение
        return (alpha << 24) | (0xFF000000 | (red << 16) | (green << 8) | blue);
    }
}
