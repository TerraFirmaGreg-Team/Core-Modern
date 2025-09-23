package su.terrafirmagreg.core.compat.create;

import com.eerussianguy.firmalife.common.blocks.OvenHopperBlock;
import com.eerussianguy.firmalife.common.blocks.greenhouse.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;

import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.common.blocks.devices.CrucibleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegisterEvent;

public final class CustomArmInteractionPointTypes {
    static {
        register("crucible", new CrucibleType());
        register("charcoal_forge", new CharcoalForgeType());
        //register("brick_oven_top", new OvenTopBlockType());
        //register("brick_oven_bottom", new OvenBottomBlockType());
        register("oven_hopper", new OvenHopperBlockType());

        register("rotor_holder", new RotorHolderBlockType());
        //register("single_planter", new SinglePlanterBlockType());
        //register("quad_planter", new QuadPlanterBlockType());
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
    /* All the ones using Firmalife stuff don't work
    public static class SinglePlanterBlockType extends ArmInteractionPointType{
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof LargePlanterBlock) return true;
            else if(state.getBlock() instanceof HangingPlanterBlock) return true;
            else if(state.getBlock() instanceof BonsaiPlanterBlock) return true;
            else return state.getBlock() instanceof TrellisPlanterBlock;
        }
    
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    
    }
    
    public static class QuadPlanterBlockType extends ArmInteractionPointType{
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            if (state.getBlock() instanceof QuadPlanterBlock) return true;
            else return state.getBlock() instanceof HydroponicPlanterBlock;
    
        }
    
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }*/

}
