package su.terrafirmagreg.core.mixins.client.firmalife;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;
import com.eerussianguy.firmalife.common.blocks.greenhouse.LargePlanterBlock;
import com.eerussianguy.firmalife.common.util.Plantable;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Show plants primary nutrients for Firmalife greenhouse planter blocks
 */
@Pseudo
@Mixin(value = LargePlanterBlock.class, remap = false)
public class FLLargePlanterBlockMixin {
    @Inject(method = "addHoeOverlayInfo", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 3))
    public void tfg$addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean debug, CallbackInfo ci, @Local(name = "slot") int slot) {
        Plantable plantable = ((LargePlanterBlockEntity) level.getBlockEntity(pos)).getPlantable(slot);
        if (plantable != null) {
            text.add(Component.translatable("tfg.tooltip.crop.fertilizer").append(Component.translatable(
                    switch (plantable.getPrimaryNutrient()) {
                        case NITROGEN -> "tfg.tooltip.fertilizer.nitrogen";
                        case POTASSIUM -> "tfg.tooltip.fertilizer.potassium";
                        case PHOSPHOROUS -> "tfg.tooltip.fertilizer.phosphorus";
                    })));
        }
    }
}