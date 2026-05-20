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
 * Packet to sync negative nutrient values from server to client.
 */
public class NegativeNutrientsPacket {

    private final float[] negativeNutrients;

    public NegativeNutrientsPacket(float[] negativeNutrients) {
        this.negativeNutrients = negativeNutrients != null ? negativeNutrients : new float[TFGNutrients.getExtendedCount()];
    }

    public static void encode(NegativeNutrientsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.negativeNutrients.length);
        for (float value : packet.negativeNutrients) {
            buffer.writeFloat(value);
        }
    }

    public static NegativeNutrientsPacket decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        float[] negativeNutrients = new float[count];
        for (int i = 0; i < count; i++) {
            negativeNutrients[i] = buffer.readFloat();
        }
        return new NegativeNutrientsPacket(negativeNutrients);
    }

    public static void handle(NegativeNutrientsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        ctx.get().setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static void handleClient(NegativeNutrientsPacket packet) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && player.getFoodData() instanceof TFCFoodData tfcFoodData) {
            NutritionData nutritionData = tfcFoodData.getNutrition();
            NutritionDataExtension.onClientUpdate(nutritionData, packet.negativeNutrients);
        }
    }
}
