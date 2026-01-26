package su.terrafirmagreg.core.common.data.entities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.client.renderers.entities.vehicles.RocketRenderer;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;

public class RocketHelper {

    public static Rocket makeRocket(EntityType<?> type, Level level) {
        return new Rocket(type, level);
    }

    public static RocketRenderer makeRocketRenderer(final EntityRendererProvider.Context renderManager) {
        return new RocketRenderer(renderManager, RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE);
    }
}
