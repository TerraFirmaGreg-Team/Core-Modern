package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.glue.SuperGlueHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;

@Mixin(value = SuperGlueHandler.class, remap = false)
public abstract class SuperGlueHandlerMixin {
    /**
     * @author Ujhik
     * @reason To add exclusions to the superglue offhand behavior of creating a glue region when using main hand items
     */
    @Inject(method = "glueInOffHandAppliesOnBlockPlace", at = @At("HEAD"), cancellable = true)
    private static void tfg$superGlueOffhandExclusions(BlockEvent.EntityPlaceEvent event, BlockPos pos, Player player, CallbackInfo ci) {
        ItemStack itemStackMainHand = player.getMainHandItem();
        if (itemStackMainHand.is(Tags.Items.SEEDS) || itemStackMainHand.is(ItemTags.TOOLS)) {
            ci.cancel();
        }
    }
}
