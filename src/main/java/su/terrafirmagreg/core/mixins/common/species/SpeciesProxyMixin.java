package su.terrafirmagreg.core.mixins.common.species;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.ninni.species.CommonProxy;
import com.ninni.species.Species;
import com.ninni.species.client.ClientProxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

// In dev/legacy Forge, `DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new)`
// can trigger loading of ClientProxy on the dedicated server. Replace the call with an
// explicit Dist check so ClientProxy is only referenced (and thus loaded) on the client.
@Mixin(value = Species.class, remap = false)
public class SpeciesProxyMixin {

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/DistExecutor;safeRunForDist(Ljava/util/function/Supplier;Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private static Object tfg$initProxy(Supplier<?> clientTarget, Supplier<?> commonTarget) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return new ClientProxy();
        }
        return new CommonProxy();
    }
}
