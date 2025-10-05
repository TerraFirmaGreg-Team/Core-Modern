package su.terrafirmagreg.core.common.data.particles;

import net.dries007.tfc.client.ClimateRenderCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec2;

public class CoolingSteam extends TextureSheetParticle {
    private final float xWind;
    private final float zWind;
    private final float verticalSpeed = 0.6f;
    private final float speed = 0.4f;

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public CoolingSteam(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        Vec2 wind = ClimateRenderCache.INSTANCE.getWind();
        this.lifetime = 100;
        this.gravity = 0;
        this.age = this.random.nextInt(20);

        xWind = wind.x;
        zWind = wind.y;

        this.scale(this.random.nextFloat() * 8F + 2F);
        System.out.println("Cooling Steam Constructor");
    }

    public void tick() {
        super.tick();

        this.yd = (age * -0.01f + 1) * verticalSpeed;

        this.xd = xWind * speed;
        this.zd = zWind * speed;
    }

}
