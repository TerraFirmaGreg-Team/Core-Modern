package su.terrafirmagreg.core.mixins.client.tfc.render;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dries007.tfc.client.render.entity.ThrownJavelinRenderer;
import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.entity.projectile.ILeashedJavelin;

/**
 * Mixin to ThrownJavelinRenderer to render the "rope".
 * Most of the code is copied from the vanilla renderer so idk lol.
 */
@Mixin(value = ThrownJavelinRenderer.class, remap = false)
public abstract class ThrownJavelinRendererMixin extends EntityRenderer<ThrownJavelin> {

    protected ThrownJavelinRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    private static final TagKey<Item> tfg$ROPE = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("forge", "rope"));

    /**
     * Renders the "rope" of the javelin using the leash renderer.
     * @param javelin The javelin entity.
     * @param pitch A float pitch value.
     * @param poseStack The pose stack for rendering transformations.
     * @param buffers The buffer source for rendering.
     * @param light The light level for rendering.
     * @param ci The callback info.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void tfg$renderLeash(ThrownJavelin javelin, float ageInTicks, float pitch, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (javelin instanceof ILeashedJavelin leashed && leashed.tfg$isLeashed()) {
            Entity leasher = leashed.tfg$getLeasher();
            if (leasher != null) {
                tfg$renderLeash(javelin, pitch, poseStack, buffers, leasher, light);
            }
        }
    }

    /**
     * Renders the "rope" of the javelin using the leash renderer.
     * @param javelin The javelin entity.
     * @param pitch A float pitch value.
     * @param poseStack The pose stack for rendering transformations.
     * @param buffers The buffer source for rendering.
     * @param leasher The player holding the javelin.
     * @param light The light level for rendering.
     */
    @Unique
    private void tfg$renderLeash(ThrownJavelin javelin, float pitch, PoseStack poseStack, MultiBufferSource buffers, Entity leasher, int light) {
        poseStack.pushPose();

        Vec3 leasherPos;
        if (leasher instanceof Player player) {
            float yaw = Mth.lerp(pitch, player.yRotO, player.getYRot()) * ((float) Math.PI / 180F);
            float playerPitch = Mth.lerp(pitch, player.xRotO, player.getXRot()) * ((float) Math.PI / 180F);

            // Determine which hand is holding the rope item.
            InteractionHand leadHand = player.getOffhandItem().is(tfg$ROPE) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            HumanoidArm arm = (leadHand == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            float sideMultiplier = (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;

            // Base left-right offset.
            double sideOffset = (double) player.getBbWidth() * 0.6F * sideMultiplier;

            // Base front-back offset.
            double forwardOffset = (double) player.getBbWidth() * 0.40F;

            double rightX = Math.cos(yaw) * sideOffset;
            double rightZ = Math.sin(yaw) * sideOffset;
            double forwardX = -Math.sin(yaw) * forwardOffset;
            double forwardZ = Math.cos(yaw) * forwardOffset;

            // Vertical pitch offset.
            double d4 = Math.sin(playerPitch) * (double) player.getBbWidth() * 0.4F;

            // Combine player position, right offset, and forward offset.
            double x = Mth.lerp(pitch, player.xo, player.getX()) + rightX + forwardX;
            double y = Mth.lerp(pitch, player.yo, player.getY()) + (double) player.getEyeHeight() + d4;
            double z = Mth.lerp(pitch, player.zo, player.getZ()) + rightZ + forwardZ;
            leasherPos = new Vec3(x, y, z);
        } else {
            leasherPos = leasher.getRopeHoldPosition(pitch);
        }

        Vec3 javelinPos = javelin.getPosition(pitch);

        double xRel = leasherPos.x - javelinPos.x;
        double yRel = leasherPos.y - javelinPos.y;
        double zRel = leasherPos.z - javelinPos.z;

        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();

        for (int i = 0; i <= 24; ++i) {
            tfg$addVertexPair(vertexConsumer, matrix4f, (float) xRel, (float) yRel, (float) zRel, light, i, false);
        }
        for (int i = 24; i >= 0; --i) {
            tfg$addVertexPair(vertexConsumer, matrix4f, (float) xRel, (float) yRel, (float) zRel, light, i, true);
        }

        poseStack.popPose();
    }

    /**
     * Adds a vertex pair to the buffer which represents a segment of the rope.
     * @param buffer The vertex consumer for rendering.
     * @param matrix The matrix for rendering transformations.
     * @param x The x-cord of the vertex.
     * @param y The y-cord of the vertex.
     * @param z The z-cord of the vertex.
     * @param light The light level for rendering.
     * @param i The index of the vertex.
     * @param inverse Whether the vertex is inverted.
     */
    @Unique
    private static void tfg$addVertexPair(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int light, int i, boolean inverse) {
        // Rope Scale.
        float size = 0.03F;

        float f1 = 0.5F;
        float f2 = 0.4F;
        float f3 = 0.3F;
        if (i % 2 == 0) {
            f1 *= 0.7F;
            f2 *= 0.7F;
            f3 *= 0.7F;
        }

        float f4 = (float) i / 24.0F;
        float f7 = x * f4;
        float f8 = y * (f4 * f4 + f4) * 0.5F;
        float f9 = z * f4;

        if (!inverse) {
            buffer.vertex(matrix, f7 + 0.0F, f8 + size, f9 + size).color(f1, f2, f3, 1.0F).uv2(light).endVertex();
            buffer.vertex(matrix, f7 + size, f8 + 0.0F, f9 + 0.0F).color(f1, f2, f3, 1.0F).uv2(light).endVertex();
        } else {
            buffer.vertex(matrix, f7 + size, f8 + size, f9 + 0.0F).color(f1, f2, f3, 1.0F).uv2(light).endVertex();
            buffer.vertex(matrix, f7 + 0.0F, f8 + 0.0F, f9 + size).color(f1, f2, f3, 1.0F).uv2(light).endVertex();
        }
    }
}
