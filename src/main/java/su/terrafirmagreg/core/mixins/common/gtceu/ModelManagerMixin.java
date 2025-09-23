package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import su.terrafirmagreg.core.common.data.buds.BudRenderer;

@Mixin(value = ModelManager.class)
public abstract class ModelManagerMixin {

    @Inject(method = "reload", at = @At(value = "TAIL"))
    private void tfg$loadDynamicModels(PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
            ProfilerFiller reloadProfiler, Executor backgroundExecutor,
            Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        BudRenderer.reinitModels();
    }
}
