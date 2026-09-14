/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;

/**
 * This is a manager for capabilities that need to be synced externally, constantly.
 * Most capabilities (Vessel, Mold, Forging, etc.) directly interact with the stack tag, which is synced by vanilla and respects the creative inventory.
 * However, that relies on the capability to be able to detect changes and sync on change.
 * This is <strong>impossible</strong> with {@link HeatCapability} and {@link FoodCapability} as their serialization can change without intervention, so it cannot be saved on modification.
 * <p>
 * A note on why {@link ItemStack#getShareTag()} does not work for this purpose:
 * <ul>
 *     <li>It needs to be overriden for items (which means it works for none of our event attached capabilities)</li>
 *     <li>In the cases where we could use it, it's not needed (because we just stick to using the stack tag which should ALWAYS be synced.)</li>
 *     <li>It's still ineffective in the case of the creative inventory.</li>
 * </ul>
 * So, in order to solve both problems (S -> C sync, and creative inventory C -> S sync):
 * <ul>
 *     <li>We patch the encode item stack to use this method, to sync non-stack-tag capabilities</li>
 *     <li>All other capabilities use the stack tag to avoid sync concerns.</li>
 * </ul>
 * Finally, in order to avoid issues caused by other mods due to incorrectly synced item stacks (see <a href="https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/2198">TerraFirmaCraft#2198</a>), we need to write and read this data in an as unconditional method as possible.
 * This means we cannot check for empty stacks, or those that do not have a capability. In the best case, we write an additional +1 bytes per item stack (a typical item stack has ~4-6 bytes default). This is about as least-cost that we can make it (in the worst case, we write 1 + two nbt tags).
 * <p>
 * We also use a separate capability instance - the {@link HeatCapability#NETWORK_CAPABILITY} and {@link FoodCapability#NETWORK_CAPABILITY}. This is done as to be able to access underlying capability implementations without triggering any initialization which may rely on on-thread resources, such as accessing recipes or recipe caches.
 */
public final class ItemStackCapabilitySync
{
    private static final String FOOD_ID = "tfc:food";
    private static final String HEAT_ID = "tfc:heat";
    private static final String EMPTY_ID = "tfc:empty";

    public static boolean hasSyncableCapability(ItemStack stack)
    {
        return stack.getCapability(FoodCapability.NETWORK_CAPABILITY).isPresent() || stack.getCapability(HeatCapability.NETWORK_CAPABILITY).isPresent();
    }

    @Nullable
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static CompoundTag writeToNetwork(ItemStack stack, @Nullable CompoundTag originalTag)
    {
        // getCapability().resolve() might be called on an uninitialized stack here, which actually involves a mutation to the stack, as
        // capabilities will be initialized for the first time. This initialization is not threadsafe, in general. In particular, during
        // deserialization/serialization of food capabilities, food traits are mutated and iterated through a list.
        //
        // While this is a weird construct, we should be able to synchronize on the item stack itself - this will prevent any stacks
        // from having getCapability invoked and resolved by two threads at once. Different stacks should be fully safe to execute independently.
        //
        // Additionally, we write the extra capability data as part of the original stack tag. This is to preserve compatibility between vanilla
        // clients (see TerraFirmaCraft#2753) by only using the vanilla network protocol. This means we have to do some finicky handling to make
        // sure we don't modify any base cases that may occur.
        synchronized (stack)
        {
            if (hasSyncableCapability(stack))
            {
                final CompoundTag writableTag = originalTag == null
                    ? new CompoundTag()
                    : originalTag.copy(); // Must avoid mutating the original tag

                // Note that the original tag might be empty - if it was, then we flag it here
                if (originalTag != null && originalTag.isEmpty())
                {
                    writableTag.putBoolean(EMPTY_ID, true);
                }
                writeToNetwork(FOOD_ID, FoodCapability.NETWORK_CAPABILITY, stack, writableTag);
                writeToNetwork(HEAT_ID, HeatCapability.NETWORK_CAPABILITY, stack, writableTag);
                return writableTag;
            }
        }
        return originalTag;
    }

    public static void readFromNetwork(ItemStack stack, @Nullable CompoundTag tag)
    {
        if (tag != null)
        {
            // If a stack tag is present, try and read our custom NBT if we added it. We prepare the stack tag *first*, because
            // we don't want to trigger capability loading until `readShareTag()` has been set. This sets the stack tag, and several
            // of our capabilities read state information from said tag when initialized.
            final @Nullable CompoundTag foodTag = readTagFromNetwork(FOOD_ID, tag);
            final @Nullable CompoundTag heatTag = readTagFromNetwork(HEAT_ID, tag);

            if (foodTag != null || heatTag != null)
            {
                // We have modified the original tag, so we need to check for the present-but-empty case
                // If we find an 'empty' flag, the original tag was present but empty. No flag = the original tag was `null`
                final boolean wasEmptyButPresent = tag.getBoolean(EMPTY_ID);

                tag.remove(EMPTY_ID);
                if (!wasEmptyButPresent && tag.isEmpty())
                {
                    tag = null;
                }
            }

            // At this point in normal loading, we read the share tag, setting the stack NBT
            // This must be done *before* we read capabilities, as we want it to be present during cap initialization, which is not lazy (as our
            // immediate query will initialize them).
            stack.readShareTag(tag);

            // Then read capabilities
            readCapabilityFromTag(foodTag, FoodCapability.NETWORK_CAPABILITY, stack);
            readCapabilityFromTag(heatTag, HeatCapability.NETWORK_CAPABILITY, stack);
        }
    }

    private static void writeToNetwork(String networkId, Capability<? extends INBTSerializable<CompoundTag>> capability, ItemStack stack, CompoundTag rootTag)
    {
        stack.getCapability(capability)
            .map(INBTSerializable::serializeNBT)
            .ifPresent(tag -> rootTag.put(networkId, tag));
    }

    @Nullable
    private static CompoundTag readTagFromNetwork(String networkId, CompoundTag tag)
    {
        if (tag.contains(networkId, Tag.TAG_COMPOUND))
        {
            final CompoundTag subTag = tag.getCompound(networkId);
            tag.remove(networkId);
            return subTag;
        }
        return null;
    }

    private static void readCapabilityFromTag(@Nullable CompoundTag tag, Capability<? extends INBTSerializable<CompoundTag>> capability, ItemStack stack)
    {
        if (tag != null)
        {
            stack.getCapability(capability).ifPresent(cap -> cap.deserializeNBT(tag));
        }
    }
}
