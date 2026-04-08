package su.terrafirmagreg.core.mixins.common.gtceu.bedrock_fluids;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.common.data.GTBedrockFluids;

import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.common.data.tfgt.TFGBedrockFluidClimates;
import su.terrafirmagreg.core.common.data.tfgt.TFGBedrockFluids;

@Mixin(value = GTBedrockFluids.class, remap = false)
public class GTBedrockFluidsMixin {

    @Final
    @Shadow
    public static Map<ResourceLocation, BedrockFluidDefinition> toReRegister;

    @Inject(method = "init", at = @At("HEAD"))
    private static void tfg$initInject(CallbackInfo ci) {
        TFGBedrockFluids.init();
        TFGBedrockFluidClimates.init();

        toReRegister.remove(GTCEu.id("heavy_oil_deposit"));
        toReRegister.remove(GTCEu.id("light_oil_deposit"));
        toReRegister.remove(GTCEu.id("oil_deposit"));
        toReRegister.remove(GTCEu.id("raw_oil_deposit"));
        toReRegister.remove(GTCEu.id("salt_water_deposit"));
        toReRegister.remove(GTCEu.id("natural_gas_deposit"));
    }

}
