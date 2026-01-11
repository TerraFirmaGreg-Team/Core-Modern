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

public class CoolingSteamProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;

    public CoolingSteamProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
            double x, double y, double z,
            double dx, double dy, double dz) {
        Options opts = Minecraft.getInstance().options;
        if (opts.particles().get() == ParticleStatus.MINIMAL) {
            return null;
        }

        CoolingSteam particle = new CoolingSteam(level, x, y, z, spriteSet);
        particle.pickSprite(spriteSet);
        return particle;
    }
}
