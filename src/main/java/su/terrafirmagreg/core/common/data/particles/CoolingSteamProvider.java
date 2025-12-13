package su.terrafirmagreg.core.common.data.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class CoolingSteamProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public CoolingSteamProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
            double x, double y, double z,
            double dx, double dy, double dz) {
        CoolingSteam particle = new CoolingSteam(level, x, y, z, spriteSet);
        particle.pickSprite(spriteSet);
        return particle;
    }
}
