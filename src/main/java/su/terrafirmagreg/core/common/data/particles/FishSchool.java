package su.terrafirmagreg.core.common.data.particles;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

/**
 * A particle that orbits around a center point, simulating a fish swimming in a school.
 * The fish orbits in a circular path with a random radius.
 * The fish quad is oriented to face along the tangent of the orbit path and rotated 45 degrees.
 */
public class FishSchool extends TextureSheetParticle {

    // Center coordinates of the orbit
    private final double cx;
    private final double cz;

    private final float radius;
    private final float angularSpeed;
    private float angle;

    private final TextureAtlasSprite sprite;
    private final float scale;

    /**
     * Constructs a FishSchool particle.
     *
     * @param level        Client instance.
     * @param x            The initial X position.
     * @param y            The initial Y position.
     * @param z            The initial Z position.
     * @param sprite       The texture sprite for the particle.
     * @param radius       The radius of the orbit.
     * @param angularSpeed The angular speed of the orbit.
     */
    public FishSchool(ClientLevel level, double x, double y, double z,
            TextureAtlasSprite sprite, float radius, float angularSpeed) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.cx = x;
        this.cz = z;
        this.radius = radius;
        this.angularSpeed = angularSpeed;
        this.angle = level.random.nextFloat() * (float) (Math.PI * 2.0);
        this.sprite = sprite;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        this.quadSize = 0.5f;

        // Random scale multiplier between 0.8 and 1.1
        this.scale = 0.8f + level.random.nextFloat() * 0.3f;

        this.setSprite(sprite);

        // Calculate the lifetime to ensure one full revolution.
        this.lifetime = Math.max(1, (int) Math.ceil((Math.PI * 2.0) / angularSpeed));

        this.x = cx + radius * Math.cos(angle);
        this.y = y;
        this.z = cz + radius * Math.sin(angle);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    /**
     * Sets the particle alpha.
     *
     * @param a Alpha value.
     */
    public void withAlpha(float a) {
        this.alpha = a;
    }

    /**
     * Updates the particle's position and state each tick.
     * Ensures smooth movement by updating the previous position.
     */
    @Override
    public void tick() {
        // Update previous position to avoid flicker.
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Remove the particle if its lifetime is over.
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }

        // Update the angle and position along the orbit.
        angle += angularSpeed;
        this.x = cx + radius * Math.cos(angle);
        this.z = cz + radius * Math.sin(angle);
    }

    /**
     * Renders the particle.
     *
     * @param buffer       The vertex buffer.
     * @param camera       The camera.
     * @param partialTicks The partial tick time for interpolation.
     */
    @Override
    public void render(@NotNull VertexConsumer buffer, Camera camera, float partialTicks) {
        float a = angle;

        // Calculate the tangent direction.
        float rX = Mth.sin(a);
        float rY = 0.0f;
        float rZ = -Mth.cos(a);

        float uX = 0.0f;
        float uY = 1.0f;
        float uZ = 0.0f;

        // Rotate the texture plane by -45 degrees so the fishy is angled correctly.
        float theta = -(Mth.PI / 4.0f);
        float c = Mth.cos(theta);
        float s = Mth.sin(theta);

        float rXp = rX * c + uX * s;
        float rYp = rY * c + uY * s;
        float rZp = rZ * c + uZ * s;

        float uXp = -rX * s + uX * c;
        float uYp = -rY * s + uY * c;
        float uZp = -rZ * s + uZ * c;

        double px = Mth.lerp(partialTicks, this.xo, this.x);
        double py = Mth.lerp(partialTicks, this.yo, this.y);
        double pz = Mth.lerp(partialTicks, this.zo, this.z);

        double cx = px - camera.getPosition().x;
        double cy = py - camera.getPosition().y;
        double cz = pz - camera.getPosition().z;

        float hs = this.quadSize * 0.5f * this.scale;

        float x0 = (float) (cx + (-rXp * hs) + (-uXp * hs));
        float y0 = (float) (cy + (-rYp * hs) + (-uYp * hs));
        float z0 = (float) (cz + (-rZp * hs) + (-uZp * hs));

        float x1 = (float) (cx + (-rXp * hs) + (uXp * hs));
        float y1 = (float) (cy + (-rYp * hs) + (uYp * hs));
        float z1 = (float) (cz + (-rZp * hs) + (uZp * hs));

        float x2 = (float) (cx + (rXp * hs) + (uXp * hs));
        float y2 = (float) (cy + (rYp * hs) + (uYp * hs));
        float z2 = (float) (cz + (rZp * hs) + (uZp * hs));

        float x3 = (float) (cx + (rXp * hs) + (-uXp * hs));
        float y3 = (float) (cy + (rYp * hs) + (-uYp * hs));
        float z3 = (float) (cz + (rZp * hs) + (-uZp * hs));

        // Get the light level for the particle.
        int light = this.getLightColor(partialTicks);

        // Texture coordinates.
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        // Render the front face.
        putVertex(buffer, x0, y0, z0, u0, v1, light);
        putVertex(buffer, x1, y1, z1, u0, v0, light);
        putVertex(buffer, x2, y2, z2, u1, v0, light);
        putVertex(buffer, x3, y3, z3, u1, v1, light);

        // Render the back face.
        putVertex(buffer, x3, y3, z3, u1, v1, light);
        putVertex(buffer, x2, y2, z2, u1, v0, light);
        putVertex(buffer, x1, y1, z1, u0, v0, light);
        putVertex(buffer, x0, y0, z0, u0, v1, light);
    }

    /**
     * Adds a vertex to the buffer.
     *
     * @param buffer The vertex buffer.
     * @param x      The X coordinate of the vertex.
     * @param y      The Y coordinate of the vertex.
     * @param z      The Z coordinate of the vertex.
     * @param u      The U texture coordinate.
     * @param v      The V texture coordinate.
     * @param light  The light level.
     */
    private void putVertex(VertexConsumer buffer, float x, float y, float z, float u, float v, int light) {
        buffer.vertex(x, y, z)
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    /**
     * Returns the render type for the particle.
     *
     * @return The render type.
     */
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    /**
     * Returns the light level for the particle.
     *
     * @param partialTicks The partial tick time for interpolation.
     * @return The light level.
     */
    @Override
    protected int getLightColor(float partialTicks) {
        return super.getLightColor(partialTicks);
    }
}