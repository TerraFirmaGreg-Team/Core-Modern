package su.terrafirmagreg.core.common.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import su.terrafirmagreg.core.common.blockentity.appliances.MixingBowlBlockEntity;

public class MixingBowlItem extends CookingApplianceBlockItem {

    public MixingBowlItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CookingApplianceItemStackFluidHandler(
                stack,
                MixingBowlBlockEntity.FLUID_INPUTS,
                MixingBowlBlockEntity.FLUID_OUTPUTS,
                MixingBowlBlockEntity.FLUID_INPUT_CAPACITY,
                MixingBowlBlockEntity.FLUID_OUTPUT_CAPACITY);
    }
}
