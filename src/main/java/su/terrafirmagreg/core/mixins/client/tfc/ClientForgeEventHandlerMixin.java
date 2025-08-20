package su.terrafirmagreg.core.mixins.client.tfc;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.dries007.tfc.client.ClientForgeEventHandler;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import su.terrafirmagreg.core.common.data.TFGParticles;

@Mixin(value = ClientForgeEventHandler.class, remap = false)
public abstract class ClientForgeEventHandlerMixin {

    @Definition(id = "WIND", field = "Lnet/dries007/tfc/client/particle/TFCParticles;WIND:Lnet/minecraftforge/registries/RegistryObject;")
    @Definition(id = "get", method = "Lnet/minecraftforge/registries/RegistryObject;get() Ljava/lang/Object; ")
    @Definition(id = "ParticleOptions", type = ParticleOptions.class)
    @Expression("(ParticleOptions) WIND.get()")
    @ModifyExpressionValue(method = "tickWind()V",
            at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static ParticleOptions redirectWindParticle(ParticleOptions original, @Local Level level) {
//            if (AdAstraData.isPlanet(level.dimension()) && AdAstraData.getPlanet(level.dimension()).dimension().location().getPath().equals("mars"))
        if (true)
            {
                return TFGParticles.COLORED_WIND.get();
            } else return original;
    }
}
