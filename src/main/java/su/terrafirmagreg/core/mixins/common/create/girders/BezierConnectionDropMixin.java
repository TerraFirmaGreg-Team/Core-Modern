package su.terrafirmagreg.core.mixins.common.create.girders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import su.terrafirmagreg.core.common.block.girder.TFGGirderData;

/***
 * Credit: Create: More Girders
 */
@Mixin(value = BezierConnection.class)
public abstract class BezierConnectionDropMixin implements TFGGirderData {
    @Redirect(method = "addItemsToPlayer", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;asStack(I)Lnet/minecraft/world/item/ItemStack;", ordinal = 0, remap = false), remap = false)
    private ItemStack tfg$replaceGirderStackForPlayer(BlockEntry<?> entry, int count) {
        Block tfgBlock = tfg$getGirderBlock();
        if (tfgBlock != null) {
            return new ItemStack(tfgBlock, count);
        }
        return entry.asStack(count);
    }

    @Redirect(method = "spawnItems", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;asStack()Lnet/minecraft/world/item/ItemStack;", ordinal = 0, remap = false), remap = false)
    private ItemStack tfg$replaceGirderStackForWorld(BlockEntry<?> entry) {
        Block tfgBlock = tfg$getGirderBlock();
        if (tfgBlock != null) {
            return new ItemStack(tfgBlock);
        }
        return entry.asStack();
    }
}
