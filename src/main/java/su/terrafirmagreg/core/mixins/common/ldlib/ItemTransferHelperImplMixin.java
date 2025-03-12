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

@Mixin(value = ItemTransferHelperImpl.class, remap = false)
public abstract class ItemTransferHelperImplMixin {


	@Inject(
        method = "exportToTarget", 
        at = @At(
            value = "INVOKE", 
            target = "Lcom/lowdragmc/lowdraglib/side/item/forge/ItemTransferHelperImpl;insertItem(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", 
            ordinal = 1
        ), 
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = false
    )
    private static void injectExportToTarget(
            IItemTransfer source, int maxAmount, Predicate<ItemStack> predicate, Level level, BlockPos pos, @Nullable Direction direction,
            CallbackInfo ci, BlockEntity blockEntity, Optional<IItemHandler> cap, IItemHandler target, int srcIndex, ItemStack sourceStack) {
        if (!sourceStack.isEmpty() ) {
            Material material = ChemicalHelper.getMaterial(sourceStack).material();
            if(material.hasProperty(TFGPropertyKeys.TFC_PROPERTY)){
                sourceStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve();
            }
        }
        
    }

}


/*
        String className = target.getClass().getName();
        boolean targetIsGTCEuHandler = className.startsWith("com.gregtechceu");
		boolean sourceIsGTCEuHandler = source instanceof NotifiableItemStackHandler;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerChatMessage targetIsGTCEuHandlerMessage = PlayerChatMessage.unsigned(player.getUUID(), targetIsGTCEuHandler ? "targetIsGTCEuHandler" : "not Target" + target.getClass().getName());
            PlayerChatMessage sourceIsGTCEuHandlerMessage = PlayerChatMessage.unsigned(player.getUUID(), sourceIsGTCEuHandler ? "sourceIsGTCEuHandler" : "not source");
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(targetIsGTCEuHandlerMessage), false, ChatType.bind(ChatType.CHAT, player));
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(sourceIsGTCEuHandlerMessage), false, ChatType.bind(ChatType.CHAT, player));

        } 

        // If the target is not GTCEu, force capability resolution
        if (!targetIsGTCEuHandler && sourceIsGTCEuHandler) {

            if(player!=null){
                PlayerChatMessage attach = PlayerChatMessage.unsigned(player.getUUID(), "attach");
                player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(attach), false, ChatType.bind(ChatType.CHAT, player));
            }
            if (!sourceStack.isEmpty()) {
                sourceStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve();
                // sourceStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
                //     CompoundTag handlerTag = new CompoundTag();
                //     for (int i = 0; i < handler.getSlots(); i++) {
                //         ItemStack slotStack = handler.getStackInSlot(i);
                //         handlerTag.put("Slot" + i, slotStack.save(new CompoundTag()));
                //     }
                //     sourceStack.getOrCreateTag().put("ForgeCaps", handlerTag);
                // });
            }
        }
*/