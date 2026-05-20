package su.terrafirmagreg.core.network.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.screen.TFGNutritionScreen;

public class SyncTeamNutritionPacket {

    private final Map<UUID, float[]> nutritionMap;

    public SyncTeamNutritionPacket(Map<UUID, float[]> nutritionMap) {
        this.nutritionMap = nutritionMap;
    }

    public static void encode(SyncTeamNutritionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.nutritionMap.size());
        packet.nutritionMap.forEach((uuid, nutrients) -> {
            buffer.writeUUID(uuid);
            buffer.writeVarInt(nutrients.length);
            for (float n : nutrients) {
                buffer.writeFloat(n);
            }
        });
    }

    public static SyncTeamNutritionPacket decode(FriendlyByteBuf buffer) {
        int mapSize = buffer.readVarInt();
        Map<UUID, float[]> map = new HashMap<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            UUID uuid = buffer.readUUID();
            int nutrientCount = buffer.readVarInt();
            float[] nutrients = new float[nutrientCount];
            for (int j = 0; j < nutrientCount; j++) {
                nutrients[j] = buffer.readFloat();
            }
            map.put(uuid, nutrients);
        }
        return new SyncTeamNutritionPacket(map);
    }

    public static void handle(SyncTeamNutritionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncTeamNutritionPacket packet) {
        TFGNutritionScreen.receiveTeamNutrition(packet.nutritionMap);
    }
}
