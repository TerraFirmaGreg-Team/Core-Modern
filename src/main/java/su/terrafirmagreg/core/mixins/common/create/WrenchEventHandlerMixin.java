package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.WrenchEventHandler;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@Mixin(WrenchEventHandler.class)
public class WrenchEventHandlerMixin {
    @Inject(method = "useOwnWrenchLogicForCreateBlocks", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$allowWrenchTagForCreateBlocks(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (AllTags.AllItemTags.WRENCH.matches(event.getItemStack()) && be instanceof KineticBlockEntity)
            ci.cancel();
    }
}
