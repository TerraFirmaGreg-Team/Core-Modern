package su.terrafirmagreg.core.common.data.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class RailgunAmmo extends TextureSheetParticle {
    private final SpriteSet sprites;

    public RailgunAmmo(ClientLevel level, double x, double y, double z,
                       double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);

        // No physics
        this.gravity = 0.0f;
        this.hasPhysics = false;

        // Strong vertical velocity, no horizontal
        this.xd = 0;
        this.yd = 5.0f;
        this.zd = 0;

        this.lifetime = 20;
        this.quadSize = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
