package su.terrafirmagreg.core.compat.kjs;

import net.minecraft.resources.ResourceLocation;
import su.terrafirmagreg.core.common.data.blocks.DecorativeAttachedPlantBlock;

public class DecorativeAttachedPlantBlockBuilder extends DecorativePlantBlockBuilder {

	public DecorativeAttachedPlantBlockBuilder(ResourceLocation i) {
		super(i);
	}

	@Override
	public DecorativeAttachedPlantBlock createObject() {
		return new DecorativeAttachedPlantBlock(createExtendedProperties(), getShape());
	}
}
