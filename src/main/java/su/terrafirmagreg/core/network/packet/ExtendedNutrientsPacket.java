package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.common.food.nutrient.NutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Packet to sync extended nutrient values from server to client.
 */
public class ExtendedNutrientsPacket {

    private final float[] extendedNutrients;

    public ExtendedNutrientsPacket(float[] extendedNutrients) {
        this.extendedNutrients = extendedNutrients != null ? extendedNutrients : new float[TFGNutrients.getExtendedCount()];
    }

    public static void encode(ExtendedNutrientsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.extendedNutrients.length);
        for (float value : packet.extendedNutrients) {
            buffer.writeFloat(value);
        }
    }

    public static ExtendedNutrientsPacket decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        float[] extendedNutrients = new float[count];
        for (int i = 0; i < count; i++) {
            extendedNutrients[i] = buffer.readFloat();
        }
        return new ExtendedNutrientsPacket(extendedNutrients);
    }

    public static void handle(ExtendedNutrientsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static void handleClient(ExtendedNutrientsPacket packet) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && player.getFoodData() instanceof TFCFoodData tfcFoodData) {
            NutritionData nutritionData = tfcFoodData.getNutrition();
            NutritionDataExtension.onClientUpdate(nutritionData, packet.extendedNutrients);
        }
    }
}
