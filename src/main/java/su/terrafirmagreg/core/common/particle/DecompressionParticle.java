package su.terrafirmagreg.core.common.particle;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DecompressionParticle extends TextureSheetParticle {

    protected DecompressionParticle(ClientLevel level, double x, double y, double z,
            double dx, double dy, double dz, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.gravity = 0;
        this.lifetime = 60;
        this.quadSize = level.random.nextFloat() < 0.05f ? 0.05f : 0.02f;
        this.hasPhysics = false;
        this.pickSprite(spriteSet);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public record Provider(SpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                double x, double y, double z, double dx, double dy, double dz) {
            return new DecompressionParticle(level, x, y, z, dx, dy, dz, spriteSet);
        }
    }
}
