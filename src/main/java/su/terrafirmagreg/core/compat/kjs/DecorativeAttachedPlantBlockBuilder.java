package su.terrafirmagreg.core.compat.kjs;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import su.terrafirmagreg.core.common.data.blocks.DecorativeAttachedPlantBlock;

public class DecorativeAttachedPlantBlockBuilder extends DecorativePlantBlockBuilder {

	private transient boolean allowVertical;

	public DecorativeAttachedPlantBlockBuilder(ResourceLocation i) {
		super(i);

		allowVertical = false;
	}

	@Info("Whether or not this block can be placed on the top or bottom sides of other blocks. Default false.")
	public DecorativeAttachedPlantBlockBuilder allowVertical(boolean enable) {
		allowVertical = enable;
		return this;
	}


	@Override
	public DecorativeAttachedPlantBlock createObject() {
		return new DecorativeAttachedPlantBlock(createExtendedProperties(), allowVertical);
	}
}
