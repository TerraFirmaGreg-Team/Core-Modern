package su.terrafirmagreg.core.client.asphalt;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingMask;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.event.AsphaltRoadSprayEvent;
import su.terrafirmagreg.core.common.data.TFGTags;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
public final class AsphaltRoadSprayClientEvent {
    private static final float ROAD_PREVIEW_Y = 15.03F / 16.0F;
    private static final float SLAB_PREVIEW_Y = 7.03F / 16.0F;
    private static final float GUIDE_LINE_OFFSET_Y = 0.01F / 16.0F;
    private static final float MIN = -0.01F;
    private static final float MAX = 1.01F;

    private AsphaltRoadSprayClientEvent() {
    }

    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) {
            return;
        }

        BlockHitResult hit = event.getTarget();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        AsphaltRoadSprayEvent.SprayAction action = resolvePreviewAction(level, pos, state, player, hit);
        if (action == null || action.solvent()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        float previewY = state.getBlock() instanceof AsphaltRoadSlabBlock ? SLAB_PREVIEW_Y : ROAD_PREVIEW_Y;

        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        renderPreviewMarking(poseStack, event.getMultiBufferSource(), action, previewY);
        renderGuideLines(poseStack, event.getMultiBufferSource(), previewY + GUIDE_LINE_OFFSET_Y);
        poseStack.popPose();
    }

    private static AsphaltRoadSprayEvent.SprayAction resolvePreviewAction(Level level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
        ItemStack main = player.getMainHandItem();
        if (AsphaltRoadSprayEvent.isSprayCan(main)) {
            return AsphaltRoadSprayEvent.resolveSprayAction(level, pos, state, player, InteractionHand.MAIN_HAND, hit.getDirection(), hit);
        }

        ItemStack offhand = player.getOffhandItem();
        if (AsphaltRoadSprayEvent.isSprayCan(offhand) && (main.isEmpty() || main.is(TFGTags.Items.ROAD_MARKING_STENCILS))) {
            return AsphaltRoadSprayEvent.resolveSprayAction(level, pos, state, player, InteractionHand.OFF_HAND, hit.getDirection(), hit);
        }

        return null;
    }

    private static void renderPreviewMarking(PoseStack poseStack, MultiBufferSource bufferSource, AsphaltRoadSprayEvent.SprayAction action, float previewY) {
        if (action.targetColor() == null || action.targetMask().isNone()) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(TFGCore.id("block/asphalt_road/mask_" + action.targetMask().getSerializedName()));
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
        int color = translucentColor(action.targetColor());

        poseStack.pushPose();
        poseStack.translate(0.5F, previewY, 0.5F);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationY(action.targetMask(), action.targetFacing())));
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(buffer, position, normal, MIN, 0.0F, MIN, sprite.getU(0.0F), sprite.getV(0.0F), color);
        vertex(buffer, position, normal, MIN, 0.0F, MAX, sprite.getU(0.0F), sprite.getV(16.0F), color);
        vertex(buffer, position, normal, MAX, 0.0F, MAX, sprite.getU(16.0F), sprite.getV(16.0F), color);
        vertex(buffer, position, normal, MAX, 0.0F, MIN, sprite.getU(16.0F), sprite.getV(0.0F), color);

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, Matrix4f position, Matrix3f normal, float x, float y, float z, float u, float v, int color) {
        buffer.vertex(position, x, y, z)
                .color(color)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static int translucentColor(DyeColor color) {
        return 0x99000000 | color.getTextColor();
    }

    private static float rotationY(AsphaltRoadMarkingMask mask, Direction facing) {
        if (mask.getDirs() == 0) {
            return 0.0F;
        }
        if (mask.getDirs() == 2) {
            return facing.getAxis() == Direction.Axis.X ? 90.0F : 0.0F;
        }
        return switch (facing) {
            case EAST -> 270.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static void renderGuideLines(PoseStack poseStack, MultiBufferSource bufferSource, float topY) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.lineWidth(2.0F);

        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float a = 0.75F;

        line(pose, buffer, MIN, topY, MIN, MAX, topY, MIN, r, g, b, a);
        line(pose, buffer, MAX, topY, MIN, MAX, topY, MAX, r, g, b, a);
        line(pose, buffer, MAX, topY, MAX, MIN, topY, MAX, r, g, b, a);
        line(pose, buffer, MIN, topY, MAX, MIN, topY, MIN, r, g, b, a);
        line(pose, buffer, MIN, topY, MIN, MAX, topY, MAX, r, g, b, a);
        line(pose, buffer, MIN, topY, MAX, MAX, topY, MIN, r, g, b, a);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2,
            float red, float green, float blue, float alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 1.0E-6F) {
            return;
        }
        float nx = dx / length;
        float ny = dy / length;
        float nz = dz / length;
        buffer.vertex(pose.pose(), x1, y1, z1).color(red, green, blue, alpha).normal(pose.normal(), nx, ny, nz).endVertex();
        buffer.vertex(pose.pose(), x2, y2, z2).color(red, green, blue, alpha).normal(pose.normal(), nx, ny, nz).endVertex();
    }
}
