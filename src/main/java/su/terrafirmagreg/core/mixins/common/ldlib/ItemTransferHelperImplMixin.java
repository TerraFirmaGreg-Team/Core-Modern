package su.terrafirmagreg.core.mixins.common.ldlib;
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
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
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


// This mixin is used to fix compatibility between TFC, GTCEu-M & AE2
// GTMachines produce items with their capabilities lazily initialized
// When the items get transferred to a AE2 system the item capabilities remain unresolved
// When the items get converted to AEKey they conflict with the items with resolved capabilities
// To fix this, we resolve the capabilities for GTCEu items registered to contain the heat capability before they get transfered
// Note: This fires every time a GT item is transferred by a machine
//
// Note: Deprecated for versions of GTCEu-Modern 1.5+
// Alternate solution for 1.2.3.a can be found in NotifiableItemHandlerMixin.java
@Mixin(value = ItemTransferHelperImpl.class, remap = false)
public abstract class ItemTransferHelperImplMixin {



	@Inject(
        method = "exportToTarget", 
        at = @At(
            // The point the actual item is transferred is within LDlib. We inject the capability resolution right before the item gets inserted
            value = "INVOKE", 
            target = "Lcom/lowdragmc/lowdraglib/side/item/forge/ItemTransferHelperImpl;insertItem(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", 
            ordinal = 1 // The first call of insertItem tests if the items can be inserted, the second call actually inserts the item
        ), 
        locals = LocalCapture.CAPTURE_FAILHARD, // Used to capture the sourceStack variable
        cancellable = false // We do not alter the normal control flow of the function
    )
    private static void injectExportToTarget(
            IItemTransfer source, int maxAmount, Predicate<ItemStack> predicate, Level level, BlockPos pos, @Nullable Direction direction,
            CallbackInfo ci, BlockEntity blockEntity, Optional<IItemHandler> cap, IItemHandler target, int srcIndex, ItemStack sourceStack) {
        if (!sourceStack.isEmpty() ) {
            // The materials that can be heated and contain the heat capabiltiy are registered in TGMaterialHandler.java
            // We can check if the item is registered when the material contains the TFC_PROPERTY tag
            Material material = ChemicalHelper.getMaterial(sourceStack).material();
            if(material.hasProperty(TFGPropertyKeys.TFC_PROPERTY)){
                // Resolve the capabilities before they get inserted
                sourceStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve();
            }
        }
        
    }

}


