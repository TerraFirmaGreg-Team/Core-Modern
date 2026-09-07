/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.IngotPileBlock;
import net.dries007.tfc.common.capabilities.Capabilities;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;

public class IngotPileBlockEntity extends TFCBlockEntity
{
    private final List<Entry> entries;

    private long lastInsertTick = -1;
    private long lastExtractTick = -1;
    private final LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(IngotPileInventory::new);

    public IngotPileBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.INGOT_PILE.get(), pos, state);

        entries = new ArrayList<>();
    }

    public void addIngot(ItemStack stack)
    {
        addIngots(stack);
    }

    public void addIngots(ItemStack stack)
    {
        while (!stack.isEmpty())
        {
            entries.add(new Entry(stack.split(1)));
        }
        markForSync();
    }

    public void removeAllIngots(Consumer<ItemStack> ingotConsumer)
    {
        for (Entry entry : this.entries)
        {
            ingotConsumer.accept(entry.stack);
        }
        this.entries.clear();
        markForSync();
    }

    public ItemStack removeIngot()
    {
        final List<ItemStack> removed = removeIngots(1);
        return removed.isEmpty() ? ItemStack.EMPTY : removed.get(0);
    }

    public List<ItemStack> removeIngotStack()
    {
        if (entries.isEmpty()) return List.of();
        ItemStack lastStack = entries.get(entries.size() - 1).stack;
        return removeIngots(lastStack.getMaxStackSize());
    }

    public List<ItemStack> removeIngots(int count)
    {
        final List<ItemStack> removed = new ArrayList<>();
        if (!entries.isEmpty())
        {
            final ItemStack lastStack = entries.get(entries.size() - 1).stack;
            for (int i = entries.size() - 1; i >= 0 && removed.size() < count; i--)
            {
                final ItemStack currentStack = entries.get(i).stack;
                if (ItemStack.isSameItemSameTags(lastStack, currentStack))
                {
                    removed.add(entries.remove(i).stack);
                }
                else
                {
                    break;
                }
            }
            markForSync();
        }
        return removed;
    }

    /**
     * Returns a cached metal for the given side, if present, otherwise grabs from the cache.
     * The metal is defined by checking what metal the stack would melt into if heated.
     * Any other items turn into {@link Metal#unknown()}.
     */
    public Metal getOrCacheMetal(int index)
    {
        if (index >= entries.size())
        {
            return Metal.unknown();
        }

        final Entry entry;
        try
        {
            entry = entries.get(index);
        }
        catch (IndexOutOfBoundsException e)
        {
            // This is terrible, but it's a threadsafety issue. `entries` might be updated between the bounds check above, and this query
            return Metal.unknown();
        }

        if (entry.metal == null)
        {
            entry.metal = Metal.getFromIngot(entry.stack);
            if (entry.metal == null)
            {
                entry.metal = Metal.unknown();
            }
        }
        return entry.metal;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        final ListTag stacks = new ListTag();
        for (final Entry entry : entries)
        {
            stacks.add(entry.stack.save(new CompoundTag()));
        }
        tag.put("stacks", stacks);
        super.saveAdditional(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag)
    {
        entries.clear();
        final ListTag list = tag.getList("stacks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            entries.add(new Entry(ItemStack.of(list.getCompound(i))));
        }
        super.loadAdditional(tag);
    }

    public void fillTooltip(Consumer<Component> tooltip)
    {
        final Object2IntMap<Metal> mapMetals = new Object2IntOpenHashMap<>();
        final Object2IntMap<Material> mapMaterials = new Object2IntOpenHashMap<>();
        final Object2IntMap<Component> mapComponents = new Object2IntOpenHashMap<>();
        int numberOfUnknown = 0;

        for (int i = 0; i < entries.size(); i++)
        {
            final Metal metal = getOrCacheMetal(i);
            if (metal != Metal.unknown())
            {
                mapMetals.mergeInt(metal, 1, Integer::sum);
                continue;
            }

            final ItemStack stack = entries.get(i).stack;
            final MaterialStack materialStack = ChemicalHelper.getMaterialStack(stack);
            if (!materialStack.isEmpty())
            {
                mapMaterials.mergeInt(materialStack.material(), 1, Integer::sum);
                continue;
            }

            final Component nameComponent = stack.getHoverName();
            if (!nameComponent.getString().isEmpty())
            {
                mapComponents.mergeInt(nameComponent, 1, Integer::sum);
                continue;
            }

            if (metal == Metal.unknown())
            {
                mapMetals.mergeInt(metal, 1, Integer::sum);
                continue;
            }
            numberOfUnknown++;
        }

        mapMetals.forEach((metal, ct) -> tooltip.accept(Component.literal(ct + "x ").append(metal.getDisplayName())));
        mapMaterials.forEach((material, ct) -> tooltip.accept(Component.literal(ct + "x ").append(material.getLocalizedName())));
        mapComponents.forEach((component, ct) -> tooltip.accept(Component.literal(ct + "x ").append(component)));

        if (numberOfUnknown > 0)
        {
            tooltip.accept(Component.literal(numberOfUnknown + "x ").append(Metal.unknown().getDisplayName().copy().withStyle(ChatFormatting.RED)));
        }
    }

    public ItemStack getPickedItemStack()
    {
        return entries.isEmpty() ? ItemStack.EMPTY : entries.get(0).stack.copy();
    }

    public ItemStack getEntryStack(int index)
    {
        return index >= 0 && index < entries.size() ? entries.get(index).stack : ItemStack.EMPTY;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
    {
        if (cap == Capabilities.ITEM)
        {
            return inventoryCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        inventoryCapability.invalidate();
    }

    private class IngotPileInventory implements IItemHandler
    {
        @Override
        public int getSlots()
        {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return getPickedItemStack();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (stack.isEmpty() || !isItemValid(slot, stack))
            {
                return stack;
            }

            if (level != null)
            {
                long time = level.getGameTime();
                if (time == lastInsertTick)
                {
                    return stack;
                }

                // Find the first pile in the stack that has room.
                BlockPos targetPos = worldPosition;
                BlockState targetState = getBlockState();
                if (targetState.getBlock() instanceof IngotPileBlock block)
                {
                    IntegerProperty countProperty = block.getCountProperty();
                    int maxCount = countProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(64);

                    while (targetState.getValue(countProperty) == maxCount)
                    {
                        BlockPos abovePos = targetPos.above();
                        BlockState aboveState = level.getBlockState(abovePos);
                        if (Helpers.isBlock(aboveState, block))
                        {
                            targetPos = abovePos;
                            targetState = aboveState;
                        }
                        else
                        {
                            // Stack is full (or at least this block and everything above that is a pile).
                            return stack;
                        }
                    }

                    if (level.getBlockEntity(targetPos) instanceof IngotPileBlockEntity targetPile)
                    {
                        int currentCount = targetPile.entries.size();
                        if (currentCount < maxCount)
                        {
                            if (!simulate)
                            {
                                lastInsertTick = time;
                                ItemStack toInsert = stack.copy();
                                toInsert.setCount(1);
                                targetPile.addIngots(toInsert);

                                // Update block state
                                level.setBlock(targetPos, targetState.setValue(countProperty, currentCount + 1), 3);
                            }
                            ItemStack result = stack.copy();
                            result.shrink(1);
                            return result;
                        }
                    }
                }
            }
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (amount <= 0)
            {
                return ItemStack.EMPTY;
            }

            if (level != null)
            {
                long time = level.getGameTime();
                if (time == lastExtractTick)
                {
                    return ItemStack.EMPTY;
                }

                // Find top pile.
                BlockPos topPos = worldPosition;
                Block block = getBlockState().getBlock();
                while (Helpers.isBlock(level.getBlockState(topPos.above()), block))
                {
                    topPos = topPos.above();
                }

                if (level.getBlockEntity(topPos) instanceof IngotPileBlockEntity topPile)
                {
                    if (topPile.entries.isEmpty())
                    {
                        return ItemStack.EMPTY;
                    }

                    if (!simulate)
                    {
                        lastExtractTick = time;
                        ItemStack result = topPile.removeIngot();

                        // Update block state.
                        BlockState topState = level.getBlockState(topPos);
                        if (topState.getBlock() instanceof IngotPileBlock pileBlock)
                        {
                            int newCount = topPile.entries.size();
                            if (newCount == 0)
                            {
                                level.removeBlock(topPos, false);
                            }
                            else
                            {
                                level.setBlock(topPos, topState.setValue(pileBlock.getCountProperty(), newCount), 3);
                            }
                        }
                        return result;
                    }
                    else
                    {
                        ItemStack result = topPile.entries.get(topPile.entries.size() - 1).stack.copy();
                        result.setCount(1);
                        return result;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            BlockState state = getBlockState();
            if (state.getBlock() == TFCBlocks.INGOT_PILE.get())
            {
                if (!stack.is(TFCTags.Items.PILEABLE_INGOTS))
                {
                    return false;
                }
            }
            else if (state.getBlock() == TFCBlocks.DOUBLE_INGOT_PILE.get())
            {
                if (!stack.is(TFCTags.Items.PILEABLE_DOUBLE_INGOTS))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }

            if (entries.isEmpty())
            {
                return true;
            }
            return ItemStack.isSameItemSameTags(entries.get(0).stack, stack);
        }
    }

    static class Entry
    {
        final ItemStack stack;
        @Nullable Metal metal;

        Entry(ItemStack stack)
        {
            this.stack = stack;
        }
    }
}
