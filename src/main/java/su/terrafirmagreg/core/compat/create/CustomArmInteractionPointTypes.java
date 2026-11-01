package su.terrafirmagreg.core.compat.create;

import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenHopperBlock;
import com.eerussianguy.firmalife.common.blocks.VatBlock;
import com.eerussianguy.firmalife.common.blocks.greenhouse.LargePlanterBlock;
import com.eerussianguy.firmalife.common.util.Plantable;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.common.blocks.devices.CrucibleBlock;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.RegisterEvent;

import javax.annotation.Nullable;

public final class CustomArmInteractionPointTypes {
    static {
        register("crucible", new CrucibleType());
        register("charcoal_forge", new CharcoalForgeType());
        //register("brick_oven_top", new OvenTopBlockType());
        //register("brick_oven_bottom", new OvenBottomBlockType());
        register("oven_hopper", new OvenHopperBlockType());
        register("vat", new VatType());

        register("rotor_holder", new RotorHolderBlockType());
        register("single_planter", new SinglePlanterType());
        register("quad_planter", new MultiPlanterType());
    }

    private static <T extends ArmInteractionPointType> void register(String name, T type) {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, Create.asResource(name), type);
    }

    public static void onRegister(final RegisterEvent event) {
        init();
    }

    public static void init() {
    }

    public static class CrucibleType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof CrucibleBlock;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class CharcoalForgeType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof CharcoalForgeBlock;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }
    /* Doesn't work yet
    public static class OvenTopBlockType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof OvenTopBlock;
        }
    
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }
    
    Doesn't work yet
    public static class OvenBottomBlockType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof OvenBottomBlock;
        }
    
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }*/

    public static class OvenHopperBlockType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof OvenHopperBlock;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class VatType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof VatBlock && (!state.hasProperty(VatBlock.SEALED) || !state.getValue(VatBlock.SEALED));
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new VatPoint(this, level, pos, state);
        }
    }

    public static class VatPoint extends ArmInteractionPoint {
        public VatPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected Vec3 getInteractionPositionVector() {
            return Vec3.atCenterOf(pos).add(0, 1 / 16f, 0);
        }

        @Override
        protected Direction getInteractionDirection() {
            return Direction.UP;
        }

        @Override
        protected IItemHandler getHandler() {
            if (!cachedHandler.isPresent()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be == null) {
                    return null;
                }
                LazyOptional<IItemHandler> handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
                cachedHandler = handler;
            }
            return cachedHandler.orElse(null);
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            ItemStack remainder = super.insert(stack, simulate);
            if (!simulate && remainder.getCount() != stack.getCount()) {
                syncVat();
            }
            return remainder;
        }

        @Override
        public ItemStack extract(int slot, int amount, boolean simulate) {
            ItemStack extracted = super.extract(slot, amount, simulate);
            if (!simulate && !extracted.isEmpty()) {
                syncVat();
            }
            return extracted;
        }

        private void syncVat() {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TFCBlockEntity tfcBlockEntity) {
                tfcBlockEntity.markForSync();
            }
        }
    }

    public static class RotorHolderBlockType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return PartAbility.ROTOR_HOLDER.getAllBlocks().contains(state.getBlock());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class SinglePlanterType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(FLBlocks.LARGE_PLANTER.get())
                    || state.is(FLBlocks.BONSAI_PLANTER.get())
                    || state.is(FLBlocks.HANGING_PLANTER.get())
                    || state.is(FLBlocks.TRELLIS_PLANTER.get());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new SinglePlanterPoint(this, level, pos, state);
        }
    }

    public static class MultiPlanterType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(FLBlocks.QUAD_PLANTER.get())
                    || state.is(FLBlocks.HYDROPONIC_PLANTER.get());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new MultiPlanterPoint(this, level, pos, state);
        }
    }

    public abstract static class PlanterPoint extends DepositOnlyArmInteractionPoint {
        protected PlanterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

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

        protected static int findValidSlotForItem(LargePlanterBlockEntity planter, IItemHandler handler, ItemStack stack) {
            for (int slot = 0; slot < planter.slots(); slot++) {
                if (handler.getStackInSlot(slot).isEmpty() && handler.isItemValid(slot, stack)) {
                    return slot;
                }
            }
            return -1;
        }

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

            IItemHandler handler = getHandler();
            if (handler == null)
                return stack;

            // Check if the seed is the correct type for the planter
            if (!isItemValidForPlanter(planter, stack)) {
                return stack;
            }

            // Find the first valid slot for the seed in the planter
            int slot = findValidSlotForItem(planter, handler, stack);
            if (slot < 0)
                return stack;

            // Only take a single seed, leaving the rest of the arm's stack untouched
            ItemStack remainder = stack.copy();
            ItemStack toInsert = remainder.split(1);
            if (simulate)
                return remainder;

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
