package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolDefinitionBuilder;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.HarvestIceBehavior;

import su.terrafirmagreg.core.compat.gtceu.behaviors.CanoeCreatorBehavior;

@Mixin(value = GTToolType.class, remap = false)
public abstract class GTToolTypeMixin {

    // TODO: ПОЧИНИТЬ!
    /**
     *
     */
//    @Redirect(method = "lambda$static$7", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/item/tool/ToolDefinitionBuilder;behaviors([Lcom/gregtechceu/gtceu/api/item/tool/behavior/IToolBehavior;)Lcom/gregtechceu/gtceu/api/item/tool/ToolDefinitionBuilder;"), remap = false)
//    private static ToolDefinitionBuilder tfg$clinit7(ToolDefinitionBuilder instance, IToolBehavior[] behaviours) {
//        return instance.behaviors(HarvestCropsBehavior.INSTANCE);
//    }

    /**
     * Устанавливает новое поведение для пилы, чтобы та могла создавать лодки каное из FirmaCiv.
     */
    @Redirect(method = "lambda$static$8", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/item/tool/ToolDefinitionBuilder;behaviors([Lcom/gregtechceu/gtceu/api/item/tool/behavior/IToolBehavior;)Lcom/gregtechceu/gtceu/api/item/tool/ToolDefinitionBuilder;"), remap = false)
    private static ToolDefinitionBuilder tfg$clinit$saw(ToolDefinitionBuilder instance, IToolBehavior[] behaviours) {
        return instance.behaviors(HarvestIceBehavior.INSTANCE, CanoeCreatorBehavior.INSTANCE);
    }
}
