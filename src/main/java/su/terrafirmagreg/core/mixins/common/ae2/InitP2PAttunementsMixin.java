package su.terrafirmagreg.core.mixins.common.ae2;

import appeng.api.features.P2PTunnelAttunement;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;
import appeng.init.internal.InitP2PAttunements;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InitP2PAttunements.class)
public class InitP2PAttunementsMixin {


    @Inject(method = "init", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$init(CallbackInfo ci){

        P2PTunnelAttunement.registerAttunementTag(AEParts.ME_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.FE_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.REDSTONE_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.FLUID_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.ITEM_P2P_TUNNEL);
        P2PTunnelAttunement.registerAttunementTag(AEParts.LIGHT_P2P_TUNNEL);

        P2PTunnelAttunement.registerAttunementApi(P2PTunnelAttunement.FLUID_TUNNEL,
                ForgeCapabilities.FLUID_HANDLER,
                GuiText.P2PAttunementFluid.text());

        ci.cancel();
    }
}
