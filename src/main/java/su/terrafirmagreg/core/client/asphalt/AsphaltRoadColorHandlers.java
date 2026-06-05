package su.terrafirmagreg.core.client.asphalt;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingMask;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Asphalt;

@OnlyIn(Dist.CLIENT)
public final class AsphaltRoadColorHandlers {

    private AsphaltRoadColorHandlers() {
    }

    private record AsphaltMarking(AsphaltRoadMarkingMask mask, DyeColor color) {
    }

    public static void registerBlocks(RegisterColorHandlersEvent.Block event) {
        final BlockColor asphaltDecalColor = (state, level, pos, tintIndex) -> {
            if (tintIndex != 1)
                return -1;
            AsphaltMarking marking = getAsphaltMarking(state);
            if (marking == null || marking.mask().isNone())
                return -1;
            return marking.color().getTextColor();
        };

        event.register(asphaltDecalColor,
                TFGBlocks_Asphalt.ASPHALT_ROAD.get(),
                TFGBlocks_Asphalt.ASPHALT_ROAD_SLAB.get());
    }

    public static void registerItems(RegisterColorHandlersEvent.Item event) {
        final ItemColor asphaltLineColor = (stack, tintIndex) -> tintIndex == 1 ? DyeColor.WHITE.getTextColor() : -1;

        event.register(asphaltLineColor,
                TFGBlocks_Asphalt.ASPHALT_ROAD.get().asItem(),
                TFGBlocks_Asphalt.ASPHALT_ROAD_SLAB.get().asItem());
    }

    private static AsphaltMarking getAsphaltMarking(BlockState state) {
        if (state.hasProperty(AsphaltRoadHelper.MASK) && state.hasProperty(AsphaltRoadHelper.COLOR)) {
            return new AsphaltMarking(state.getValue(AsphaltRoadHelper.MASK), state.getValue(AsphaltRoadHelper.COLOR));
        }
        return null;
    }
}
