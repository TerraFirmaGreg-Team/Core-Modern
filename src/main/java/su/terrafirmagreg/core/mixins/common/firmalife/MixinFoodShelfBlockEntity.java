package su.terrafirmagreg.core.mixins.common.firmalife;


import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoodShelfBlockEntity.class)
public abstract class MixinFoodShelfBlockEntity extends InventoryBlockEntity<ItemStackHandler> implements ClimateReceiver {

    @Shadow private boolean climateValid;

    @Shadow protected abstract void updatePreservation(boolean climateValid);
    @Shadow protected abstract FoodTrait getFoodTrait();

    protected MixinFoodShelfBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                        InventoryFactory<ItemStackHandler> inventoryFactory,
                                        Component defaultName) {
        super(type, pos, state, inventoryFactory, defaultName);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true,remap = false)
    private void tfg$use(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        assert level != null;
        var res = InteractionResult.PASS;
        final ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && isItemValid(0, held))
        {
            if (climateValid)
            {
                FoodCapability.applyTrait(held, ((FoodShelfBlockEntity)(Object)  this).getFoodTrait());
            }
            player.setItemInHand(hand, Helpers.mergeInsertStack(inventory, 0, held));

            FoodCapability.removeTrait(player.getItemInHand(hand),getFoodTrait());
            res = InteractionResult.sidedSuccess(level.isClientSide);
        }
        else if (held.isEmpty())
        {
            ItemStack stack = inventory.extractItem(0, player.isShiftKeyDown() ? Integer.MAX_VALUE : 1, false);
            if (stack.isEmpty()) cir.setReturnValue(InteractionResult.PASS);
            FoodCapability.removeTrait(stack, getFoodTrait());
            FoodCapability.setCreationDate(stack,FoodCapability.getRoundedCreationDate());
            ItemHandlerHelper.giveItemToPlayer(player, stack);
            res = InteractionResult.sidedSuccess(level.isClientSide);
        }
        updatePreservation(climateValid);
        markForSync();
        cir.setReturnValue(res);
    }
}

