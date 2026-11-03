package su.terrafirmagreg.core.compat.create.mechanicalArm;

import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.LargePlanterBlock;
import com.eerussianguy.firmalife.common.util.Plantable;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public abstract class PlanterPoint extends AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint {
    protected PlanterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    /**
     * Checks if a given {@link ItemStack} is a {@link Plantable}, and if the given planter is the correct type and tier for that plantable.
     */
    protected boolean isItemValidForPlanter(LargePlanterBlockEntity planter, ItemStack stack) {
        Plantable plant = Plantable.get(stack);
        return plant != null
                && cachedState.getBlock() instanceof LargePlanterBlock planterBlock
                && planterBlock.getPlanterType() == plant.getPlanterType()
                && planter.getTier() >= plant.getTier();
    }

    protected static int findFirstOpenSlot(IItemHandler handler, int slotCount) {
        for (int slot = 0; slot < slotCount; slot++) {
            if (handler.getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    protected static int countOpenSlots(IItemHandler handler, int slotCount) {
        int count = 0;
        for (int slot = 0; slot < slotCount; slot++) {
            if (handler.getStackInSlot(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    protected static int findValidSlotForItem(LargePlanterBlockEntity planter, IItemHandler handler, ItemStack stack) {
        for (int slot = 0; slot < planter.slots(); slot++) {
            if (handler.getStackInSlot(slot).isEmpty() && handler.isItemValid(slot, stack)) {
                return slot;
            }
        }
        return -1;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    @Nullable
    protected IItemHandler getHandler() {
        if (!cachedHandler.isPresent()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null)
                return null;
            cachedHandler = be.getCapability(Capabilities.ITEM);
        }
        return cachedHandler.orElse(null);
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof LargePlanterBlockEntity planter)) {
            return stack;
        }

        // java pass by reference value makes my head hurt

        IItemHandler handler = getHandler();
        if (handler == null)
            return stack;

        // Check if the seed is the correct type for the planter
        if (!isItemValidForPlanter(planter, stack))
            return stack;

        // Find the first valid slot for the seed in the planter
        int slot = findValidSlotForItem(planter, handler, stack);
        if (slot < 0)
            return stack;

        // if simulate, tell arm how many seeds can be inserted into the planter
        if (simulate) {
            int numOpenSlots = countOpenSlots(handler, getSlotCount());
            if (numOpenSlots == 0)
                return stack;   // No open slots, return the original stack

            ItemStack remainder = stack.copy();
            remainder.shrink(Math.min(numOpenSlots, stack.getCount()));
            return remainder;
        }

        ItemStack remainder = stack.copy();
        ItemStack toInsert = remainder.split(1);
        // Insert the seed into the planter and set growth to 0
        if (!handler.insertItem(slot, toInsert, false).isEmpty()) {
            return stack;   // If the insert failed, return the original stack
        }
        planter.setGrowth(slot, 0.0F);
        planter.updateCache();

        return remainder;
    }

    @Override
    public void updateCachedState() {
        BlockState oldState = cachedState;
        super.updateCachedState();
        if (oldState != cachedState)
            cachedHandler.invalidate();
    }

    public static class SinglePlanterPoint extends PlanterPoint {

        public SinglePlanterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            // super.getInteractionPositionVector() returns the center of the block, but we want to offset it based on the type of planter
            if (cachedState.is(FLBlocks.HANGING_PLANTER.get())) {
                return super.getInteractionPositionVector().add(0, 0.25f, 0); // Bottom of hanging planter is at 0.75f
            } else {
                return super.getInteractionPositionVector(); // Center of block for other planters
            }
        }

        @Override
        public int getSlotCount() {
            return 1;
        }
    }

    public static class MultiPlanterPoint extends PlanterPoint {

        public MultiPlanterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            IItemHandler handler = getHandler();
            if (handler == null)
                return super.getInteractionPositionVector();
            // make the arm point at each of the four slots in the planter, depending on which one is open
            switch (findFirstOpenSlot(handler, getSlotCount())) {
                case 0 -> {
                    return super.getInteractionPositionVector().add(-0.25f, -0.125f, -0.25f);
                }
                case 1 -> {
                    return super.getInteractionPositionVector().add(0.25f, -0.125f, 0.25f);
                }
                case 2 -> {
                    return super.getInteractionPositionVector().add(0.25f, -0.125f, -0.25f);
                }
                case 3 -> {
                    return super.getInteractionPositionVector().add(-0.25f, -0.125f, 0.25f);
                }
                default -> {
                    return super.getInteractionPositionVector();
                }
            }
        }

        @Override
        public void updateCachedState() {
            super.updateCachedState();
            cachedAngles = null;
        }

        @Override
        public int getSlotCount() {
            return 4;
        }
    }
}
