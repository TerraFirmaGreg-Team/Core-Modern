package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.talrey.createdeco.blocks.ShippingContainerBlock;
import com.simibubi.create.content.logistics.vault.ItemVaultBlock;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = ItemVaultBlockEntity.class, remap = false)
public class ItemVaultBlockEntityMixin {

    /**
     * Fixes Create Deco's shipping containers getting the wrong block axis, causing items to either disappear or dupe.
     * Adapted from https://github.com/talrey/CreateDeco/pull/269 which is for 1.21, since Create Deco is abandoned (and ARR).
     */

    @Redirect(method = "initCapability", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/vault/ItemVaultBlock;getVaultBlockAxis(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/Direction$Axis;"))
    private Direction.Axis tfg$initCapability(BlockState state) {
        if (((Object) this) instanceof ShippingContainerBlock.Entity) {
            return ShippingContainerBlock.getVaultBlockAxis(state);
        } else {
            return ItemVaultBlock.getVaultBlockAxis(state);
        }
    }
}
