package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticlePacket {
    private final double x, y, z;
    private final double motX, motY, motZ;
    private final ResourceLocation particleId;
    private final int count;
    private final double dx, dy, dz;

    public ParticlePacket(double x, double y, double z,
            double motX, double motY, double motZ,
            ResourceLocation particleId,
            int count,
            double dx, double dy, double dz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motX = motX;
        this.motY = motY;
        this.motZ = motZ;
        this.particleId = particleId;
        this.count = count;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public static void encode(ParticlePacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
        buf.writeDouble(pkt.motX);
        buf.writeDouble(pkt.motY);
        buf.writeDouble(pkt.motZ);
        buf.writeResourceLocation(pkt.particleId);
        buf.writeInt(pkt.count);
        buf.writeDouble(pkt.dx);
        buf.writeDouble(pkt.dy);
        buf.writeDouble(pkt.dz);
    }

    public static ParticlePacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double motX = buf.readDouble();
        double motY = buf.readDouble();
        double motZ = buf.readDouble();
        ResourceLocation particleId = buf.readResourceLocation();
        int count = buf.readInt();
        double dx = buf.readDouble();
        double dy = buf.readDouble();
        double dz = buf.readDouble();
        return new ParticlePacket(x, y, z, motX, motY, motZ, particleId, count, dx, dy, dz);
    }

    public static void handle(ParticlePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null)
                return;

            ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(pkt.particleId);
            if (type instanceof SimpleParticleType simpleType) {
                RandomSource random = level.random;
                for (int i = 0; i < pkt.count; i++) {
                    double spawnX = pkt.x + (random.nextDouble() * 2 - 1) * pkt.dx;
                    double spawnY = pkt.y + (random.nextDouble() * 2 - 1) * pkt.dy;
                    double spawnZ = pkt.z + (random.nextDouble() * 2 - 1) * pkt.dz;
                    level.addParticle(simpleType,
                            spawnX, spawnY, spawnZ,
                            pkt.motX, pkt.motY, pkt.motZ);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
