package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;

import earth.terrarium.adastra.client.utils.DimensionRenderingUtils;

import su.terrafirmagreg.core.TFGCore;

@Mixin(value = DimensionRenderingUtils.class, remap = false)
public class PlanetResourceUtilsMixin {

    @Shadow
    @Final
    @Mutable
    public static ResourceLocation MERCURY;

    @Final
    @Shadow
    public static ResourceLocation VENUS;

    @Final
    @Shadow
    public static ResourceLocation EARTH;

    @Final
    @Shadow
    public static ResourceLocation MARS;

    @Final
    @Shadow
    @Mutable
    public static List<ResourceLocation> SOLAR_SYSTEM_TEXTURES;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void tfg$modifyFinalVars(CallbackInfo ci) {
        MERCURY = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/mercury.png");
        SOLAR_SYSTEM_TEXTURES = List.of(MERCURY, VENUS, EARTH, MARS);
    }
}
