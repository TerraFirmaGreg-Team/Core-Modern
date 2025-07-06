package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTRecipeTypes.class, remap = false)
public abstract class GTRecipeTypesMixin {

    @Shadow @Final public static GTRecipeType CUTTER_RECIPES;
    @Shadow @Final public static GTRecipeType LASER_ENGRAVER_RECIPES;
    @Shadow @Final public static GTRecipeType CHEMICAL_RECIPES;
    @Shadow @Final public static GTRecipeType ARC_FURNACE_RECIPES;
    @Shadow @Final public static GTRecipeType MACERATOR_RECIPES;
    @Shadow @Final public static GTRecipeType CHEMICAL_BATH_RECIPES;
    @Shadow @Final public static GTRecipeType CENTRIFUGE_RECIPES;
    @Shadow @Final public static GTRecipeType COMPRESSOR_RECIPES;

    @Shadow @Final public static GTRecipeType IMPLOSION_RECIPES;

    @Shadow @Final public static GTRecipeType FERMENTING_RECIPES;

    /**
     * Нужно для того, чтобы настроить доп. рецепты.
     * Расширяет кол-во слотов в некоторых машинах.
     * */
    @Inject(method = "init", at = @At(value = "TAIL"), remap = false)
    private static void tfg$init(CallbackInfo ci) {

        // Добавляет новый слот под микросхему
        LASER_ENGRAVER_RECIPES.setMaxIOSize(3, 1, 0, 0);
        LASER_ENGRAVER_RECIPES.setSlotOverlay(false, false, true, GuiTextures.CIRCUIT_OVERLAY);

        // Добавляет новый слот под микросхему
        CHEMICAL_RECIPES.setMaxIOSize(3, 2, 3, 2);
        CHEMICAL_RECIPES.setSlotOverlay(false, false, true, GuiTextures.CIRCUIT_OVERLAY);

        ARC_FURNACE_RECIPES.setMaxIOSize(2, 9, 1, 0);

        MACERATOR_RECIPES.setMaxIOSize(1, 6, 0, 0);
        MACERATOR_RECIPES.setUiBuilder((recipe, widgetGroup) -> {
            widgetGroup.addWidget(new LabelWidget(3, widgetGroup.getSize().height - 38, Component.translatable("tfg.recipe.macerator_warning"))
                .setTextColor(0x555555)
                .setDropShadow(false));
        });

        CHEMICAL_BATH_RECIPES.setMaxIOSize(2, 6, 1, 1);

        CENTRIFUGE_RECIPES.setMaxIOSize(3, 6, 1, 6);
        CENTRIFUGE_RECIPES.setSlotOverlay(false, false, true, GuiTextures.CIRCUIT_OVERLAY);

        COMPRESSOR_RECIPES.setMaxIOSize(2, 1, 3, 1);
        COMPRESSOR_RECIPES.setSlotOverlay(false, false, true, GuiTextures.CIRCUIT_OVERLAY);
        COMPRESSOR_RECIPES.setSlotOverlay(false, true, false, GuiTextures.COMPRESSOR_OVERLAY);
        COMPRESSOR_RECIPES.setSlotOverlay(false, true, true, GuiTextures.COMPRESSOR_OVERLAY);

        IMPLOSION_RECIPES.setMaxIOSize(6, 2, 0, 0);

        FERMENTING_RECIPES.setMaxIOSize(2, 1, 1, 1);
        FERMENTING_RECIPES.setSlotOverlay(false, false, true, GuiTextures.CIRCUIT_OVERLAY);
    }
}
