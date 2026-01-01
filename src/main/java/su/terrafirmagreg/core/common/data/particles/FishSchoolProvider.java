package su.terrafirmagreg.core.common.data.particles;

import org.jetbrains.annotations.NotNull;

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
        RandomSource rand = level.random;
        float radius = 1.0f + rand.nextFloat() * 4.0f;

        // Base period speed.
        float baseTicks = 200.0f;
        // Jitter it a bit so not all schools move identically.
        float jitter = 0.75f + rand.nextFloat() * 0.5f;
        // Angular speed in radians per tick.
        float angularSpeed = (float) (Math.PI * 2.0) / (baseTicks * jitter);

        // Select a random sprite from the set.
        var sprite = sprites.get(rand);

        FishSchool p = new FishSchool(level, x, y, z, sprite, radius, angularSpeed);
        p.setColor(1.0f, 1.0f, 1.0f);
        p.withAlpha(1.0f);
        return p;
    }
}
