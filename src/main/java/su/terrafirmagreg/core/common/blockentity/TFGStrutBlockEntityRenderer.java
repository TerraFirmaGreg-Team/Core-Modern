package su.terrafirmagreg.core.common.blockentity;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import com.cake.struts.compat.flywheel.StrutsFlywheelCompatLoader;
import com.cake.struts.content.StrutModelBuilder;
import com.cake.struts.content.block.StrutBlock;
import com.cake.struts.content.block.StrutBlockEntity;
import com.cake.struts.content.block.StrutBlockEntityRenderer;
import com.cake.struts.mixin.StrutRenderSystemAccessor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;

@SuppressWarnings({ "all" })
public class TFGStrutBlockEntityRenderer extends StrutBlockEntityRenderer {
    public TFGStrutBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StrutBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Block var8 = blockEntity.getBlockState().getBlock();
        if (var8 instanceof StrutBlock strutBlock) {
            if (blockEntity.getLevel() != null) {
                if (StrutsFlywheelCompatLoader.supportsVisualization(blockEntity.getLevel())) {
                    return;
                }

                if (blockEntity.connectionQuadCache == null) {
                    blockEntity.connectionQuadCache = StrutModelBuilder.buildConnectionQuads(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity,
                            strutBlock.getModelType());
                }

                List<BakedQuad> quads = blockEntity.connectionQuadCache;
                if (quads.isEmpty()) {
                    return;
                }

                VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
                Function<Vector3f, Integer> lighter = blockEntity.createLighter();

                for (BakedQuad quad : quads) {
                    putBulkLitData(consumer, poseStack.last(), quad, new float[] { 1.0F, 1.0F, 1.0F, 1.0F }, 1.0F, 1.0F, 1.0F, 1.0F, lighter, packedOverlay, true);
                }

                return;
            }
        }

    }

    private void putBulkLitData(VertexConsumer consumer, PoseStack.Pose p_85988_, BakedQuad quads, float[] p_331397_, float p_85990_, float p_85991_, float p_85992_, float p_331416_,
            Function<Vector3f, Integer> lighter, int p_85993_, boolean p_331268_) {
        int[] vertices = quads.getVertices();
        Vec3i vec3i = quads.getDirection().getNormal();
        Matrix4f matrix4f = p_85988_.pose();
        Vector3f vector3f = p_85988_.normal().transform(new Vector3f((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ()));
        int i = 8;
        int j = vertices.length / 8;
        int k = (int) (p_331416_ * 255.0F);

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int l = 0; l < j; ++l) {
                intbuffer.clear();
                intbuffer.put(vertices, l * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float f3;
                float f4;
                float f5;
                if (p_331268_) {
                    float diffuse = calculateDiffuse(vector3f);
                    float f6 = (float) (bytebuffer.get(12) & 255);
                    float f7 = (float) (bytebuffer.get(13) & 255);
                    float f8 = (float) (bytebuffer.get(14) & 255);
                    f3 = f6 * p_331397_[l] * p_85990_ * diffuse;
                    f4 = f7 * p_331397_[l] * p_85991_ * diffuse;
                    f5 = f8 * p_331397_[l] * p_85992_ * diffuse;
                } else {
                    f3 = p_331397_[l] * p_85990_ * 255.0F;
                    f4 = p_331397_[l] * p_85991_ * 255.0F;
                    f5 = p_331397_[l] * p_85992_ * 255.0F;
                }

                int vertexAlpha = p_331268_ ? (int) (p_331416_ * (float) (bytebuffer.get(15) & 255) / 255.0F * 255.0F) : k;
                int i1 = FastColor.ARGB32.color(vertexAlpha, (int) f3, (int) f4, (int) f5);
                float f10 = bytebuffer.getFloat(16);
                float f9 = bytebuffer.getFloat(20);
                Vector3f worldPos = new Vector3f(f, f1, f2);
                int j1 = consumer.applyBakedLighting((Integer) lighter.apply(worldPos), bytebuffer);
                Vector3f vector3f1 = matrix4f.transformPosition(f, f1, f2, new Vector3f());
                consumer.applyBakedNormals(vector3f, bytebuffer, p_85988_.normal());
                consumer.vertex((double) vector3f1.x(), (double) vector3f1.y(), (double) vector3f1.z()).color(i1 >> 16 & 255, i1 >> 8 & 255, i1 & 255, i1 >> 24 & 255).uv(f10, f9)
                        .overlayCoords(p_85993_).uv2(j1).normal(vector3f.x(), vector3f.y(), vector3f.z()).endVertex();
            }
        }

    }

    private static float calculateDiffuse(Vector3f normal) {
        return calculateDiffuse(normal, StrutRenderSystemAccessor.struts$getShaderLightDirections()[0], StrutRenderSystemAccessor.struts$getShaderLightDirections()[1]);
    }

    private static float calculateDiffuse(Vector3fc normal, Vector3fc lightDir0, Vector3fc lightDir1) {
        float light0 = Math.max(0.0F, lightDir0.dot(normal));
        float light1 = Math.max(0.0F, lightDir1.dot(normal));
        return Math.min(1.0F, (light0 + light1) * 0.6F + 0.4F);
    }
}
