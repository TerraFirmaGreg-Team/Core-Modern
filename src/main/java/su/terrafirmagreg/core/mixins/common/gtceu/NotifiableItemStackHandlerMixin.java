package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraftforge.common.capabilities.ForgeCapabilities;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import su.terrafirmagreg.core.compat.gtceu.TFGPropertyKeys;


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

    @Redirect(
        method = "handleRecipe", 
        at = @At(
            value = "INVOKE", 
            target = "Lcom/gregtechceu/gtceu/api/transfer/item/CustomItemStackHandler;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
            ordinal = 0
        )
    )
    private static ItemStack injectHandleIngredient(CustomItemStackHandler capability, int slot, ItemStack stack, boolean simulated) {
        // The materials that can be heated and contain the heat capabiltiy are registered in TGMaterialHandler.java
        // We can check if the item is registered when the material contains the TFC_PROPERTY tag
        
        if(!simulated){
            MaterialStack materialStack = ChemicalHelper.getMaterial(stack);
            if(materialStack != null){
            Material material = materialStack.material();
                if(material != null && material.hasProperty(TFGPropertyKeys.TFC_PROPERTY)){
                    // Force capability resolution immediately after copying
                    stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
                        // Just accessing it ensures it's initialized
                    });
                }
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


