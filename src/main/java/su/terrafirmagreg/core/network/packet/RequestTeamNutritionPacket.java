package su.terrafirmagreg.core.network.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import su.terrafirmagreg.core.network.TFGNetworkHandler;

public class RequestTeamNutritionPacket {

    public RequestTeamNutritionPacket() {
    }

    public static void encode(RequestTeamNutritionPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestTeamNutritionPacket decode(FriendlyByteBuf buffer) {
        return new RequestTeamNutritionPacket();
    }

    public static void handle(RequestTeamNutritionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) {
                return;
            }

            Optional<Team> teamOptional = FTBTeamsAPI.api().getManager().getTeamForPlayerID(sender.getUUID());
            if (teamOptional.isEmpty()) {
                return;
            }

            Team team = teamOptional.get();
            Map<UUID, float[]> nutritionMap = new HashMap<>();

            for (UUID memberUuid : team.getMembers()) {
                ServerPlayer member = sender.server.getPlayerList().getPlayer(memberUuid);
                if (member != null && member.getFoodData() instanceof TFCFoodData foodData) {
                    Nutrient[] nutrients = Nutrient.VALUES;
                    float[] values = new float[nutrients.length];
                    for (int i = 0; i < nutrients.length; i++) {
                        values[i] = foodData.getNutrition().getNutrient(nutrients[i]);
                    }
                    nutritionMap.put(memberUuid, values);
                }
            }

            if (!nutritionMap.isEmpty()) {
                TFGNetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> sender),
                        new SyncTeamNutritionPacket(nutritionMap));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
