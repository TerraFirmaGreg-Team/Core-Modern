package su.terrafirmagreg.core.common.data.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class OreProspectorVeinProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public OreProspectorVeinProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double dx, double dy, double dz) {
        OreProspectorVein particle = new OreProspectorVein(level, x, y, z);
        particle.pickSprite(spriteSet);
        return particle;
    }
}