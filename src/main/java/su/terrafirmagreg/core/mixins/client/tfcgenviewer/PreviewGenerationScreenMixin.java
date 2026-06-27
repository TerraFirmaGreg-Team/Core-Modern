package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.notenoughmail.tfcgenviewer.screen.PreviewGenerationScreen;

import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.world.level.levelgen.WorldOptions;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.utils.CustomSpawnHelper;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.TfgClientPreviewState;

/**
 * Create-world preview does not use {@link com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket}; clear any
 * leftover viewer session from a previous in-world session so {@link TfgClientPreviewState#useTfgOverworldPipeline()}
 * follows config / unresolved-session inference instead of stale {@code active}.
 */
@Mixin(value = PreviewGenerationScreen.class, remap = false)
public class PreviewGenerationScreenMixin {

    @Shadow(remap = false)
    private String localSeed;

    @Shadow(remap = false)
    private OptionInstance<Boolean> spawnOverlay;

    @Inject(method = "<init>", at = @At(value = "RETURN"), remap = false)
    private void tfg$afterInit(CallbackInfo ci) {
        TfgClientPreviewState.leave();
    }

    /**
     * Matches {@link WorldOptions#parseSeed(String)} parsing in {@link PreviewGenerationScreen} before allocating {@link net.dries007.tfc.world.region.RegionGenerator}.
     */
    @Inject(method = "getRegionGenerator", at = @At("HEAD"), remap = false)
    private void tfg$syncSeedBeforeRegionGen(CallbackInfoReturnable<RegionChunkDataGenerator> cir) {
        tfg$syncSeedWorldFromLocalSeed(localSeed);
    }

    /**
     * {@code applyUpdates(false)} pushes preview settings into Create World. When TFCGenViewer Spawn Overlay is ON, enable
     * viewer spawn preset so {@link su.terrafirmagreg.core.mixins.common.tfc.ForgeEventHandlerMixin#onCreateWorldSpawn} honors
     * preview center without climate retries. When overlay is OFF, keep the Game Tab spawn preset; clear a stale viewer id.
     */
    @Inject(method = "applyUpdates(Z)V", at = @At("TAIL"), remap = false)
    private void tfg$afterApplyUpdatesToParent(boolean local, CallbackInfo ci) {
        if (!local) {
            final boolean overlayOn = spawnOverlay != null && spawnOverlay.get();
            if (overlayOn) {
                TFGConfig.COMMON.NEW_WORLD_SPAWN.set(CustomSpawnHelper.VIEWER_SPAWN_ID);
            } else if (CustomSpawnHelper.VIEWER_SPAWN_ID.equals(TFGConfig.COMMON.NEW_WORLD_SPAWN.get())) {
                TFGConfig.COMMON.NEW_WORLD_SPAWN.set(CustomSpawnHelper.DEFAULT_SPAWN.id());
            }
            Minecraft.getInstance().execute(CustomSpawnHelper.CreateWorldSpawnCycle::syncFromConfig);
        }
    }

    @Unique
    private static void tfg$syncSeedWorldFromLocalSeed(String seedString) {
        Seed.worldSeed = WorldOptions.parseSeed(seedString).orElse(WorldOptions.randomSeed());
    }
}
