package su.terrafirmagreg.core.network.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.forsteri.createliquidfuel.util.Triplet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Packet for syncing blaze burner liquid fuel map to the client to populate emi.
 */
public record FuelSyncPacket(Map<ResourceLocation, JsonElement> data) {

    private static final Gson GSON = new Gson();
    @SuppressWarnings("removal")
    private static final ResourceLocation JSON_LOADER_ID = ResourceLocation.of("createliquidfuel:drainable_fuel_loader", ':');

    // Populated by mixin after server-side JSON reload
    public static Map<ResourceLocation, JsonElement> capturedJsonData = new HashMap<>();

    public static void encode(FuelSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.data.size());
        for (Map.Entry<ResourceLocation, JsonElement> entry : pkt.data.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeUtf(GSON.toJson(entry.getValue()));
        }
    }

    public static FuelSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, JsonElement> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation key = buf.readResourceLocation();
            JsonElement element = GSON.fromJson(buf.readUtf(), JsonElement.class);
            map.put(key, element);
        }
        return new FuelSyncPacket(map);
    }

    // Copied from LiquidBurnerFuelJsonLoader.apply()
    public static void handle(FuelSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (Map.Entry<ResourceLocation, JsonElement> entry : pkt.data.entrySet()) {
                JsonElement element = entry.getValue();
                if (!element.isJsonObject())
                    continue;

                JsonObject object = element.getAsJsonObject();
                JsonElement fluidElement = object.get("fluid");
                if (fluidElement == null)
                    continue;

                @SuppressWarnings("removal")
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidElement.getAsString()));
                if (fluid == null)
                    continue;

                BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.put(fluid,
                        Pair.of(
                                JSON_LOADER_ID,
                                Triplet.of(
                                        object.has("burnTime")
                                                ? object.get("burnTime").getAsInt()
                                                : object.has("superHeat") && object.get("superHeat").getAsBoolean()
                                                        ? 32
                                                        : 20,
                                        object.has("superHeat") && object.get("superHeat").getAsBoolean(),
                                        object.has("amountConsumedPerTick")
                                                ? object.get("amountConsumedPerTick").getAsInt()
                                                : object.has("superHeat") && object.get("superHeat").getAsBoolean()
                                                        ? 10
                                                        : 1)));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
