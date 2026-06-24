package su.terrafirmagreg.core.common.particle;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class GeothermalSteam extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public GeothermalSteam(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;

        this.lifetime = 200;
        this.gravity = 0;
        this.age = this.random.nextInt(20);

        this.scale(this.random.nextFloat() * 8F + 10F);

        this.setSpriteFromAge(this.spriteSet);
    }

    public void tick() {
        super.tick();

        this.setSpriteFromAge(this.spriteSet);

        this.yd = 0.025f;
    }
}
