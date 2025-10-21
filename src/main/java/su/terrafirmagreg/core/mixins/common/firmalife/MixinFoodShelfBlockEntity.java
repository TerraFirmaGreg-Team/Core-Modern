/*
 * This file mixins code from Firmalife (https://github.com/eerussianguy/firmalife?tab=MIT-1-ov-file)
 * MIT License
 * Copyright (c) 2022 eerussianguy
 */
package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.eerussianguy.firmalife.common.blockentities.FoodShelfBlockEntity;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.dries007.tfc.common.capabilities.food.IFood;
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

/**
 * @author aidie8 & Redeix
 * @reason Enables rounding of food creation date when taking food from shelf.
 *         https://github.com/eerussianguy/firmalife/blob/1.20.x/src/main/java/com/eerussianguy/firmalife/common/blockentities/FoodShelfBlockEntity.java
 */
@Mixin(value = FoodShelfBlockEntity.class, remap = false)
public abstract class MixinFoodShelfBlockEntity extends InventoryBlockEntity<ItemStackHandler> implements ClimateReceiver {

    @Shadow
    private boolean climateValid;

    @Shadow
    public abstract void updatePreservation(boolean climateValid);

    @Shadow
    public abstract FoodTrait getFoodTrait();

    /**
     * Mixin food shelf block entity for date rounding.
     *
     * @param type             BlockEntityType
     * @param pos              BlockPos
     * @param state            BlockState
     * @param inventoryFactory ItemStackHandler
     * @param defaultName      Component
     */
    protected MixinFoodShelfBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            InventoryFactory<ItemStackHandler> inventoryFactory,
            Component defaultName) {
        super(type, pos, state, inventoryFactory, defaultName);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$use(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        assert level != null;
        var res = InteractionResult.PASS;
        final ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && isItemValid(0, held)) {
            // Skip rotten food.
            IFood heldFood = FoodCapability.get(held);
            if (heldFood != null && heldFood.isRotten()) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            if (climateValid) {
                FoodCapability.applyTrait(held, getFoodTrait());
            }
            player.setItemInHand(hand, Helpers.mergeInsertStack(inventory, 0, held));

            // Remove the shelf trait from whatever remains in hand.
            FoodCapability.removeTrait(player.getItemInHand(hand), getFoodTrait());
            res = InteractionResult.sidedSuccess(level.isClientSide);

        } else if (held.isEmpty()) {
            ItemStack stack = inventory.extractItem(0, player.isShiftKeyDown() ? Integer.MAX_VALUE : 1, false);
            if (stack.isEmpty()) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }

            // Remove shelf trait and round the stack's creation date.
            FoodCapability.removeTrait(stack, getFoodTrait());
            IFood food = FoodCapability.get(stack);
            if (food != null) {
                long orig = food.getCreationDate();
                long rounded = FoodCapability.getRoundedCreationDate(orig);
                food.setCreationDate(Math.min(orig, rounded));
            }

            ItemHandlerHelper.giveItemToPlayer(player, stack);
            res = InteractionResult.sidedSuccess(level.isClientSide);
        }

        updatePreservation(climateValid);
        markForSync();
        cir.setReturnValue(res);
    }
}
