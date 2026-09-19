package su.terrafirmagreg.core.mixins.common.sophisticatedcore;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.common.capabilities.ItemStackCapabilitySync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Sophisticated Core syncs storage slots via {@code PacketHelper} instead of
 * {@link FriendlyByteBuf#writeItemStack}. That path skips TFC's
 * {@link ItemStackCapabilitySync}, so food spoilage and heat tooltips look reset
 * in backpack/storage GUIs until the stack is picked up (vanilla carried sync).
 * <p>
 * Mirrors {@link net.dries007.tfc.mixin.FriendlyByteBufMixin}: wrap the NBT
 * write/read used by the packet codec with {@link ItemStackCapabilitySync}.
 *
 * @see <a href="https://github.com/TerraFirmaGreg-Team/Modpack-Modern/issues/4787">Modpack-Modern#4787</a>
 */
@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.network.PacketHelper", remap = false)
public class PacketHelperMixin {

    @Redirect(
        method = "writeItemStack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;writeNbt(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/network/FriendlyByteBuf;",
            remap = true
        ),
        remap = false
    )
    private static FriendlyByteBuf tfg$writeSyncableCapabilityData(
        FriendlyByteBuf buffer,
        @Nullable CompoundTag tag,
        ItemStack stack
    ) {
        return buffer.writeNbt(ItemStackCapabilitySync.writeToNetwork(stack, tag));
    }

    @Redirect(
        method = "readItemStack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;readShareTag(Lnet/minecraft/nbt/CompoundTag;)V",
            remap = false
        ),
        remap = false
    )
    private static void tfg$readSyncableCapabilityData(ItemStack stack, @Nullable CompoundTag tag) {
        ItemStackCapabilitySync.readFromNetwork(stack, tag);
    }
}
