package su.terrafirmagreg.core.common.data.entities.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.EntityRenderersEvent;

import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.client.renderers.entities.vehicles.RocketRenderer;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.registry.ModEntityTypes;

import su.terrafirmagreg.core.common.data.TFGEntities;

public class RocketHelper {

    public static final Map<EntityType<?>, Long> ROCKET_FUEL_CAP = Map.of(
            ModEntityTypes.TIER_1_ROCKET.get(), 3000L,
            ModEntityTypes.TIER_2_ROCKET.get(), 3000L,
            ModEntityTypes.TIER_3_ROCKET.get(), 3000L,
            ModEntityTypes.TIER_4_ROCKET.get(), 3000L,
            TFGEntities.TIER_1_DOUBLE_ROCKET.get(), 6000L,
            TFGEntities.TIER_2_DOUBLE_ROCKET.get(), 6000L,
            TFGEntities.TIER_3_DOUBLE_ROCKET.get(), 6000L,
            TFGEntities.TIER_4_DOUBLE_ROCKET.get(), 6000L);

    public static final Map<EntityType<?>, List<Long>> ROCKET_FUEL_USAGE = Map.of(
            ModEntityTypes.TIER_1_ROCKET.get(), new ArrayList<>(List.of(1000L, 3000L)),
            ModEntityTypes.TIER_2_ROCKET.get(), new ArrayList<>(List.of(1000L, 3000L)),
            ModEntityTypes.TIER_3_ROCKET.get(), new ArrayList<>(List.of(1000L, 3000L)),
            ModEntityTypes.TIER_4_ROCKET.get(), new ArrayList<>(List.of(1000L, 3000L)),
            TFGEntities.TIER_1_DOUBLE_ROCKET.get(), new ArrayList<>(List.of(2000L, 6000L)),
            TFGEntities.TIER_2_DOUBLE_ROCKET.get(), new ArrayList<>(List.of(2000L, 6000L)),
            TFGEntities.TIER_3_DOUBLE_ROCKET.get(), new ArrayList<>(List.of(2000L, 6000L)),
            TFGEntities.TIER_4_DOUBLE_ROCKET.get(), new ArrayList<>(List.of(2000L, 6000L)));

    public static Rocket makeRocket(EntityType<?> type, Level level) {
        return new Rocket(type, level);
    }

    public static RocketRenderer makeRocketRendererT1(final EntityRendererProvider.Context renderManager) {
        return new RocketRenderer(renderManager, RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE);
    }

    public static RocketRenderer makeRocketRendererT2(final EntityRendererProvider.Context renderManager) {
        return new RocketRenderer(renderManager, RocketModel.TIER_2_LAYER, RocketRenderer.TIER_2_TEXTURE);
    }

    public static RocketRenderer makeRocketRendererT3(final EntityRendererProvider.Context renderManager) {
        return new RocketRenderer(renderManager, RocketModel.TIER_3_LAYER, RocketRenderer.TIER_3_TEXTURE);
    }

    public static RocketRenderer makeRocketRendererT4(final EntityRendererProvider.Context renderManager) {
        return new RocketRenderer(renderManager, RocketModel.TIER_4_LAYER, RocketRenderer.TIER_4_TEXTURE);
    }

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions registry) {
        //registry.registerLayerDefinition(DoubleRocketModel.TIER_1_DOUBLE_LAYER, DoubleRocketModel::createTier1DoubleLayer);
    }

}
