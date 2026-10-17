package su.terrafirmagreg.core.mixins.client.minecraft;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {

    protected ClientLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration,
            Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    // Changes the maximum visual brightness of the level during daytime on venus

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true, require = 0)
    private void tfg$getSkyDarken(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (this.dimension() == Planet.VENUS && TFGConfig.CLIENT.VENUS_DIMMING.get()) {
            cir.setReturnValue(cir.getReturnValueF() * 0.7f);
        }
    }
}
