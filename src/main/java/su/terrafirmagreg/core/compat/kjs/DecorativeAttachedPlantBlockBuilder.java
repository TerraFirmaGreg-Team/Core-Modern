package su.terrafirmagreg.core.compat.kjs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import su.terrafirmagreg.core.common.data.blocks.DecorativeAttachedPlantBlock;

public class DecorativeAttachedPlantBlockBuilder extends DecorativePlantBlockBuilder {

	public DecorativeAttachedPlantBlockBuilder(ResourceLocation i) {
		super(i);
	}

	@Override
	public DecorativeAttachedPlantBlock createObject() {
		return new DecorativeAttachedPlantBlock(createExtendedProperties().offsetType(BlockBehaviour.OffsetType.XZ), getShape());
	}
}
