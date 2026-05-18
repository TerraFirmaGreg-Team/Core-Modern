package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;
import su.terrafirmagreg.core.common.food.nutrient.NutritionDataExtension;
import su.terrafirmagreg.core.network.TFGNetworkHandler;
import su.terrafirmagreg.core.network.packet.ExtendedNutrientsPacket;

/**
 * Mixin to send extended nutrients to the client alongside the regular nutrition data.
 */
@Mixin(TFCFoodData.class)
public class TFCFoodDataMixin {

    @Shadow(remap = false)
    @Final
    private Player sourcePlayer;

    @Shadow(remap = false)
    @Final
    private NutritionData nutritionData;

    /**
     * After the tick method sends the FoodDataUpdatePacket, also send extended nutrients
     * and apply nutrition-based effects.
     */
    @Inject(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void tfg$sendExtendedNutrients(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            float[] extendedNutrients = NutritionDataExtension.getExtendedNutrients(nutritionData);
            if (extendedNutrients != null && extendedNutrients.length > 0) {
                TFGNetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ExtendedNutrientsPacket(extendedNutrients));
            }
            NutrientEffectsHandler.tick(serverPlayer, nutritionData);
        }
    }

    /**
     * When the client receives a nutrition update re-evaluate nutrient effects so
     * multipliers are available for the UI.
     */
    @Inject(method = "onClientUpdate([FF)V", at = @At("TAIL"), remap = false)
    private void tfg$onClientUpdate(float[] nutrients, float thirst, CallbackInfo ci) {
        NutrientEffectsHandler.onClientUpdate(sourcePlayer, nutritionData);
    }
}
