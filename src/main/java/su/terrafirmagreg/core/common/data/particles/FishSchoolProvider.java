package su.terrafirmagreg.core.common.data.particles;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Provider for FishSchool particle.
 */
public class FishSchoolProvider implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet sprites;

    public FishSchoolProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(@NotNull SimpleParticleType type, ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed) {
        Options opts = Minecraft.getInstance().options;
        if (opts.particles().get() == ParticleStatus.MINIMAL) {
            return null;
        }

        RandomSource rand = level.random;
        float radius = 1.0f + rand.nextFloat() * 4.0f;

        // Constant tangential speed (blocks per tick).
        float baseLinearSpeed = 0.1f;
        // Jitter so schools aren't perfectly synchronized.
        float jitter = 0.9f + rand.nextFloat() * 0.2f;
        float linearSpeed = baseLinearSpeed * jitter;

        // Select a random sprite from the set.
        var sprite = sprites.get(rand);

        FishSchool p = new FishSchool(level, x, y, z, sprite, radius, linearSpeed);
        p.setColor(1.0f, 1.0f, 1.0f);
        p.withAlpha(1.0f);
        return p;
    }
}
