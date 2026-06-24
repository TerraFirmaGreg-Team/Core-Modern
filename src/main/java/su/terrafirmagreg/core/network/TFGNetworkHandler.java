package su.terrafirmagreg.core.network;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.EnvironmentClientCache;
import su.terrafirmagreg.core.network.packet.*;

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
        INSTANCE.registerMessage(
                id(),
                ExtendedNutrientsPacket.class,
                ExtendedNutrientsPacket::encode,
                ExtendedNutrientsPacket::decode,
                ExtendedNutrientsPacket::handle);
        INSTANCE.registerMessage(
                id(),
                RequestTeamNutritionPacket.class,
                RequestTeamNutritionPacket::encode,
                RequestTeamNutritionPacket::decode,
                RequestTeamNutritionPacket::handle);
        INSTANCE.registerMessage(
                id(),
                SyncTeamNutritionPacket.class,
                SyncTeamNutritionPacket::encode,
                SyncTeamNutritionPacket::decode,
                SyncTeamNutritionPacket::handle);
        INSTANCE.registerMessage(
                id(),
                SelectColorPacket.class,
                SelectColorPacket::encode,
                SelectColorPacket::decode,
                SelectColorPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(
                id(),
                SelectChromaticCodePacket.class,
                SelectChromaticCodePacket::encode,
                SelectChromaticCodePacket::decode,
                SelectChromaticCodePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(
                id(),
                EnvironmentQueryPacket.class,
                EnvironmentQueryPacket::encode,
                EnvironmentQueryPacket::decode,
                EnvironmentQueryPacket::handle);
        INSTANCE.registerMessage(
                id(),
                EnvironmentResponsePacket.class,
                EnvironmentResponsePacket::encode,
                EnvironmentResponsePacket::decode,
                EnvironmentResponsePacket::handle);
        INSTANCE.registerMessage(
                id(),
                DecompressionEventPacket.class,
                DecompressionEventPacket::encode,
                DecompressionEventPacket::decode,
                DecompressionEventPacket::handle);
        INSTANCE.registerMessage(
                id(),
                ForcedPosePacket.class,
                ForcedPosePacket::encode,
                ForcedPosePacket::decode,
                ForcedPosePacket::handle);
    }

    public static void sendForcedPose(ServerPlayer player, @Nullable Pose pose) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ForcedPosePacket(pose));
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

    public static void sendDecompressionEventStart(ServerLevel level, BlockPos pos, int durationTicks, Vec3 breachAxis) {
        sendToAllAround(level, pos, DecompressionEventPacket.start(pos, durationTicks, breachAxis));
    }

    public static void sendDecompressionEventStop(ServerLevel level, BlockPos pos) {
        sendToAllAround(level, pos, DecompressionEventPacket.stop(pos));
    }

    public static void sendDecompressionEventShift(ServerLevel level, BlockPos oldPos, BlockPos newPos, int durationTicks, int elapsedTicks, Vec3 newBreachAxis) {
        DecompressionEventPacket pkt = DecompressionEventPacket.shift(oldPos, newPos, durationTicks, elapsedTicks, newBreachAxis);
        // Send in both locations to affect all players. Client handles dedup.
        sendToAllAround(level, oldPos, pkt);
        sendToAllAround(level, newPos, pkt);
    }

    /**
     * Sends an environment query from client to server.
     * Called from client side only.
     *
     * @param pos The position to query
     */
    public static void sendAtmosphereQuery(BlockPos pos) {
        INSTANCE.sendToServer(new EnvironmentQueryPacket(pos));
    }

    /**
     * Sends an environment response from server to a specific client.
     *
     * @param player The player to send to
     * @param pos The position that was queried
     * @param state The environment state at that position
     */
    public static void sendEnvironmentResponse(ServerPlayer player, BlockPos pos, EnvironmentClientCache.EnvironmentState state) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new EnvironmentResponsePacket(pos, state));
    }
}
