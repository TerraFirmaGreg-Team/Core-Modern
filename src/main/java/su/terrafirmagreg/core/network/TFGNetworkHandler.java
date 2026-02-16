package su.terrafirmagreg.core.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.network.packet.FuelSyncPacket;
import su.terrafirmagreg.core.network.packet.OreHighlightPacket;
import su.terrafirmagreg.core.network.packet.OreHighlightVeinPacket;
import su.terrafirmagreg.core.network.packet.ParticlePacket;
import su.terrafirmagreg.core.network.packet.SoundPacket;

public class TFGNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void init() {
        INSTANCE.registerMessage(
                id(),
                ParticlePacket.class,
                ParticlePacket::encode,
                ParticlePacket::decode,
                ParticlePacket::handle);
        INSTANCE.registerMessage(
                id(),
                SoundPacket.class,
                SoundPacket::encode,
                SoundPacket::decode,
                SoundPacket::handle);
        INSTANCE.registerMessage(
                id(),
                OreHighlightPacket.class,
                OreHighlightPacket::encode,
                OreHighlightPacket::decode,
                OreHighlightPacket::handle);
        INSTANCE.registerMessage(
                id(),
                OreHighlightVeinPacket.class,
                OreHighlightVeinPacket::encode,
                OreHighlightVeinPacket::decode,
                OreHighlightVeinPacket::handle);
        INSTANCE.registerMessage(
                id(),
                FuelSyncPacket.class,
                FuelSyncPacket::encode,
                FuelSyncPacket::decode,
                FuelSyncPacket::handle);
    }

    private static void sendToAllAround(Level level, BlockPos pos, Object packet) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                pos.getX(), pos.getY(), pos.getZ(),
                64, serverLevel.dimension())), packet);
    }

    public static void sendParticle(
            ServerLevel level,
            double x, double y, double z,
            Vec3 motion,
            ResourceLocation particleId,
            int count,
            double dx, double dy, double dz) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        ParticlePacket packet = new ParticlePacket(
                x, y, z,
                motion.x, motion.y, motion.z,
                particleId,
                count,
                dx, dy, dz);
        sendToAllAround(level, pos, packet);
    }

    public static void sendSound(
            ServerLevel level,
            double x, double y, double z,
            ResourceLocation soundId,
            float volume,
            float pitch) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        SoundPacket packet = new SoundPacket(
                x, y, z,
                soundId,
                volume,
                pitch);
        sendToAllAround(level, pos, packet);
    }
}
