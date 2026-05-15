/*
package su.terrafirmagreg.core.mixins.common.starcatcher;


 // Mixin to replace Starcatcher's tank texture with custom TFG logic.
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
*/
