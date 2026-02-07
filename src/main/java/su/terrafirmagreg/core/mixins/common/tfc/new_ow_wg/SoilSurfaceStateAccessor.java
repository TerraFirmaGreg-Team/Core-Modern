package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceState;

@Mixin(value = SoilSurfaceState.class, remap = false)
public interface SoilSurfaceStateAccessor {

    @Invoker("<init>")
    static SoilSurfaceState newSoilSurfaceState(List<SurfaceState> regions) {
        throw new AssertionError();
    }
}
