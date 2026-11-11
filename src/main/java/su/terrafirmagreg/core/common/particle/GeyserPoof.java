package su.terrafirmagreg.core.common.particle;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.ClimateRenderCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec2;

public class GeyserPoof extends TextureSheetParticle {
    private final float xWind;
    private final float zWind;
    private final SpriteSet spriteSet;

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public GeyserPoof(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
        super(level, x, y, z);
        this.spriteSet = spriteSet;

        Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
        this.lifetime = 160;
        this.gravity = 0;
        this.age = this.random.nextInt(20);

        xWind = wind.x;
        zWind = wind.y;

        this.scale(4.0f);

        this.setSpriteFromAge(this.spriteSet);
    }

    public void tick() {
        super.tick();

        this.setSpriteFromAge(this.spriteSet);

        float verticalSpeed = 0.05f;
        this.yd = (age * -0.003f + 1) * verticalSpeed;

        float speed = 0.1f;
        this.xd = xWind * speed;
        this.zd = zWind * speed;
    }
}
