package exception.terrafirmagreg.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nonnull;

public class ColoredDripParticle extends DripParticle {
    public ColoredDripParticle(ClientLevel world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z, fluid);
        this.lifetime = (int) (16.0D / (world.random.nextDouble() * 0.8D + 0.2D));
        this.gravity = 0.007F;
    }

    @Override
    protected void postMoveUpdate() {
        if (this.onGround) {
            this.remove();
        }
    }

    public static class ColoredDripParticleFactory implements ParticleProvider<ColoredParticleType> {
        protected final SpriteSet sprite;

        public ColoredDripParticleFactory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(@Nonnull ColoredParticleType typeIn, @Nonnull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            DripParticle dripparticle = new ColoredDripParticle(world, x, y, z, Fluids.EMPTY);

            float[] colors = typeIn.getColor();
            if (colors != null) {
                dripparticle.setColor(colors[0], colors[1], colors[2]);
            } else {
                dripparticle.setColor(0.92F, 0.782F, 0.72F);
            }

            dripparticle.pickSprite(this.sprite);

            return dripparticle;
        }
    }
}
