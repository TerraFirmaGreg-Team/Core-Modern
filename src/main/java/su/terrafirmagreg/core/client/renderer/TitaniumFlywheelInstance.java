package su.terrafirmagreg.core.client.renderer;

import com.teammoeg.steampowered.client.instance.AbstractSPFlywheelInstance;
import com.teammoeg.steampowered.oldcreatestuff.OldFlywheelBlockEntity;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import su.terrafirmagreg.core.client.TFGBlockPartials;

public class TitaniumFlywheelInstance extends AbstractSPFlywheelInstance {

    public TitaniumFlywheelInstance(VisualizationContext modelManager, OldFlywheelBlockEntity tile, float d) {
        super(modelManager, tile, d);
    }

    @Override
    protected PartialModel getWheelModel() {
        return TFGBlockPartials.TITANIUM_FLYWHEEL;
    }

    @Override
    protected PartialModel getUpperSlidingModel() {
        return TFGBlockPartials.TITANIUM_FLYWHEEL_UPPER_SLIDING;
    }

    @Override
    protected PartialModel getLowerSlidingModel() {
        return TFGBlockPartials.TITANIUM_FLYWHEEL_LOWER_SLIDING;
    }

    @Override
    protected PartialModel getUpperRotatingModel() {
        return TFGBlockPartials.TITANIUM_FLYWHEEL_UPPER_ROTATING;
    }

    @Override
    protected PartialModel getLowerRotatingModel() {
        return TFGBlockPartials.TITANIUM_FLYWHEEL_LOWER_ROTATING;
    }
}
