package su.terrafirmagreg.core.common.data.particles;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.particle.WindParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

import snownee.jade.util.Color;

public class ColoredWindParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet set;
    private final float r;
    private final float g;
    private final float b;
    private static int particlesSpawned = 0;

    public ColoredWindParticleProvider(SpriteSet set, int color) {
        this.set = set;
        Color rgb = Color.hex(Integer.toHexString(color));
        this.r = rgb.getRed();
        this.g = rgb.getGreen();
        this.b = rgb.getBlue();
    }

    public ColoredWindParticleProvider(SpriteSet set, Color color) {
        this(set, color.toInt());
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public @Nullable Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        WindParticle particle = new WindParticle(pLevel, pX, pY, pZ);
        //        if (particlesSpawned++ >= 20) {
        //            System.out.println(pLevel.getBiome(BlockPos.containing(pX, pY, pZ)).tags().toList());
        //            particlesSpawned = 0;
        //        }
        particle.pickSprite(set);
        particle.setColor(this.r / 255f, this.g / 255f, this.b / 255f);
        particle.scale(3.0f);
        return particle;
    }
}
