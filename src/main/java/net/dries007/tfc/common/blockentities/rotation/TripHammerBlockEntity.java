/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities.rotation;

import com.mojang.math.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.blocks.TripHammerBlock;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.items.HammerItem;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.rotation.Rotation;

import static net.dries007.tfc.TerraFirmaCraft.*;


public class TripHammerBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler>
{
    public static void serverTick(Level level, BlockPos pos, BlockState state, TripHammerBlockEntity hammer)
    {
        int cooldown = hammer.cooldownTicks--;
        final Rotation rotation = hammer.getRotation();
        if (rotation != null)
        {
            final float angle = hammer.getRealRotationDegrees(rotation, 1f);
            final ItemStack item = hammer.inventory.getStackInSlot(0);
            if (cooldown > 0 || item.isEmpty())
            {
                // If we don't track the angle when on cooldown/no item there may be large jumps between the last angle and current angle
                hammer.lastAngle = angle;
                return;
            }
            
            // Must account for:
            // 1. the angle wrapping around from 360 to 0
            // 2. the rotation speed being too fast and/or offset enough to sneak past the expected angle
            // 3. negative rotational speeds
            // 4. no last angle (Float.NEGATIVE_INFINITY), e.g. rotation was just applied
            final float lastAngle = hammer.lastAngle;
            final float minAngle = Math.min(angle, lastAngle);
            final float maxAngle = Math.max(angle, lastAngle);
            if (angle > 90 && angle < 270 && minAngle > 0 && minAngle < 180 && maxAngle > 180)
            {
                if (rotation.positiveDirection() != state.getValue(TripHammerBlock.FACING).getClockWise())
                {
                    level.destroyBlock(pos, true);
                    return;
                }

                final BlockPos anvilPos = pos.relative(state.getValue(TripHammerBlock.FACING));
                // instanceof AnvilBlock is a check that this isn't a rock anvil block, which are incompatible
                if (level.getBlockEntity(anvilPos) instanceof AnvilBlockEntity anvil && level.getBlockState(anvilPos).getBlock() instanceof AnvilBlock)
                {
                    level.playSound(null, pos, TFCSounds.ANVIL_HIT.get(), SoundSource.BLOCKS, 0.4f, 0.2f);
                    if (anvil.workRemotely(ForgeStep.HIT_LIGHT, 12, true))
                    {
                        Helpers.damageItem(item, 1);
                        hammer.markForSync();
                        anvil.markForSync();
                    }
                    if (item.isEmpty())
                    {
                        level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS);
                    }
                    hammer.cooldownTicks = Mth.ceil(0.8f * Mth.TWO_PI / rotation.positiveSpeed());
                    // Update client if the hammer broke
                    hammer.checkForLastTickSync();
                }
            }
            hammer.lastAngle = angle;
        }
        else
        {
            // No last angle
            hammer.lastAngle = Float.NEGATIVE_INFINITY;
        }
    }

    private int cooldownTicks = 10;
    private float lastAngle = Float.NEGATIVE_INFINITY;

    private static final Component NAME = Component.translatable(MOD_ID + ".block_entity.trip_hammer");

    public TripHammerBlockEntity(BlockPos pos, BlockState state)
    {
        this(TFCBlockEntities.TRIP_HAMMER.get(), pos, state);
    }

    public TripHammerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, defaultInventory(1), NAME);
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);
        markForSync();
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return stack.getItem() instanceof HammerItem hammer && hammer.getMetalTexture() != null;
    }

    public float getRealRotationDegrees(Rotation rotation, float partialTick)
    {
        return Constants.RAD_TO_DEG * rotation.angle(partialTick);
    }

    @Nullable
    public Rotation getRotation()
    {
        assert level != null;
        if (level.getBlockEntity(worldPosition.above()) instanceof BladedAxleBlockEntity axle)
        {
            return axle.getRotationNode().rotation();
        }
        return null;
    }
}
