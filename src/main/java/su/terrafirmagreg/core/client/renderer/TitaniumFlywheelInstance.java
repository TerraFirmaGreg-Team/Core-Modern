package su.terrafirmagreg.core.client.renderer;

import com.teammoeg.steampowered.client.instance.AbstractSPFlywheelInstance;
import com.teammoeg.steampowered.oldcreatestuff.OldFlywheelBlockEntity;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import su.terrafirmagreg.core.common.data.TFGPartialModels;

public class TitaniumFlywheelInstance extends AbstractSPFlywheelInstance {

    public TitaniumFlywheelInstance(VisualizationContext modelManager, OldFlywheelBlockEntity tile, float d) {
        super(modelManager, tile, d);
    }

    @Override
    protected PartialModel getWheelModel() {
        return TFGPartialModels.TITANIUM_FLYWHEEL;
    }

    @Override
    protected PartialModel getUpperSlidingModel() {
        return TFGPartialModels.TITANIUM_FLYWHEEL_UPPER_SLIDING;
    }

    @Override
    protected PartialModel getLowerSlidingModel() {
        return TFGPartialModels.TITANIUM_FLYWHEEL_LOWER_SLIDING;
    }

    @Override
    protected PartialModel getUpperRotatingModel() {
        return TFGPartialModels.TITANIUM_FLYWHEEL_UPPER_ROTATING;
    }

    @Override
    protected PartialModel getLowerRotatingModel() {
        return TFGPartialModels.TITANIUM_FLYWHEEL_LOWER_ROTATING;
    }
}
