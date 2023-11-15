package exception.terrafirmagreg.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ColoredParticleType extends ParticleType<ColoredParticleType> implements ParticleOptions {
    private static final Deserializer<ColoredParticleType> DESERIALIZER = new Deserializer<>() {
        @Nonnull
        @Override
        public ColoredParticleType fromCommand(@Nonnull ParticleType<ColoredParticleType> particleType, @Nonnull StringReader stringReader) {
            return (ColoredParticleType) particleType;
        }

        @Nonnull
        @Override
        public ColoredParticleType fromNetwork(@Nonnull ParticleType<ColoredParticleType> particleType, @Nonnull FriendlyByteBuf buffer) {
            return (ColoredParticleType) particleType;
        }
    };
    private final Codec<ColoredParticleType> codec = Codec.unit(this::getType);
    private float[] color = null;

    public ColoredParticleType() {
        super(false, DESERIALIZER);
    }

    @Override
    public Codec<ColoredParticleType> codec() {
        return codec;
    }

    @Nullable
    public float[] getColor() {
        return this.color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    @Nonnull
    @Override
    public ColoredParticleType getType() {
        return this;
    }

    @Override
    public void writeToNetwork(@Nonnull FriendlyByteBuf packetBuffer) {
    }

    @Nonnull
    @Override
    public String writeToString() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(this).toString();
    }
}