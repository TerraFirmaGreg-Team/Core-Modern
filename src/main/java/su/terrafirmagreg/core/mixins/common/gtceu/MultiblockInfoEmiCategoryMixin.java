package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiRecipe;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.EmiRegistry;

@Mixin(value = MultiblockInfoEmiCategory.class)
public class MultiblockInfoEmiCategoryMixin {

    @Unique
    private final static List<ResourceLocation> tfg$excludedMultis = List.of(
            GTCEu.id("primitive_pump"),
            GTCEu.id("charcoal_pile_igniter"),
            GTCEu.id("active_transformer"),
            GTCEu.id("primitive_blast_furnace"));

    @Inject(method = "registerDisplays", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void tfg$registerDisplays(EmiRegistry registry, CallbackInfo ci) {
        GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoEmiRecipe::new)
                .filter(multi -> !tfg$excludedMultis.contains(multi.getId()))
                .forEach(registry::addRecipe);
        ci.cancel();
    }
}
