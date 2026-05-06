package su.terrafirmagreg.core.common.block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.IngotPileBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.DoubleIngotPileBlock;
import net.dries007.tfc.common.blocks.devices.IngotPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class IngotPedestalBlock extends Block implements EntityBlock {
    // This is the only way to read private fields as far as I know
    static final Field ENTRIES_FIELD;
    static final Field ENTRY_STACK_FIELD;
    static final Constructor<?> ENTRY_CTOR;

    static {
        try {
            ENTRIES_FIELD = declaredField(IngotPileBlockEntity.class, "entries");
            Class<?> entryClass = Class.forName(
                    "net.dries007.tfc.common.blockentities.IngotPileBlockEntity$Entry");
            ENTRY_STACK_FIELD = declaredField(entryClass, "stack");
            ENTRY_CTOR = entryClass.getDeclaredConstructor(ItemStack.class);
            ENTRY_CTOR.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Field declaredField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public IngotPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IngotPedestalBlockEntity(TFGBlockEntities.INGOT_PEDESTAL.get(), pos, state);
    }

    public static class IngotPedestalBlockEntity extends BlockEntity {
        /** Prevent the ingot pile from giving BUs, depends on minecraft being single-threaded. */
        public static boolean SUPPRESSING = false;
        private boolean cacheValid = false;
        /** Cached contiguous stacks of ingots in the pile, last entry represents the ingots at the top. */
        private final ArrayList<ItemStack> stacks = new ArrayList<>();
        /** Position of the topmost IngotPileBlock. Null when the column is empty. */
        @Nullable
        private BlockPos cachedTopPos = null;

        private final LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.of(this::createItemHandler);

        public IngotPedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.ITEM_HANDLER ? itemHandlerOpt.cast() : super.getCapability(cap, side);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            itemHandlerOpt.invalidate();
        }

        /** Invalidate the cache of an ingot pedestal under a pile, called on an external change to the pile. */
        public static void notifyExternalColumnChange(Level level, BlockPos changedPos) {
            if (SUPPRESSING || level.isClientSide())
                return;

            BlockPos cur = changedPos.below();
            while (true) {
                BlockState state = level.getBlockState(cur);
                Block block = state.getBlock();

                if (block instanceof IngotPedestalBlock) {
                    if (level.getBlockEntity(cur) instanceof IngotPedestalBlockEntity be) {
                        be.invalidateCache();
                    }
                    return;
                } else if (block instanceof IngotPileBlock) {
                    cur = cur.below();
                } else {
                    return;
                }
            }
        }

        public void invalidateCache() {
            cacheValid = false;
            stacks.clear();
            cachedTopPos = null;
        }

        private void ensureCache() {
            if (!cacheValid)
                rebuildCache();
        }

        private void rebuildCache() {
            stacks.clear();
            cachedTopPos = null;

            if (level == null) {
                cacheValid = true;
                return;
            }

            cachedTopPos = findTopPilePosRaw();
            if (cachedTopPos == null) {
                cacheValid = true;
                return;
            }

            BlockPos cur = cachedTopPos;

            while (level.getBlockState(cur).getBlock() instanceof IngotPileBlock) {
                if (!(level.getBlockEntity(cur) instanceof IngotPileBlockEntity pile))
                    break;

                List<Object> raw = rawEntries(pile);

                for (int i = raw.size() - 1; i >= 0; i--) {
                    ItemStack stack = entryStack(raw.get(i)).copyWithCount(1);

                    if (stacks.isEmpty()) {
                        stacks.add(stack.copyWithCount(1));
                    } else {
                        ItemStack lastStack = stacks.get(stacks.size() - 1);
                        if (ItemStack.isSameItemSameTags(lastStack, stack)) {
                            lastStack.setCount(lastStack.getCount() + 1);
                        } else {
                            stacks.add(stack.copyWithCount(1));
                        }
                    }
                }
                cur = cur.below();
            }

            cacheValid = true;
        }

        private IItemHandler createItemHandler() {
            return new IItemHandler() {
                @Override
                public int getSlots() {
                    if (level == null)
                        return 1;
                    ensureCache();
                    return stacks.size() + 1;
                }

                /** Slots 0 is accessible, slots 1+ inaccessible, last slot always empty. */
                @Override
                public ItemStack getStackInSlot(int slot) {
                    if (level == null || slot < 0)
                        return ItemStack.EMPTY;
                    ensureCache();
                    if (slot >= stacks.size())
                        return ItemStack.EMPTY;
                    return stacks.get(slot).copy();
                }

                @Override
                public int getSlotLimit(int slot) {
                    ensureCache();
                    if (slot < 0 || slot >= stacks.size())
                        return 64;
                    if (slot < stacks.size())
                        return stacks.get(slot).getCount();
                    return 1;
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return isSupportedIngot(stack);
                }

                /** Returns EMPTY for any slot other than 0 to mark as inaccessible. */
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (level == null || level.isClientSide() || amount <= 0)
                        return ItemStack.EMPTY;
                    ensureCache();

                    if (slot != 0 || stacks.isEmpty())
                        return ItemStack.EMPTY;
                    ItemStack topStack = stacks.get(0);
                    int toExtract = Math.min(amount, topStack.getCount());
                    if (simulate) {
                        return topStack.copyWithCount(toExtract);
                    }

                    SUPPRESSING = true;
                    try {
                        int extracted = batchExtractLive(toExtract);
                        if (extracted == 0)
                            return ItemStack.EMPTY;

                        ItemStack result = topStack.copyWithCount(extracted);
                        topStack.setCount(topStack.getCount() - extracted);
                        if (topStack.getCount() <= 0) {
                            stacks.remove(0);
                        }
                        return result;
                    } finally {
                        SUPPRESSING = false;
                    }
                }

                /** Places ingots on the top of the stack. */
                @Override
                public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                    if (level == null || level.isClientSide()
                            || stack.isEmpty() || !isSupportedIngot(stack)) {
                        return stack;
                    }

                    ensureCache();

                    SUPPRESSING = true;
                    try {
                        int inserted = batchInsertLive(stack, stack.getCount(), simulate);
                        if (!simulate && inserted > 0) {
                            if (stacks.isEmpty()) {
                                stacks.add(stack.copyWithCount(inserted));
                            } else {
                                ItemStack topStack = stacks.get(0); // stacks[0] is now the top
                                if (ItemStack.isSameItemSameTags(topStack, stack)) {
                                    topStack.setCount(topStack.getCount() + inserted);
                                } else {
                                    stacks.add(0, stack.copyWithCount(inserted)); // insert at top
                                }
                            }
                        }
                        int leftover = stack.getCount() - inserted;
                        return leftover <= 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
                    } finally {
                        SUPPRESSING = false;
                    }
                }
            };
        }

        /** Removes up to n ingots from the top. Does not check ingot type. */
        private int batchExtractLive(int toExtract) {
            if (level == null || cachedTopPos == null)
                return 0;

            int extracted = 0;
            BlockPos cur = cachedTopPos;

            while (extracted < toExtract && cur != null) {
                BlockState state = level.getBlockState(cur);
                if (!(state.getBlock() instanceof IngotPileBlock pileBlock))
                    break;
                if (!(level.getBlockEntity(cur) instanceof IngotPileBlockEntity pile))
                    break;

                List<Object> raw = rawEntries(pile);
                if (raw.isEmpty()) {
                    // TODO: Piles can't be empty can they?
                    cur = cur.below();
                    continue;
                }

                int available = raw.size();
                int take = Math.min(toExtract - extracted, available);

                raw.subList(available - take, available).clear();
                extracted += take;

                // Update pile block, bypassing TFC's mechanics
                if (raw.isEmpty()) {
                    BlockPos belowPos = cur.below();
                    level.removeBlock(cur, false);
                    cur = (extracted < toExtract
                            && level.getBlockState(belowPos).getBlock() instanceof IngotPileBlock)
                                    ? belowPos
                                    : null;
                } else {
                    level.setBlock(cur,
                            state.setValue(pileBlock.getCountProperty(), raw.size()),
                            Block.UPDATE_CLIENTS);
                    pile.markForSync();
                    break;
                }
            }

            if (cur == null || !(level.getBlockState(cur).getBlock() instanceof IngotPileBlock)) {
                cachedTopPos = findTopPilePosRaw();
            } else {
                cachedTopPos = cur;
            }

            return extracted;
        }

        /** Inserts up to n ingots on the top. */
        private int batchInsertLive(ItemStack stack, int count, boolean simulate) {
            if (level == null || !isSupportedIngot(stack) || count <= 0)
                return 0;

            int inserted = 0;
            int remaining = count;

            while (remaining > 0) {
                if (cachedTopPos != null) {
                    BlockState topState = level.getBlockState(cachedTopPos);
                    if (!(topState.getBlock() instanceof IngotPileBlock topPileBlock))
                        break;
                    if (!isCompatiblePile(topPileBlock, stack))
                        break;

                    int currentCount = topState.getValue(topPileBlock.getCountProperty());
                    int max = getMaxCount(topPileBlock);
                    int canAdd = Math.min(remaining, max - currentCount);

                    if (canAdd > 0) {
                        if (!simulate) {
                            final int toAdd = canAdd;
                            final BlockPos fillPos = cachedTopPos;
                            level.setBlock(fillPos,
                                    topState.setValue(topPileBlock.getCountProperty(), currentCount + toAdd),
                                    Block.UPDATE_CLIENTS);
                            level.getBlockEntity(fillPos, TFCBlockEntities.INGOT_PILE.get())
                                    .ifPresent(pile -> appendEntries(pile, stack, toAdd));
                        }
                        inserted += canAdd;
                        remaining -= canAdd;
                    }

                    if (remaining <= 0)
                        break;

                    // TODO: Does this need to check if build limit was reached?
                    BlockPos newPos = cachedTopPos.above();
                    if (!level.getBlockState(newPos).isAir())
                        break; // Column is blocked.

                    if (!simulate) {
                        Block pileBlock = getPileBlockFor(stack);
                        IngotPileBlock typed = (IngotPileBlock) pileBlock;
                        int max2 = getMaxCount(pileBlock);
                        int toPlace = Math.min(remaining, max2);
                        BlockState newState = typed.defaultBlockState()
                                .setValue(typed.getCountProperty(), toPlace);
                        level.setBlock(newPos, newState, Block.UPDATE_CLIENTS);
                        final int fp = toPlace;
                        level.getBlockEntity(newPos, TFCBlockEntities.INGOT_PILE.get())
                                .ifPresent(pile -> appendEntries(pile, stack, fp));
                        cachedTopPos = newPos;
                        inserted += toPlace;
                        remaining -= toPlace;
                    } else {
                        int max2 = getMaxCount(getPileBlockFor(stack));
                        int toPlace = Math.min(remaining, max2);
                        inserted += toPlace;
                        remaining -= toPlace;
                        break;
                    }

                } else {
                    // First block above pedestal
                    BlockPos above = worldPosition.above();
                    if (!level.getBlockState(above).isAir())
                        break;

                    Block pileBlock = getPileBlockFor(stack);
                    IngotPileBlock typed = (IngotPileBlock) pileBlock;
                    int max = getMaxCount(pileBlock);
                    int toPlace = Math.min(remaining, max);

                    if (!simulate) {
                        BlockState newState = typed.defaultBlockState()
                                .setValue(typed.getCountProperty(), toPlace);
                        level.setBlock(above, newState, Block.UPDATE_CLIENTS);
                        final int fp = toPlace;
                        level.getBlockEntity(above, TFCBlockEntities.INGOT_PILE.get())
                                .ifPresent(pile -> appendEntries(pile, stack, fp));
                        cachedTopPos = above;
                    }
                    inserted += toPlace;
                    remaining -= toPlace;
                }
            }

            return inserted;
        }

        /** Find the topmost ingot pile block above this pedestal. */
        @Nullable
        private BlockPos findTopPilePosRaw() {
            if (level == null)
                return null;
            BlockPos pos = worldPosition.above();
            if (!(level.getBlockState(pos).getBlock() instanceof IngotPileBlock))
                return null;
            while (level.getBlockState(pos.above()).getBlock() instanceof IngotPileBlock) {
                pos = pos.above();
            }
            return pos;
        }

        @SuppressWarnings("unchecked")
        private static List<Object> rawEntries(IngotPileBlockEntity pile) {
            try {
                return (List<Object>) ENTRIES_FIELD.get(pile);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("[TFGPedestal] Cannot read IngotPileBlockEntity.entries", e);
            }
        }

        private void appendEntries(IngotPileBlockEntity pile, ItemStack stack, int count) {
            List<Object> entries = rawEntries(pile);
            ItemStack single = stack.copyWithCount(1);
            try {
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CTOR.newInstance(single.copy()));
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("[TFGPedestal] Cannot create IngotPileBlockEntity.Entry", e);
            }
            pile.markForSync();
        }

        private static ItemStack entryStack(Object entry) {
            try {
                return (ItemStack) ENTRY_STACK_FIELD.get(entry);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("[TFGPedestal] Cannot read Entry.stack", e);
            }
        }

        private static boolean isSupportedIngot(ItemStack stack) {
            return !stack.isEmpty() && (stack.is(TFCTags.Items.PILEABLE_INGOTS) || stack.is(TFCTags.Items.PILEABLE_DOUBLE_INGOTS));
        }

        private static boolean isCompatiblePile(IngotPileBlock pileBlock, ItemStack stack) {
            return (pileBlock instanceof DoubleIngotPileBlock) == stack.is(TFCTags.Items.PILEABLE_DOUBLE_INGOTS);
        }

        private static Block getPileBlockFor(ItemStack stack) {
            return stack.is(TFCTags.Items.PILEABLE_DOUBLE_INGOTS)
                    ? TFCBlocks.DOUBLE_INGOT_PILE.get()
                    : TFCBlocks.INGOT_PILE.get();
        }

        private static int getMaxCount(Block pileBlock) {
            return pileBlock instanceof DoubleIngotPileBlock ? 36 : 64;
        }
    }
}
