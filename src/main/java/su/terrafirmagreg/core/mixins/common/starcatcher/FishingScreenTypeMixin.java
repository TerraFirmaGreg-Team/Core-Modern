package su.terrafirmagreg.core.mixins.common.starcatcher;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.compat.starcatcher.FishingScreenType;
import su.terrafirmagreg.core.compat.starcatcher.FishingScreenTypeUtils;

/**
 * Mixin to replace Starcatcher's tank texture with custom TFG logic.
 */
@Pseudo
@Mixin(targets = "com.wdiscute.starcatcher.minigame.FishingMinigameScreen", remap = false)
public class FishingScreenTypeMixin {

    @Shadow(remap = false)
    public ResourceLocation tankTexture;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false, require = 0)
    private void tfg$useCustomTankTexture(CallbackInfo ci) {
        if (!ModList.get().isLoaded("starcatcher")) {
            return;
        }

        try {
            FishingScreenType screenType = FishingScreenTypeUtils.determineScreenType();
            this.tankTexture = screenType.getTexture();
        } catch (Exception e) {
            TFGCore.LOGGER.error("TFG-Core: Error in tank texture mixin: {}", e.getMessage());
        }
    }
}
