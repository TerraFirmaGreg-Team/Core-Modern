package su.terrafirmagreg.core.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

import su.terrafirmagreg.core.TFGCore;

/**
 * Provides dynamic "Growth: *" tooltips for blocks that use blockstate integer properties with the name "age".
 * JADE, by default, only provides static tooltips for CropBlock or blocks with BlockStateProperties.AGE_2, AGE_3, AGE_7.
 * So it's useless for blocks that don't use those specific growth stages.
 */
public class DynamicAgeProvider implements IBlockComponentProvider {

    public static final DynamicAgeProvider INSTANCE = new DynamicAgeProvider();
    public static final ResourceLocation GROWTH_ID = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "dynamic_age");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        Block block = state.getBlock();

        int currentAge = -1;
        int maxAge = -1;

        // Check for the blockstate integer property named "age".
        IntegerProperty ageProperty = null;
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty intProp && property.getName().equals("age")) {
                ageProperty = intProp;
                break;
            }
        }

        if (ageProperty != null) {
            currentAge = state.getValue(ageProperty);
            maxAge = ageProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
        }
        // CropBlocks.
        else if (block instanceof CropBlock cropBlock) {
            currentAge = cropBlock.getAge(state);
            maxAge = cropBlock.getMaxAge();
        }

        if (currentAge != -1 && maxAge > 0) {

            tooltip.remove(Identifiers.MC_CROP_PROGRESS);

            float growthValue = ((float) currentAge / maxAge) * 100.0F;

            if (growthValue < 100.0F) {
                tooltip.add(Component.translatable("tooltip.jade.crop_growth",
                        IThemeHelper.get().info(String.format("%.0f%%", growthValue))));
            } else {
                tooltip.add(Component.translatable("tooltip.jade.crop_growth",
                        IThemeHelper.get().success(Component.translatable("tooltip.jade.crop_mature"))));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GROWTH_ID;
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.BODY + 10;
    }
}
