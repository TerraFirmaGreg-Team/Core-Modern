package su.terrafirmagreg.core.common.data.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec3;

public class RailgunBoom extends TextureSheetParticle {

    private final SpriteSet sprites;
    private final int count;
    private final int delay;
    private final float scale;
    private final float gap;

    public RailgunBoom(ClientLevel level, double x, double y, double z,
            double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        this.scale = 1.0f + this.random.nextFloat() * 1.5f;
        this.setSize(scale, scale);
        this.count = 5;
        this.delay = 3;
        this.gap = 3.0f;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        // Static velocity
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.lifetime = count * delay + 5;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return this.scale;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 view = camera.getPosition();
        double baseX = xo + (x - xo) * partialTicks - view.x;
        double baseY = yo + (y - yo) * partialTicks - view.y;
        double baseZ = zo + (z - zo) * partialTicks - view.z;

        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        float a = this.alpha;
        int light = this.getLightColor(partialTicks);

        for (int i = 0; i < count; i++) {
            int localAge = this.age - (i * delay);
            int localLifetime = delay * count;

            if (localAge < 0 || localAge >= localLifetime)
                continue;

            // Set sprite based on local age
            setSpriteFromLocalAge(sprites, localAge, localLifetime);

            float currentScale = this.scale * (this.count - i);
            float half = currentScale / 2f;

            float u0 = this.getU0();
            float u1 = this.getU1();
            float v0 = this.getV0();
            float v1 = this.getV1();

            double yOffset = i * this.gap;
            double px = baseX;
            double py = baseY + yOffset;
            double pz = baseZ;

            // Draw both front and back faces
            addQuad(buffer, px, py, pz, -half, half, u0, u1, v0, v1, r, g, b, a, light);
            addQuad(buffer, px, py, pz, half, -half, u0, u1, v0, v1, r, g, b, a, light); // mirrored
        }
    }

    private void setSpriteFromLocalAge(SpriteSet spriteSet, int localAge, int localLifetime) {
        this.setSprite(spriteSet.get(localAge, localLifetime));
    }

    private void addQuad(VertexConsumer buffer, double x, double y, double z, float leftOffset, float rightOffset,
            float u0, float u1, float v0, float v1,
            float r, float g, float b, float a, int light) {
        buffer.vertex(x + leftOffset, y, z + rightOffset).uv(u0, v0).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(x + leftOffset, y, z + leftOffset).uv(u0, v1).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(x + rightOffset, y, z + leftOffset).uv(u1, v1).color(r, g, b, a).uv2(light).endVertex();
        buffer.vertex(x + rightOffset, y, z + rightOffset).uv(u1, v0).color(r, g, b, a).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
