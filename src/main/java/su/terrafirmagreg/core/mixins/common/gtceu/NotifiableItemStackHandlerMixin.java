package su.terrafirmagreg.core.mixins.common.gtceu;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;

import net.minecraftforge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.llamalad7.mixinextras.sugar.Local;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.entity.BlockEntity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import java.util.Optional;
import java.util.Set;
import net.dries007.tfc.common.items.TFCItems;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import su.terrafirmagreg.core.compat.gtceu.TFGPropertyKeys;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.DummyCraftingContainer;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;


// This mixin is used to fix compatibility between TFC, GTCEu-M & AE2
// GTMachines produce items with their capabilities lazily initialized
// When the items get transferred to a AE2 system the item capabilities remain unresolved
// When the items get converted to AEKey they conflict with the items with resolved capabilities
// To fix this, we resolve the capabilities for GTCEu items registered to contain the heat capability before they get transfered
//
// Note: Deprecated for versions of GTCEu-Modern 1.5+
// Slight alteration required
@Mixin(value = NotifiableItemStackHandler.class, remap = false)
public abstract class NotifiableItemStackHandlerMixin {


    //THIS VERSION WORKS, but runs twice on versions before GTCEu-M 1.5
    //TO update to GTCEu-M 1.5+ replace the method field with handleRecipe

    // @Redirect(
    //     method = "handleIngredient", // method = "handleRecipe", for GTCEu-M 1.5+
    //     at = @At(
    //         value = "INVOKE", 
    //         target = "Lcom/lowdragmc/lowdraglib/misc/ItemStackTransfer;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", //for GTCEu-M 1.5+ target = "Lcom/gregtechceu/gteu/api/transfer/item/CustomItemStackHandler;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
    //         ordinal = 0
    //     )
    // )
    private static ItemStack injectHandleIngredient(ItemStackTransfer capability, int slot, ItemStack stack, boolean simulated) {
        // The materials that can be heated and contain the heat capabiltiy are registered in TGMaterialHandler.java
        // We can check if the item is registered when the material contains the TFC_PROPERTY tag
        
        if(!simulated){

            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerChatMessage message = PlayerChatMessage.unsigned(player.getUUID(), "non simulated");
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(message), false, ChatType.bind(ChatType.CHAT, player));
            } 

            Material material = ChemicalHelper.getMaterial(stack).material();
            if(material.hasProperty(TFGPropertyKeys.TFC_PROPERTY)){
                // Force capability resolution immediately after copying
                stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
                    // Just accessing it ensures it's initialized
                });
            }
        }else{
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerChatMessage message = PlayerChatMessage.unsigned(player.getUUID(), "simulated");
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(message), false, ChatType.bind(ChatType.CHAT, player));
            } 
        }
            
        ItemStack returnStack = capability.insertItem(slot, stack, simulated);
        return returnStack;
    }
}

// These are just some versions that might work, but dont

 // // @Redirect(
    // //     method = "handleIngredient", 
    // //     at = @At(
    // //         value = "INVOKE", 
    // //         target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
    // //         ordinal = 0
    // //     )
    // // )
    // // private static ItemStack removeCopy(ItemStack stack) {
    // //     // Remove it to save resources
    // //     return null;
    // // }

    // @Redirect(
    //     method = "handleIngredient", 
    //     at = @At(
    //         // The point the actual item is transferred is within LDlib. We inject the capability resolution right before the item gets inserted
    //         value = "INVOKE", 
    //         target = "Lcom/lowdragmc/lowdraglib/misc/ItemStackTransfer;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
    //         ordinal = 0
    //     )
    // )
    // private static ItemStack redirectInsertItem(ItemStackTransfer capability, int slot, ItemStack stack, boolean simulate){
    //     return ItemStack.EMPTY;
    // } 


	// @Inject(
    //     method = "handleIngredient", 
    //     at = @At(
    //         // The point the actual item is transferred is within LDlib. We inject the capability resolution right before the item gets inserted
    //         value = "INVOKE", 
    //         target = "Lcom/lowdragmc/lowdraglib/misc/ItemStackTransfer;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
    //         ordinal = 0,
    //         shift = At.Shift.AFTER
    //     ),
    //     locals = LocalCapture.CAPTURE_FAILHARD // Used to capture the simulate variable
    // )
    // private static void injectHandleIngredient(
    //     IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate, IO handlerIO, ItemStackTransfer storage,
    //     CallbackInfoReturnable<List<Ingredient>> cil, @Local(ordinal=0) ItemStack output, @Local(ordinal = 1) LocalRef<ItemStack> leftStack
    // ) {
    //     ItemStack stack = output.copy();
    //     // The materials that can be heated and contain the heat capabiltiy are registered in TGMaterialHandler.java
    //     // We can check if the item is registered when the material contains the TFC_PROPERTY tag
    //     if(!simulate){
    //         Player player = Minecraft.getInstance().player;
    //         if (player != null) {
    //             PlayerChatMessage message = PlayerChatMessage.unsigned(player.getUUID(), "non simulated");
    //             player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(message), false, ChatType.bind(ChatType.CHAT, player));
    //         } 

    //         Material material = ChemicalHelper.getMaterial(stack).material();
    //         if(material.hasProperty(TFGPropertyKeys.TFC_PROPERTY)){
    //             // Force capability resolution immediately after copying
    //             stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
    //                 // Just accessing it ensures it's initialized
    //             });
    //         }
    //     }else{
    //         Player player = Minecraft.getInstance().player;
    //         if (player != null) {
    //             PlayerChatMessage message = PlayerChatMessage.unsigned(player.getUUID(), "simulated");
    //             player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(message), false, ChatType.bind(ChatType.CHAT, player));
    //         } 
    //     }
    //     leftStack.set(stack);
    // }


