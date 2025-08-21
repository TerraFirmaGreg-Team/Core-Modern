package su.terrafirmagreg.core.common.data.blockentity;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.entities.misc.Seat;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.INBTSerializable;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blocks.LargeNestBoxBlock;
import su.terrafirmagreg.core.common.data.capabilities.ILargeEgg;
import su.terrafirmagreg.core.common.data.capabilities.LargeEggCapability;
import su.terrafirmagreg.core.common.data.contianer.LargeNestBoxContainer;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;

import javax.annotation.Nullable;

public class LargeNestBoxBlockEntity extends TickableInventoryBlockEntity<LargeNestBoxBlockEntity.LargeNestBoxInventory> {

    public static void serverTick(Level level, BlockPos pos, BlockState state, LargeNestBoxBlockEntity nest)
    {

        nest.checkForLastTickSync();

        if (level.getGameTime() % 30 == 0)
        {
            if(!(state.getBlock() instanceof LargeNestBoxBlock)) return;
            Entity sitter = Seat.getSittingEntity(level, pos);
            if (sitter instanceof TFCSniffer sniffer)
            {
                if (sniffer.isReadyForAnimalProduct())
                {
                    final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(pos);

                    final Direction backward = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
                    final Direction left = backward.getClockWise();

                    switch(state.getValue(LargeNestBoxBlock.NEST_PART)){
                        case 0: break;
                        case 1: cursor.move(backward); break;
                        case 2: cursor.move(left); break;
                        case 3: cursor.move(backward).move(left); break;
                    }

                    if (sniffer.getRandom().nextInt(7) == 0)
                    {
                        Helpers.playSound(level, pos, SoundEvents.CHICKEN_EGG);
                        if (Helpers.insertOne(level, cursor, TFGBlockEntities.LARGE_NEST_BOX.get(), sniffer.makeEgg()))
                        {
                            sniffer.setFertilized(false);
                            sniffer.setProductsCooldown();
                            sniffer.stopRiding();
                            nest.markForSync();
                        }
                    }
                }
                else
                {
                    sniffer.stopRiding();
                }
            }

            for (int slot = 0; slot < nest.inventory.getSlots(); slot++)
            {
                final ItemStack stack = nest.inventory.getStackInSlot(slot);
                final @Nullable ILargeEgg egg = LargeEggCapability.get(stack);
                if (egg != null && egg.getHatchDay() > 0 && egg.getHatchDay() <= Calendars.SERVER.getTotalDays())
                {
                    egg.getEntity(level).ifPresent(entity -> {
                        entity.moveTo(pos, 0f, 0f);
                        level.addFreshEntity(entity);
                    });
                    nest.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }



    public static final int SLOTS = 4;
    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.large_nest_box");

    public LargeNestBoxBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFGBlockEntities.LARGE_NEST_BOX.get(), pos, state, LargeNestBoxInventory::new, NAME);

        if (TFCConfig.SERVER.nestBoxEnableAutomation.get())
        {
            sidedInventory.on(new PartialItemHandler(inventory).extractAll(), Direction.DOWN);
        }
    }



    @Override
    public int getSlotStackLimit(int slot)
    {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return Helpers.mightHaveCapability(stack, LargeEggCapability.CAPABILITY);
    }


    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        markForSync();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory inv, Player player)
    {
        return LargeNestBoxContainer.create(this, inv, windowID);
    }


    public static class LargeNestBoxInventory extends InventoryItemHandler implements INBTSerializable<CompoundTag>{
        private final InventoryBlockEntity<?> entity;

        LargeNestBoxInventory(InventoryBlockEntity<?> entity) {
            super(entity, SLOTS);
            this.entity = entity;
        }

        @Override
        public int getSlotStackLimit(int slot)
        {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            return Helpers.mightHaveCapability(stack, LargeEggCapability.CAPABILITY);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            Level level = this.entity.getLevel();
            BlockState state = this.entity.getBlockState();
            BlockPos pos = this.entity.getBlockPos();
            final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(pos);

            final Direction forward = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            final Direction right = forward.getClockWise();
            for (int i = 0; i < getSlots(); i++) {
                int newEggState = 0;


                ItemStack stack = getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    newEggState = 1;
                }
                switch(slot){
                    case 0: cursor.move(forward); break;
                    case 1: cursor.move(forward).move(right); break;
                    case 2: break;
                    case 3: cursor.move(right); break;
                }

                if (!(level.getBlockState(cursor).getBlock() instanceof LargeNestBoxBlock)) break;
                int eggState = level.getBlockState(cursor).getValue(LargeNestBoxBlock.HAS_EGG_TYPE);

                if (eggState != newEggState){
                    BlockState targetState = level.getBlockState(cursor);
                    level.setBlockAndUpdate(cursor, targetState.setValue(LargeNestBoxBlock.HAS_EGG_TYPE, newEggState));
                }
            }
        }
    }
}
