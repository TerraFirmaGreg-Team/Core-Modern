package su.terrafirmagreg.core.world.new_ow_wg.surface_states;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

// TFC why did you have to make SoilSurfaceState.NeedsPostProcessing private with a private constructor...

public record NeedsPostProcessingSoilSurfaceState(List<SurfaceState> regions) implements SurfaceState {

    @Override
    public @NotNull BlockState getState(SurfaceBuilderContext context) {
        // Bias a little towards sand regions
        // Without: pure sand < 55mm, mixed sand < 110mm. With: pure sand < 73mm, mixed sand < 126mm
        final float rainfall = context.rainfall();
        final int index = (int) Mth.clampedMap(rainfall, 20, 500, 0, regions.size() - 0.01f);

        return regions.get(index).getState(context);
    }

    @Override
    public void setState(SurfaceBuilderContext context) {
        context.chunk().setBlockState(context.pos(), getState(context), false);
        context.chunk().markPosForPostprocessing(context.pos());
    }
}
