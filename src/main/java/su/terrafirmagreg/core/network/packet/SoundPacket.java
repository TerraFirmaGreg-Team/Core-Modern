package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

public class SoundPacket {
    private final double x, y, z;
    private final ResourceLocation soundId;
    private final float volume;
    private final float pitch;

    public SoundPacket(double x, double y, double z, ResourceLocation soundId, float volume, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.soundId = soundId;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static void encode(SoundPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
        buf.writeResourceLocation(pkt.soundId);
        buf.writeFloat(pkt.volume);
        buf.writeFloat(pkt.pitch);
    }

    public static SoundPacket decode(FriendlyByteBuf buf) {
        return new SoundPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readResourceLocation(),
                buf.readFloat(),
                buf.readFloat());
    }

    public static void handle(SoundPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null)
                return;

            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(pkt.soundId);
            if (sound != null) {
                level.playLocalSound(pkt.x, pkt.y, pkt.z, sound, SoundSource.BLOCKS, pkt.volume, pkt.pitch, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
