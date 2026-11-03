package su.terrafirmagreg.core.common.particle;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.ClimateRenderCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec2;

public class LargeSmoke extends SmokeParticle {
    private final float xWind;
    private final float zWind;

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    protected LargeSmoke(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, 2.5F, sprites);

        Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
        xWind = wind.x;
        zWind = wind.y;
    }

    @Override
    public void tick() {
        super.tick();

        float speed = 0.4f;
        this.xd = xWind * speed;
        this.zd = zWind * speed;
    }
}
