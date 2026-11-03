package su.terrafirmagreg.core.compat.create.mechanicalArm;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenHopperBlock;
import com.eerussianguy.firmalife.common.blocks.VatBlock;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.common.blocks.devices.CrucibleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
        register("multi_planter", new MultiPlanterType());
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

        @SuppressWarnings("DataFlowIssue")
        @Override
        @Nullable
        protected IItemHandler getHandler() {
            if (!cachedHandler.isPresent()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be == null) {
                    return null;
                }
                cachedHandler = be.getCapability(ForgeCapabilities.ITEM_HANDLER);
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
            return new PlanterPoint.SinglePlanterPoint(this, level, pos, state);
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
            return new PlanterPoint.MultiPlanterPoint(this, level, pos, state);
        }
    }

}
